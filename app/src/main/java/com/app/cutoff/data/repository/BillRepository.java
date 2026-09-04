package com.app.cutoff.data.repository;

import androidx.lifecycle.LiveData;

import com.app.cutoff.data.database.dao.BillDao;
import com.app.cutoff.data.database.entity.BillEntity;

import java.util.List;

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

    @Inject
    public BillRepository(BillDao billDao) {
        this.billDao = billDao;
    }

    // ---- Fixed bill templates (Settings > Fixed bills management) ----

    public LiveData<List<BillEntity>> observeFixedBillTemplates() {
        return billDao.observeFixedBillTemplates();
    }

    public List<BillEntity> getActiveFixedBillTemplatesSync() {
        return billDao.getActiveFixedBillTemplatesSync();
    }

    public long addFixedBillTemplate(String name, double amount, String iconKey, int sortOrder) {
        BillEntity template = new BillEntity(
                BillEntity.TYPE_FIXED, name, amount, iconKey,
                /*isTemplate=*/ true, /*cutoffId=*/ null, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, sortOrder);
        return billDao.insert(template);
    }

    public void updateFixedBillTemplate(BillEntity template) {
        billDao.update(template);
    }

    public void deleteFixedBillTemplate(BillEntity template) {
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
        BillEntity bill = new BillEntity(
                BillEntity.TYPE_VARIABLE, name, amount, /*iconKey=*/ null,
                /*isTemplate=*/ false, cutoffId, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, /*sortOrder=*/ 0);
        billDao.insert(bill);
    }

    /** A one-off fixed bill added directly to a cutoff (not backed by a template). */
    public void addFixedBillToCutoff(long cutoffId, String name, double amount) {
        BillEntity bill = new BillEntity(
                BillEntity.TYPE_FIXED, name, amount, /*iconKey=*/ null,
                /*isTemplate=*/ false, cutoffId, /*sourceBillId=*/ null,
                /*active=*/ true, /*paid=*/ false, /*sortOrder=*/ 0);
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
    void copyActiveTemplatesIntoCutoff(long cutoffId) {
        List<BillEntity> templates = billDao.getActiveFixedBillTemplatesSync();
        for (BillEntity template : templates) {
            BillEntity snapshot = new BillEntity(
                    BillEntity.TYPE_FIXED, template.getName(), template.getAmount(),
                    template.getIconKey(), /*isTemplate=*/ false, cutoffId, template.getId(),
                    /*active=*/ true, /*paid=*/ false, template.getSortOrder());
            billDao.insert(snapshot);
        }
    }
}
