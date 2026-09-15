package com.app.cutoff;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelStore;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.app.cutoff.data.backup.BackupManager;
import com.app.cutoff.data.database.AppDatabase;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.data.repository.*;
import com.app.cutoff.ui.home.viewmodel.HomeViewModel;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;
import com.app.cutoff.utils.DateUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import static org.junit.Assert.*;

/** Uses only an in-memory database and test-package files; never the user's database. */
@RunWith(AndroidJUnit4.class)
public class ImportRegressionTest {
    private AppDatabase db;
    private BackupRepository backups;
    private CutoffRepository cutoffs;
    private BillRepository bills;
    private SalaryRepository salary;
    private final ViewModelStore models = new ViewModelStore();
    private final List<Runnable> cleanup = new ArrayList<>();
    private Context context;
    private File file;

    @Before public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        salary = new SalaryRepository(db.salaryDao());
        bills = new BillRepository(db.billDao(), db.cutoffDao());
        cutoffs = new CutoffRepository(db.cutoffDao(), salary, bills);
        backups = new BackupRepository(context, db, db.billDao(), db.cutoffDao(), db.salaryDao(), cutoffs);
        file = File.createTempFile("restore-regression", ".json", context.getCacheDir());
    }

    @After public void tearDown() {
        onMain(() -> { for (Runnable action : cleanup) action.run(); models.clear(); });
        db.close();
        file.delete();
    }

    private void onMain(Runnable action) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action);
    }

    private <T> CountDownLatch watch(LiveData<T> data, Predicate<T> matches) {
        CountDownLatch ready = new CountDownLatch(1);
        Observer<T> observer = value -> { if (matches.test(value)) ready.countDown(); };
        onMain(() -> data.observeForever(observer));
        cleanup.add(() -> data.removeObserver(observer));
        return ready;
    }

    private void await(CountDownLatch ready) throws Exception {
        assertTrue("Live data did not refresh", ready.await(10, TimeUnit.SECONDS));
    }

    private Uri backup(boolean includeToday) throws Exception {
        List<CutoffEntity> periods = new ArrayList<>();
        List<BillEntity> rows = new ArrayList<>();
        BillEntity template = new BillEntity("FIXED", "Rent", 100, null, true, null, null, true, false, 0, "BDO");
        template.setId(50);
        rows.add(template);
        LocalDate today = LocalDate.now();
        LocalDate[] dates = includeToday
                ? new LocalDate[]{today.minusMonths(1), today, today.plusMonths(1)}
                : new LocalDate[]{today.minusMonths(1)};
        long id = 100;
        for (LocalDate date : dates) {
            CutoffEntity cutoff = new CutoffEntity(DateUtils.periodStart(date).toEpochDay(),
                    DateUtils.periodEnd(date).toEpochDay(), 1234);
            cutoff.setId(id);
            periods.add(cutoff);
            BillEntity fixed = new BillEntity("FIXED", "Edited rent", 77, null, false, id, 50L, true, true, 0, "GOTYME");
            fixed.setId(id * 10);
            BillEntity variable = new BillEntity("VARIABLE", "Purchase", 23, null, false, id, null, false, false, 1, "BDO");
            variable.setId(id * 10 + 1);
            BillEntity incentive = new BillEntity("INCENTIVE", "Bonus", 50, null, false, id, null, false, false, 2, "BDO");
            incentive.setId(id * 10 + 2);
            rows.addAll(Arrays.asList(fixed, variable, incentive));
            id++;
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(BackupManager.toJson(new SalaryEntity(2000, 3000), rows, periods)
                    .toString().getBytes(StandardCharsets.UTF_8));
        }
        return Uri.fromFile(file);
    }

    @Test public void onboardingRestoresHistoryAndStandaloneBills() throws Exception {
        SettingsRepository settings = new SettingsRepository(null, null) {
            @Override public void setOnboardingComplete(boolean value) { }
        };
        OnboardingViewModel model = new OnboardingViewModel(salary, bills, settings, backups);
        models.put("onboarding", model);
        CountDownLatch done = watch(model.getImportSucceeded(), Boolean.TRUE::equals);
        model.importFromFile(backup(true));
        await(done);
        assertEquals(3, db.cutoffDao().getAllCutoffsSync().size());
        assertEquals(10, db.billDao().getAllBillsSync().size());
        assertEquals(2000, db.salaryDao().getSalarySync().getFirstToFifteenth(), 0);
        long templateId = db.billDao().getActiveFixedBillTemplatesSync().get(0).getId();
        for (BillEntity bill : db.billDao().getAllBillsSync()) {
            if (!bill.isTemplate() && BillEntity.TYPE_FIXED.equals(bill.getType())) {
                assertEquals(77, bill.getAmount(), 0);
                assertTrue(bill.isPaid());
                assertEquals("GOTYME", bill.getBank());
                assertEquals(Long.valueOf(templateId), bill.getSourceBillId());
            }
        }
    }

    @Test public void retainedHomeSwitchesToImportedIdsWithoutRestart() throws Exception {
        long oldId = cutoffs.ensureCurrentCutoffExists();
        HomeViewModel model = new HomeViewModel(cutoffs, bills);
        models.put("home", model);
        await(watch(model.getCurrentCutoff(), value -> value != null && value.getId() == oldId));
        CountDownLatch restored = watch(model.getCurrentCutoff(),
                value -> value != null && value.getId() != oldId && value.getSalarySnapshot() == 1234);
        CountDownLatch expenses = watch(model.getCurrentCutoffExpenses(), value -> value != null && value == 100);
        CountDownLatch incentives = watch(model.getCurrentCutoffIncentives(), value -> value != null && value == 50);
        backups.importFrom(backup(true));
        await(restored);
        await(expenses);
        await(incentives);
    }

    @Test public void missingCurrentPeriodUsesRestoredDefaults() throws Exception {
        backups.importFrom(backup(false));
        CutoffEntity current = db.cutoffDao().findCutoffContainingDaySync(LocalDate.now().toEpochDay());
        assertNotNull(current);
        assertEquals(DateUtils.isFirstHalf(LocalDate.now()) ? 2000 : 3000, current.getSalarySnapshot(), 0);
        assertEquals(2, db.cutoffDao().getAllCutoffsSync().size());
        assertEquals(5, db.billDao().getAllBillsSync().size());
    }
}
