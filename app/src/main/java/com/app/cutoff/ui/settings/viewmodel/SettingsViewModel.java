package com.app.cutoff.ui.settings.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.backup.BackupFormatException;
import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.data.repository.BackupRepository;
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
    private final BackupRepository backupRepository;

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> backupStatusMessage = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(SettingsRepository settingsRepository, SalaryRepository salaryRepository,
                              BillRepository billRepository, SaveSalaryUseCase saveSalaryUseCase,
                              BackupRepository backupRepository) {
        this.settingsRepository = settingsRepository;
        this.salaryRepository = salaryRepository;
        this.billRepository = billRepository;
        this.saveSalaryUseCase = saveSalaryUseCase;
        this.backupRepository = backupRepository;
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

    /** One-shot status text ("Export complete", "Import failed: ...") for a Toast in the Fragment. */
    public LiveData<String> getBackupStatusMessage() {
        return backupStatusMessage;
    }

    public void exportData(Uri destination) {
        ioExecutor.execute(() -> {
            try {
                backupRepository.exportTo(destination);
                backupStatusMessage.postValue("Export complete");
            } catch (Exception e) {
                backupStatusMessage.postValue("Export failed: " + e.getMessage());
            }
        });
    }

    /** Caller is responsible for confirming with the user first -- this replaces all existing data. */
    public void importData(Uri source) {
        ioExecutor.execute(() -> {
            try {
                backupRepository.importFrom(source);
                backupStatusMessage.postValue("Import complete");
            } catch (BackupFormatException e) {
                backupStatusMessage.postValue("Import failed: " + e.getMessage());
            } catch (Exception e) {
                backupStatusMessage.postValue("Import failed: couldn't read that file");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}
