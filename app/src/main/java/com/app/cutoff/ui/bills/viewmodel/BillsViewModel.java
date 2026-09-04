package com.app.cutoff.ui.bills.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.utils.ValidationUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Backs the Fixed Bills management screen (screen 9 in the wireframe —
 * add/edit/delete recurring fixed-bill templates). Changes here only
 * affect future auto-generated cutoffs, never past ones.
 *
 * Writes are dispatched on ioExecutor since Room forbids queries/writes on
 * the main thread by default (BillDao's insert/update/delete are plain
 * synchronous calls, not suspend/LiveData-wrapped).
 */
@HiltViewModel
public class BillsViewModel extends ViewModel {

    private final BillRepository billRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public BillsViewModel(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public LiveData<List<BillEntity>> getFixedBillTemplates() {
        return billRepository.observeFixedBillTemplates();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addFixedBillTemplate(String name, String rawAmount, String iconKey, int sortOrder) {
        if (!ValidationUtils.isNonEmpty(name)) {
            errorMessage.setValue("Name is required");
            return;
        }
        if (!ValidationUtils.isPositiveAmount(rawAmount)) {
            errorMessage.setValue("Enter a valid amount");
            return;
        }
        ioExecutor.execute(() ->
                billRepository.addFixedBillTemplate(name.trim(), Double.parseDouble(rawAmount), iconKey, sortOrder));
    }

    public void updateFixedBillTemplate(BillEntity template) {
        ioExecutor.execute(() -> billRepository.updateFixedBillTemplate(template));
    }

    public void deleteFixedBillTemplate(BillEntity template) {
        ioExecutor.execute(() -> billRepository.deleteFixedBillTemplate(template));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}
