package com.app.cutoff.ui.onboarding.viewmodel;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.backup.BackupManager;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.preference.ThemePreference;
import com.app.cutoff.data.repository.BackupRepository;
import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.data.repository.SalaryRepository;
import com.app.cutoff.data.repository.SettingsRepository;
import com.app.cutoff.ui.onboarding.model.OnboardingState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Scoped to the onboarding nav graph (via hiltViewModel(navGraphId) at the
 * fragment level — see WelcomeFragment/etc.), so the same instance is
 * shared across all 6 onboarding screens and their selections accumulate
 * in one OnboardingState until the final "Go to home" commit.
 *
 * Theme is the one exception: it's persisted immediately on selection (see
 * setThemeMode()) so ThemeFragment can preview it live, the same way
 * SettingsFragment does post-onboarding. Salary and fixed bills stay
 * draft-only in OnboardingState until completeOnboarding().
 */
@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final SalaryRepository salaryRepository;
    private final BillRepository billRepository;
    private final SettingsRepository settingsRepository;
    private final BackupRepository backupRepository;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final OnboardingState state = new OnboardingState();
    private final MutableLiveData<Boolean> onboardingComplete = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> importSucceeded = new MutableLiveData<>();
    private final MutableLiveData<String> importError = new MutableLiveData<>();

    @Inject
    public OnboardingViewModel(SalaryRepository salaryRepository, BillRepository billRepository,
                                SettingsRepository settingsRepository, BackupRepository backupRepository) {
        this.salaryRepository = salaryRepository;
        this.billRepository = billRepository;
        this.settingsRepository = settingsRepository;
        this.backupRepository = backupRepository;
        // Sensible starting defaults, matching the wireframe's starter fixed bills.
        state.setThemeMode(ThemePreference.MODE_SYSTEM);
    }

    public OnboardingState getState() {
        return state;
    }

    public void setSalary(double firstToFifteenth, double sixteenthToEnd) {
        state.setFirstToFifteenth(firstToFifteenth);
        state.setSixteenthToEnd(sixteenthToEnd);
    }

    /**
     * Persists the theme immediately (rather than waiting for
     * completeOnboarding()) so ThemeFragment can apply it live, matching
     * SettingsFragment's behavior. @return true if the Activity should be
     * recreated to apply the change.
     */
    public boolean setThemeMode(String themeMode) {
        boolean changed = !themeMode.equals(state.getThemeMode());
        state.setThemeMode(themeMode);
        settingsRepository.setThemeMode(themeMode);
        return changed;
    }

    public void addFixedBill(String name, double amount, String iconKey) {
        addFixedBill(name, amount, iconKey, com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK);
    }

    public void addFixedBill(String name, double amount, String iconKey, String bank) {
        state.addDraftFixedBill(new OnboardingState.DraftFixedBill(name, amount, iconKey, bank));
    }

    public void removeFixedBill(OnboardingState.DraftFixedBill bill) {
        state.removeDraftFixedBill(bill);
    }

    /** Fires true once the file is parsed and OnboardingState is prefilled; fires once per attempt. */
    public LiveData<Boolean> getImportSucceeded() {
        return importSucceeded;
    }

    /** Non-null when the last import attempt failed, with a message suitable for a Toast. */
    public LiveData<String> getImportError() {
        return importError;
    }

    /**
     * Parses the chosen backup file and prefills salary + fixed bills into
     * OnboardingState -- nothing is written to the database here, that
     * still only happens in completeOnboarding(). The backup file doesn't
     * carry a theme (see BackupManager), so theme is unaffected.
     */
    public void importFromFile(@NonNull Uri source) {
        ioExecutor.execute(() -> {
            try {
                BackupManager.BackupPayload payload = backupRepository.parseOnly(source);

                if (payload.salary != null) {
                    state.setFirstToFifteenth(payload.salary.getFirstToFifteenth());
                    state.setSixteenthToEnd(payload.salary.getSixteenthToEnd());
                }

                state.getDraftFixedBills().clear();
                for (BillEntity template : payload.fixedBillTemplates) {
                    state.addDraftFixedBill(new OnboardingState.DraftFixedBill(
                            template.getName(), template.getAmount(), template.getIconKey(), template.getBank()));
                }

                state.setImported(true);
                importSucceeded.postValue(true);
            } catch (Exception e) {
                importError.postValue("Couldn't read that file");
            }
        });
    }

    /** Fires true once everything is persisted, so the Finish screen can navigate to Home. */
    public LiveData<Boolean> getOnboardingComplete() {
        return onboardingComplete;
    }

    /**
     * Commits all onboarding selections to the database. Called once, from
     * the "All set" screen's "Go to home" button. Theme was already
     * persisted when it was picked (see setThemeMode()); saving it again
     * here is harmless and keeps this method self-contained.
     */
    public void completeOnboarding() {
        ioExecutor.execute(() -> {
            salaryRepository.saveSalary(state.getFirstToFifteenth(), state.getSixteenthToEnd());
            settingsRepository.setThemeMode(state.getThemeMode());

            int sortOrder = 0;
            for (OnboardingState.DraftFixedBill draft : state.getDraftFixedBills()) {
                billRepository.addFixedBillTemplate(draft.name, draft.amount, draft.iconKey, sortOrder++, draft.bank);
            }

            settingsRepository.setOnboardingComplete(true);
            onboardingComplete.postValue(true);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}
