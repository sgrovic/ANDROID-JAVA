package com.app.cutoff.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.cutoff.data.database.entity.BillEntity;

import java.util.List;

@Dao
public interface BillDao {

    @Insert
    long insert(BillEntity bill);

    @Insert
    List<Long> insertAll(List<BillEntity> bills);

    @Update
    void update(BillEntity bill);

    @Delete
    void delete(BillEntity bill);

    // ---- Fixed bill templates (Settings > Fixed bills) ----

    @Query("SELECT * FROM bill WHERE type = 'FIXED' AND isTemplate = 1 ORDER BY sortOrder ASC")
    LiveData<List<BillEntity>> observeFixedBillTemplates();

    @Query("SELECT * FROM bill WHERE type = 'FIXED' AND isTemplate = 1 AND active = 1 ORDER BY sortOrder ASC")
    List<BillEntity> getActiveFixedBillTemplatesSync();

    // Future cutoff snapshots linked to a recurring template. These are used
    // when Settings > Fixed Bills changes so already-created upcoming cutoffs
    // stay in sync with the template. Past/current cutoffs are never touched.
    @Query("SELECT b.* FROM bill b INNER JOIN cutoff c ON b.cutoffId = c.id " +
            "WHERE b.type = 'FIXED' AND b.isTemplate = 0 AND b.sourceBillId = :sourceBillId " +
            "AND c.periodStartEpochDay > :todayEpochDay ORDER BY c.periodStartEpochDay ASC")
    List<BillEntity> getFutureFixedSnapshotsForTemplateSync(long sourceBillId, long todayEpochDay);

    @Query("DELETE FROM bill WHERE type = 'FIXED' AND isTemplate = 0 AND sourceBillId = :sourceBillId " +
            "AND cutoffId IN (SELECT id FROM cutoff WHERE periodStartEpochDay > :todayEpochDay)")
    void deleteFutureFixedSnapshotsForTemplate(long sourceBillId, long todayEpochDay);

    // ---- Rows belonging to a specific cutoff (fixed snapshots + variable + incentives) ----

    @Query("SELECT * FROM bill WHERE cutoffId = :cutoffId AND type = 'FIXED' ORDER BY sortOrder ASC")
    LiveData<List<BillEntity>> observeFixedSnapshotsForCutoff(long cutoffId);

    @Query("SELECT * FROM bill WHERE cutoffId = :cutoffId AND type = 'VARIABLE' ORDER BY id ASC")
    LiveData<List<BillEntity>> observeVariableBillsForCutoff(long cutoffId);

    @Query("SELECT * FROM bill WHERE cutoffId = :cutoffId AND type = 'INCENTIVE' ORDER BY id ASC")
    LiveData<List<BillEntity>> observeIncentivesForCutoff(long cutoffId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bill WHERE cutoffId = :cutoffId AND type IN ('FIXED', 'VARIABLE')")
    LiveData<Double> observeTotalExpensesForCutoff(long cutoffId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bill WHERE cutoffId = :cutoffId AND type = 'INCENTIVE'")
    LiveData<Double> observeTotalIncentivesForCutoff(long cutoffId);

    // ---- Backup export/import (BackupRepository) ----

    @Query("SELECT * FROM bill")
    List<BillEntity> getAllBillsSync();

    @Query("DELETE FROM bill")
    void deleteAllBills();
}
