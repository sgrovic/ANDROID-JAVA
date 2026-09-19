package com.app.cutoff.data.repository;

import androidx.lifecycle.LiveData;

import com.app.cutoff.data.database.dao.BillDao;
import com.app.cutoff.data.database.dao.CutoffDao;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.BillEntity;

import java.util.List;
import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Single source of truth for all bill-like rows (fixed templates, fixed
 * snapshots, variable bills, incentives). ViewModels never touch BillDao
 * directly.
 */
@Singleton
public class BillRepository {

    private final BillDao billDao;
    private final CutoffDao cutoffDao;

    @Inject
    public BillRepository(BillDao billDao, CutoffDao cutoffDao) {
        this.billDao = billDao;
        this.cutoffDao = cutoffDao;
    }

    // ---- Fixed bill templates (Settings > Fixed bills management) ----

    public LiveData<List<BillEntity>> observeFixedBillTemplates() {
        return billDao.observeFixedBillTemplates();
    }

    public List<BillEntity> getActiveFixedBillTemplatesSync() {
        return billDao.getActiveFixedBillTemplatesSync();
    }

    public long addFixedBillTemplate(String name, double amount, String iconKey, int sortOrder) {
        return addFixedBillTemplate(name, amount, iconKey, sortOrder,
                com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK,
                BillEntity.RECURRENCE_BOTH_CUTOFFS);
    }


    public long addFixedBillTemplate(String name, double amount, String iconKey, int sortOrder, String bank,
                                     String recurrenceSchedule) {
        BillEntity template = new BillEntity(
                BillEntity.TYPE_FIXED, name, amount, iconKey,
                /*isTemplate=*/ true, /*cutoffId=*/ null, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, sortOrder, bank);
        template.setRecurrenceSchedule(recurrenceSchedule);
        long templateId = billDao.insert(template);
        template.setId(templateId);

        // If upcoming cutoffs were already opened/generated, add the new
        // recurring bill to those cutoffs too. The current and past cutoffs
        // remain historical snapshots and are never changed.
        copyTemplateIntoFutureCutoffs(template);
        return templateId;
    }

    public void updateFixedBillTemplate(BillEntity template) {
        billDao.update(template);

        // Keep already-created upcoming cutoffs synchronized with Settings.
        // Current/past snapshots remain untouched so historical records and
        // current-period planning are preserved.
        long todayEpochDay = LocalDate.now().toEpochDay();
        List<BillEntity> snapshots = billDao.getFutureFixedSnapshotsForTemplateSync(
                template.getId(), todayEpochDay);
        for (BillEntity snapshot : snapshots) {
            CutoffEntity cutoff = cutoffDao.getCutoffSync(snapshot.getCutoffId());
            if (cutoff == null || !belongsInCutoff(template, cutoff)) {
                billDao.delete(snapshot);
                continue;
            }
            snapshot.setName(template.getName());
            snapshot.setAmount(template.getAmount());
            snapshot.setIconKey(template.getIconKey());
            snapshot.setBank(template.getBank());
            snapshot.setSortOrder(template.getSortOrder());
            snapshot.setRecurrenceSchedule(template.getRecurrenceSchedule());
            billDao.update(snapshot);
        }
        // A schedule may now include a future cutoff that did not previously
        // have this bill. Add only the missing snapshots.
        copyTemplateIntoFutureCutoffs(template);
    }

    public void deleteFixedBillTemplate(BillEntity template) {
        // Remove this recurring bill from already-created upcoming cutoffs,
        // but leave current/past snapshots untouched as historical records.
        billDao.deleteFutureFixedSnapshotsForTemplate(
                template.getId(), LocalDate.now().toEpochDay());
        billDao.delete(template);
    }

    // ---- Rows belonging to a specific cutoff ----

    public LiveData<List<BillEntity>> observeFixedSnapshotsForCutoff(long cutoffId) {
        return billDao.observeFixedSnapshotsForCutoff(cutoffId);
    }

    public LiveData<List<BillEntity>> observeVariableBillsForCutoff(long cutoffId) {
        return billDao.observeVariableBillsForCutoff(cutoffId);
    }

    public LiveData<List<BillEntity>> observeIncentivesForCutoff(long cutoffId) {
        return billDao.observeIncentivesForCutoff(cutoffId);
    }

    public LiveData<Double> observeTotalExpensesForCutoff(long cutoffId) {
        return billDao.observeTotalExpensesForCutoff(cutoffId);
    }

    public LiveData<Double> observeTotalIncentivesForCutoff(long cutoffId) {
        return billDao.observeTotalIncentivesForCutoff(cutoffId);
    }

    public void addVariableBill(long cutoffId, String name, double amount) {
        addVariableBill(cutoffId, name, amount, com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK);
    }

    public void addVariableBill(long cutoffId, String name, double amount, String bank) {
        BillEntity bill = new BillEntity(
                BillEntity.TYPE_VARIABLE, name, amount, /*iconKey=*/ null,
                /*isTemplate=*/ false, cutoffId, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, /*sortOrder=*/ 0, bank);
        billDao.insert(bill);
    }

    /** A one-off fixed bill added directly to a cutoff (not backed by a template). */
    public void addFixedBillToCutoff(long cutoffId, String name, double amount) {
        addFixedBillToCutoff(cutoffId, name, amount, com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK);
    }

    public void addFixedBillToCutoff(long cutoffId, String name, double amount, String bank) {
        BillEntity bill = new BillEntity(
                BillEntity.TYPE_FIXED, name, amount, /*iconKey=*/ null,
                /*isTemplate=*/ false, cutoffId, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, /*sortOrder=*/ 0, bank);
        billDao.insert(bill);
    }

    public void addIncentive(long cutoffId, String name, double amount) {
        BillEntity incentive = new BillEntity(
                BillEntity.TYPE_INCENTIVE, name, amount, /*iconKey=*/ null,
                /*isTemplate=*/ false, cutoffId, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, /*sortOrder=*/ 0);
        billDao.insert(incentive);
    }

    public void updateBill(BillEntity bill) {
        billDao.update(bill);
    }

    public void deleteBill(BillEntity bill) {
        billDao.delete(bill);
    }

    /**
     * Copies every active fixed-bill template into a snapshot row for the
     * newly created cutoff. Called only by CutoffRepository at cutoff
     * generation time.
     */
    /**
     * Adds a recurring template to every already-created future cutoff.
     * This is intentionally separate from copyActiveTemplatesIntoCutoff(),
     * which handles a single newly-created cutoff.
     */
    private void copyTemplateIntoFutureCutoffs(BillEntity template) {
        long todayEpochDay = LocalDate.now().toEpochDay();
        List<CutoffEntity> futureCutoffs = cutoffDao.getFutureCutoffsSync(todayEpochDay);

        // Load existing snapshots once instead of querying once per cutoff.
        List<BillEntity> existing = billDao.getFutureFixedSnapshotsForTemplateSync(
                template.getId(), todayEpochDay);

        for (CutoffEntity cutoff : futureCutoffs) {
            // Do not create a duplicate if this template is already present
            // in the future cutoff. This can happen when a cutoff was created
            // after the template was added and the app is syncing again.
            boolean alreadyExists = false;
            for (BillEntity snapshot : existing) {
                if (snapshot.getCutoffId() != null && snapshot.getCutoffId() == cutoff.getId()) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists && belongsInCutoff(template, cutoff)) {
                BillEntity snapshot = new BillEntity(
                        BillEntity.TYPE_FIXED, template.getName(), template.getAmount(),
                        template.getIconKey(), /*isTemplate=*/ false, cutoff.getId(),
                        template.getId(), /*active=*/ true, /*paid=*/ false,
                        template.getSortOrder(), template.getBank());
                snapshot.setRecurrenceSchedule(template.getRecurrenceSchedule());
                billDao.insert(snapshot);
            }
        }
    }

    void copyActiveTemplatesIntoCutoff(long cutoffId) {
        List<BillEntity> templates = billDao.getActiveFixedBillTemplatesSync();
        for (BillEntity template : templates) {
            CutoffEntity cutoff = cutoffDao.getCutoffSync(cutoffId);
            if (cutoff == null || !belongsInCutoff(template, cutoff)) continue;
            BillEntity snapshot = new BillEntity(
                    BillEntity.TYPE_FIXED, template.getName(), template.getAmount(),
                    template.getIconKey(), /*isTemplate=*/ false, cutoffId, template.getId(),
                    /*active=*/ true, /*paid=*/ false, template.getSortOrder(), template.getBank());
            snapshot.setRecurrenceSchedule(template.getRecurrenceSchedule());
            billDao.insert(snapshot);
        }
    }

    private boolean belongsInCutoff(BillEntity template, CutoffEntity cutoff) {
        boolean firstCutoff = java.time.LocalDate.ofEpochDay(cutoff.getPeriodEndEpochDay())
                .getDayOfMonth() == com.app.cutoff.utils.DateUtils.SPLIT_DAY;
        String schedule = template.getRecurrenceSchedule();
        return BillEntity.RECURRENCE_BOTH_CUTOFFS.equals(schedule)
                || (firstCutoff && BillEntity.RECURRENCE_FIRST_CUTOFF.equals(schedule))
                || (!firstCutoff && BillEntity.RECURRENCE_SECOND_CUTOFF.equals(schedule));
    }
}
