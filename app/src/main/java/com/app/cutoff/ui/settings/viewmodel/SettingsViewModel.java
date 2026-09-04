package com.app.cutoff.ui.settings.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.data.repository.SalaryRepository;
import com.app.cutoff.data.repository.SettingsRepository;
import com.app.cutoff.domain.usecase.SaveSalaryUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final SettingsRepository settingsRepository;
    private final SalaryRepository salaryRepository;
    private final BillRepository billRepository;
    private final SaveSalaryUseCase saveSalaryUseCase;

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(SettingsRepository settingsRepository, SalaryRepository salaryRepository,
                              BillRepository billRepository, SaveSalaryUseCase saveSalaryUseCase) {
        this.settingsRepository = settingsRepository;
        this.salaryRepository = salaryRepository;
        this.billRepository = billRepository;
        this.saveSalaryUseCase = saveSalaryUseCase;
    }

    public LiveData<SalaryEntity> getSalary() {
        return salaryRepository.observeSalary();
    }

    /** Count of fixed-bill templates, shown next to "Fixed bills" in Settings. */
    public LiveData<Integer> getFixedBillCount() {
        return Transformations.map(billRepository.observeFixedBillTemplates(), java.util.List::size);
    }

    public String getThemeMode() {
        return settingsRepository.getThemeMode();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /** @return true if the Activity should be recreated to apply the new theme. */
    public boolean setThemeMode(String themeMode) {
        boolean changed = !themeMode.equals(settingsRepository.getThemeMode());
        settingsRepository.setThemeMode(themeMode);
        return changed;
    }

    private final java.util.concurrent.ExecutorService ioExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor();

    public void updateSalary(String rawFirstToFifteenth, String rawSixteenthToEnd) {
        ioExecutor.execute(() -> {
            String error = saveSalaryUseCase.execute(rawFirstToFifteenth, rawSixteenthToEnd);
            if (error != null) errorMessage.postValue(error);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}
