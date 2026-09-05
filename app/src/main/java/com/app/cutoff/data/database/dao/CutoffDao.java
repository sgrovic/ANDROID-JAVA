package com.app.cutoff.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.pojo.CutoffSummary;

import java.util.List;

@Dao
public interface CutoffDao {

    @Insert
    long insert(CutoffEntity cutoff);

    @Update
    void update(CutoffEntity cutoff);

    // Used by CutoffRepository to check "does a cutoff already exist for today?"
    // before auto-generating a new one.
    @Query("SELECT * FROM cutoff WHERE :epochDay BETWEEN periodStartEpochDay AND periodEndEpochDay LIMIT 1")
    CutoffEntity findCutoffContainingDaySync(long epochDay);

    @Query("SELECT * FROM cutoff WHERE id = :id")
    LiveData<CutoffEntity> observeCutoff(long id);

    @Query("SELECT * FROM cutoff WHERE id = :id")
    CutoffEntity getCutoffSync(long id);

    @Query("SELECT * FROM cutoff WHERE periodStartEpochDay > :todayEpochDay ORDER BY periodStartEpochDay ASC")
    List<CutoffEntity> getFutureCutoffsSync(long todayEpochDay);

    @Query("SELECT * FROM cutoff ORDER BY periodStartEpochDay DESC")
    LiveData<List<CutoffEntity>> observeAllCutoffsDesc();

    @Query("SELECT * FROM cutoff WHERE periodEndEpochDay < :todayEpochDay ORDER BY periodStartEpochDay DESC")
    LiveData<List<CutoffEntity>> observePastCutoffs(long todayEpochDay);

    // Same rows as observePastCutoffs, but with expense/incentive totals joined in
    // per cutoff, for rows (e.g. Home > past cutoffs) that need to show "X left"
    // without each list item running its own per-row LiveData query.
    @Query("SELECT c.id AS id, c.periodEndEpochDay AS periodEndEpochDay, c.salarySnapshot AS salarySnapshot, " +
            "COALESCE(SUM(CASE WHEN b.type IN ('FIXED', 'VARIABLE') THEN b.amount ELSE 0 END), 0) AS totalExpenses, " +
            "COALESCE(SUM(CASE WHEN b.type = 'INCENTIVE' THEN b.amount ELSE 0 END), 0) AS totalIncentives " +
            "FROM cutoff c LEFT JOIN bill b ON b.cutoffId = c.id " +
            "WHERE c.periodEndEpochDay < :todayEpochDay " +
            "GROUP BY c.id ORDER BY c.periodStartEpochDay DESC")
    LiveData<List<CutoffSummary>> observePastCutoffSummaries(long todayEpochDay);

    // ---- Backup export/import (BackupRepository) ----

    @Query("SELECT * FROM cutoff")
    List<CutoffEntity> getAllCutoffsSync();

    @Query("DELETE FROM cutoff")
    void deleteAllCutoffs();
}
