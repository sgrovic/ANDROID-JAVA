package com.app.cutoff.ui.cutoff.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.data.repository.CutoffRepository;
import com.app.cutoff.domain.usecase.AddBillUseCase;
import com.app.cutoff.utils.Constants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Backs the Cutoff Detail screen: salary/remaining header, incentives,
 * fixed bill snapshots, and variable bills for one specific cutoff (id
 * comes from nav arguments via SavedStateHandle).
 *
 * Writes are dispatched on ioExecutor since Room forbids queries/writes on
 * the main thread by default (BillDao's insert/update/delete are plain
 * synchronous calls, not suspend/LiveData-wrapped).
 */
@HiltViewModel
public class CutoffViewModel extends ViewModel {

    private final long cutoffId;
    private final CutoffRepository cutoffRepository;
    private final BillRepository billRepository;
    private final AddBillUseCase addBillUseCase;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Cached once: creating a fresh LiveData per getter call and immediately reading
    // .getValue() on it (as the old code did) always returns null, since Room's
    // query LiveData only starts computing once it has an active observer.
    private final LiveData<CutoffEntity> cutoff;
    private final LiveData<List<BillEntity>> fixedBills;
    private final LiveData<List<BillEntity>> variableBills;
    private final LiveData<List<BillEntity>> incentives;
    private final LiveData<Double> totalExpenses;
    private final LiveData<Double> totalIncentives;

    @Inject
    public CutoffViewModel(SavedStateHandle savedStateHandle, CutoffRepository cutoffRepository,
                            BillRepository billRepository, AddBillUseCase addBillUseCase) {
        this.cutoffId = savedStateHandle.get(Constants.ARG_CUTOFF_ID);
        this.cutoffRepository = cutoffRepository;
        this.billRepository = billRepository;
        this.addBillUseCase = addBillUseCase;
        this.cutoff = cutoffRepository.observeCutoff(cutoffId);
        this.fixedBills = billRepository.observeFixedSnapshotsForCutoff(cutoffId);
        this.variableBills = billRepository.observeVariableBillsForCutoff(cutoffId);
        this.incentives = billRepository.observeIncentivesForCutoff(cutoffId);
        this.totalExpenses = billRepository.observeTotalExpensesForCutoff(cutoffId);
        this.totalIncentives = billRepository.observeTotalIncentivesForCutoff(cutoffId);
    }

    public LiveData<CutoffEntity> getCutoff() {
        return cutoff;
    }

    public LiveData<List<BillEntity>> getFixedBills() {
        return fixedBills;
    }

    public LiveData<List<BillEntity>> getVariableBills() {
        return variableBills;
    }

    public LiveData<List<BillEntity>> getIncentives() {
        return incentives;
    }

    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }

    public LiveData<Double> getTotalIncentives() {
        return totalIncentives;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /** Called from AddBillDialog for a variable bill. */
    public void addVariableBill(String name, String rawAmount) {
        ioExecutor.execute(() -> {
            String error = addBillUseCase.execute(cutoffId, AddBillUseCase.Kind.VARIABLE, name, rawAmount);
            if (error != null) errorMessage.postValue(error);
        });
    }

    /** Called from AddIncentiveDialog. */
    public void addIncentive(String name, String rawAmount) {
        ioExecutor.execute(() -> {
            String error = addBillUseCase.execute(cutoffId, AddBillUseCase.Kind.INCENTIVE, name, rawAmount);
            if (error != null) errorMessage.postValue(error);
        });
    }

    /** Called from AddBillDialog for a one-off fixed bill added directly to this cutoff. */
    public void addFixedBill(String name, String rawAmount) {
        ioExecutor.execute(() -> {
            String error = addBillUseCase.execute(cutoffId, AddBillUseCase.Kind.FIXED, name, rawAmount);
            if (error != null) errorMessage.postValue(error);
        });
    }

    public void deleteBill(BillEntity bill) {
        ioExecutor.execute(() -> billRepository.deleteBill(bill));
    }

    public void updateBill(BillEntity bill) {
        ioExecutor.execute(() -> billRepository.updateBill(bill));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}
