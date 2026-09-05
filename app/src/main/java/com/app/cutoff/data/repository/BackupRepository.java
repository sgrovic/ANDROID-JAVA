package com.app.cutoff.data.repository;

import android.content.Context;
import android.net.Uri;

import com.app.cutoff.data.backup.BackupFormatException;
import com.app.cutoff.data.backup.BackupManager;
import com.app.cutoff.data.database.AppDatabase;
import com.app.cutoff.data.database.dao.BillDao;
import com.app.cutoff.data.database.dao.CutoffDao;
import com.app.cutoff.data.database.dao.SalaryDao;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Exports the whole database (salary, fixed-bill templates, cutoffs, and
 * their bills/incentives) to a user-chosen JSON file, and restores it back.
 *
 * Every public method here does file and/or database I/O and must be
 * called from a background thread -- see SettingsViewModel, which runs
 * these through its ioExecutor, matching the pattern already used for
 * SaveSalaryUseCase.
 */
@Singleton
public class BackupRepository {

    private final Context appContext;
    private final AppDatabase appDatabase;
    private final BillDao billDao;
    private final CutoffDao cutoffDao;
    private final SalaryDao salaryDao;

    @Inject
    public BackupRepository(@ApplicationContext Context appContext, AppDatabase appDatabase,
                             BillDao billDao, CutoffDao cutoffDao, SalaryDao salaryDao) {
        this.appContext = appContext;
        this.appDatabase = appDatabase;
        this.billDao = billDao;
        this.cutoffDao = cutoffDao;
        this.salaryDao = salaryDao;
    }

    public void exportTo(Uri destination) throws IOException, JSONException {
        SalaryEntity salary = salaryDao.getSalarySync();
        List<BillEntity> allBills = billDao.getAllBillsSync();
        List<CutoffEntity> allCutoffs = cutoffDao.getAllCutoffsSync();

        String json = BackupManager.toJson(salary, allBills, allCutoffs).toString(2);

        try (OutputStream out = appContext.getContentResolver().openOutputStream(destination, "w")) {
            if (out == null) {
                throw new IOException("Could not open the chosen file for writing.");
            }
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Reads and parses a backup file without touching the database. Used by
     * onboarding's import step, which only needs to preview the salary and
     * fixed-bill values into OnboardingState -- everything is committed
     * together later, when the user taps "Go to home".
     */
    public BackupManager.BackupPayload parseOnly(Uri source) throws IOException, BackupFormatException {
        String json = readAll(source);
        return BackupManager.fromJson(json);
    }

    /**
     * Wipes all bills, cutoffs, and salary data and replaces it with the
     * contents of the given backup file, inside a single transaction so a
     * mid-import failure can't leave the database half-replaced.
     */
    public void importFrom(Uri source) throws IOException, BackupFormatException {
        String json = readAll(source);
        BackupManager.BackupPayload payload = BackupManager.fromJson(json);
        appDatabase.runInTransaction(() -> applyPayload(payload));
    }

    private void applyPayload(BackupManager.BackupPayload payload) {
        billDao.deleteAllBills();
        cutoffDao.deleteAllCutoffs();

        if (payload.salary != null) {
            salaryDao.upsert(payload.salary);
        }

        // Fixed-bill templates get new ids on insert; remember the old -> new
        // mapping so FIXED snapshots below can point sourceBillId at the
        // right (new) template.
        Map<Long, Long> templateIdMap = new HashMap<>();
        for (BillEntity template : payload.fixedBillTemplates) {
            long oldId = template.getId();
            template.setId(0);
            long newId = billDao.insert(template);
            templateIdMap.put(oldId, newId);
        }

        for (BackupManager.CutoffBackupGroup group : payload.cutoffs) {
            CutoffEntity cutoff = group.cutoff;
            cutoff.setId(0);
            long newCutoffId = cutoffDao.insert(cutoff);

            for (BillEntity bill : group.bills) {
                bill.setId(0);
                bill.setCutoffId(newCutoffId);
                Long oldSourceBillId = bill.getSourceBillId();
                bill.setSourceBillId(oldSourceBillId != null ? templateIdMap.get(oldSourceBillId) : null);
                billDao.insert(bill);
            }
        }
    }

    private String readAll(Uri source) throws IOException {
        try (InputStream in = appContext.getContentResolver().openInputStream(source)) {
            if (in == null) {
                throw new IOException("Could not open the chosen file for reading.");
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8.name());
        }
    }
}
