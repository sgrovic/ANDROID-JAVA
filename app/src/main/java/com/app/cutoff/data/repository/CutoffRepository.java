package com.app.cutoff.data.repository;

import androidx.lifecycle.LiveData;

import com.app.cutoff.data.database.dao.CutoffDao;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.data.database.pojo.CutoffSummary;
import com.app.cutoff.utils.DateUtils;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Owns cutoff auto-generation. Cutoffs are never created manually by the
 * user — this repository checks, on demand (e.g. app launch), whether
 * today's period already has a CutoffEntity, and if not, creates one:
 * snapshotting the correct salary half and copying active fixed-bill
 * templates into it via BillRepository.
 *
 * ensureCurrentCutoffExists() must be called off the main thread (see
 * HomeViewModel, which dispatches it through an executor/coroutine-free
 * background thread since this is Java 8, not Kotlin).
 */
@Singleton
public class CutoffRepository {

    private final CutoffDao cutoffDao;
    private final SalaryRepository salaryRepository;
    private final BillRepository billRepository;

    @Inject
    public CutoffRepository(CutoffDao cutoffDao, SalaryRepository salaryRepository,
                             BillRepository billRepository) {
        this.cutoffDao = cutoffDao;
        this.salaryRepository = salaryRepository;
        this.billRepository = billRepository;
    }

    public LiveData<List<CutoffEntity>> observeAllCutoffsDesc() {
        return cutoffDao.observeAllCutoffsDesc();
    }

    public LiveData<List<CutoffEntity>> observePastCutoffs() {
        return cutoffDao.observePastCutoffs(LocalDate.now().toEpochDay());
    }

    /** Past cutoffs with expense/incentive totals joined in, for the "X left" row label. */
    public LiveData<List<CutoffSummary>> observePastCutoffSummaries() {
        return cutoffDao.observePastCutoffSummaries(LocalDate.now().toEpochDay());
    }

    public LiveData<CutoffEntity> observeCutoff(long id) {
        return cutoffDao.observeCutoff(id);
    }

    /**
     * Ensures a CutoffEntity exists for the period containing today's date.
     * Safe to call every app launch — it's a no-op if one already exists.
     * MUST be called from a background thread (Room throws on main thread
     * for the sync queries used here).
     *
     * @return the id of today's cutoff (existing or newly created).
     */
    public long ensureCurrentCutoffExists() {
        return ensureCutoffExistsForDate(LocalDate.now());
    }

    /**
     * Ensures the cutoff containing {@code date} exists and returns its id.
     * This is also used when the user opens a future cutoff from Home so that
     * the normal Cutoff Detail screen can be used for advance budget planning.
     * MUST be called from a background thread.
     */
    public long ensureCutoffExistsForDate(LocalDate date) {
        long epochDay = date.toEpochDay();

        CutoffEntity existing = cutoffDao.findCutoffContainingDaySync(epochDay);
        if (existing != null) {
            return existing.getId();
        }

        LocalDate periodStart = DateUtils.periodStart(date);
        LocalDate periodEnd = DateUtils.periodEnd(date);
        double salarySnapshot = getSalaryForDate(date);

        CutoffEntity newCutoff = new CutoffEntity(
                periodStart.toEpochDay(), periodEnd.toEpochDay(), salarySnapshot);
        long newCutoffId = cutoffDao.insert(newCutoff);

        // Future cutoffs get the same active fixed-bill templates as a normal
        // newly-created cutoff. They become snapshots that can be edited
        // independently for advance planning.
        billRepository.copyActiveTemplatesIntoCutoff(newCutoffId);

        return newCutoffId;
    }

    private double getSalaryForDate(LocalDate date) {
        SalaryEntity salary = salaryRepository.getSalarySync();
        if (salary == null) return 0;
        return DateUtils.isFirstHalf(date)
                ? salary.getFirstToFifteenth()
                : salary.getSixteenthToEnd();
    }
}
