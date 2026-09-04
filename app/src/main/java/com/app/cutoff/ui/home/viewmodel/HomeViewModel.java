package com.app.cutoff.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.pojo.CutoffSummary;
import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.data.repository.CutoffRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Backs the Home screen. On init, ensures today's cutoff exists (creating
 * it if this is the first launch of a new period) then exposes the
 * current cutoff id, its expense/incentive totals, and past cutoffs for
 * the RecyclerView list.
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final CutoffRepository cutoffRepository;
    private final BillRepository billRepository;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Long> currentCutoffId = new MutableLiveData<>();

    // Cached once (not recreated per getter call): Transformations.switchMap()/Room's
    // LiveData only start computing once they gain an observer, so calling the getter
    // fresh and immediately reading .getValue() on it always returns null. Caching a
    // single instance here means the LiveData observed by the Fragment is the same
    // instance whose value is read, so it's never stale/null once activated.
    private final LiveData<CutoffEntity> currentCutoff;
    private final LiveData<Double> currentCutoffExpenses;
    private final LiveData<Double> currentCutoffIncentives;

    @Inject
    public HomeViewModel(CutoffRepository cutoffRepository, BillRepository billRepository) {
        this.cutoffRepository = cutoffRepository;
        this.billRepository = billRepository;
        this.currentCutoff = Transformations.switchMap(currentCutoffId, cutoffRepository::observeCutoff);
        this.currentCutoffExpenses = Transformations.switchMap(currentCutoffId, billRepository::observeTotalExpensesForCutoff);
        this.currentCutoffIncentives = Transformations.switchMap(currentCutoffId, billRepository::observeTotalIncentivesForCutoff);
        ensureCurrentCutoff();
    }

    private void ensureCurrentCutoff() {
        ioExecutor.execute(() -> {
            long id = cutoffRepository.ensureCurrentCutoffExists();
            currentCutoffId.postValue(id);
        });
    }

    public LiveData<Long> getCurrentCutoffId() {
        return currentCutoffId;
    }

    public LiveData<CutoffEntity> getCurrentCutoff() {
        return currentCutoff;
    }

    /** Past cutoffs with expense/incentive totals joined in, so each row can show "X left". */
    public LiveData<List<CutoffSummary>> getPastCutoffs() {
        return cutoffRepository.observePastCutoffSummaries();
    }

    /** Total FIXED + VARIABLE bill amount for the current cutoff. */
    public LiveData<Double> getCurrentCutoffExpenses() {
        return currentCutoffExpenses;
    }

    /** Total INCENTIVE amount for the current cutoff. */
    public LiveData<Double> getCurrentCutoffIncentives() {
        return currentCutoffIncentives;
    }

    /**
     * Materializes a future cutoff on demand and returns its database id.
     * The Home screen uses this before navigating to Cutoff Detail.
     */
    public void ensureCutoffForDate(LocalDate date, OnFutureCutoffReadyListener listener) {
        ioExecutor.execute(() -> {
            long id = cutoffRepository.ensureCutoffExistsForDate(date);
            if (listener != null) {
                listener.onReady(id);
            }
        });
    }

    public interface OnFutureCutoffReadyListener {
        void onReady(long cutoffId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}
