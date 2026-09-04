package com.app.cutoff.ui.onboarding.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.preference.ThemePreference;
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
 * shared across all 5 onboarding screens and their selections accumulate
 * in one OnboardingState until the final "Go to home" commit.
 */
@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final SalaryRepository salaryRepository;
    private final BillRepository billRepository;
    private final SettingsRepository settingsRepository;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final OnboardingState state = new OnboardingState();
    private final MutableLiveData<Boolean> onboardingComplete = new MutableLiveData<>(false);

    @Inject
    public OnboardingViewModel(SalaryRepository salaryRepository, BillRepository billRepository,
                                SettingsRepository settingsRepository) {
        this.salaryRepository = salaryRepository;
        this.billRepository = billRepository;
        this.settingsRepository = settingsRepository;
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

    public void setThemeMode(String themeMode) {
        state.setThemeMode(themeMode);
    }

    public void addFixedBill(String name, double amount, String iconKey) {
        state.addDraftFixedBill(new OnboardingState.DraftFixedBill(name, amount, iconKey));
    }

    public void removeFixedBill(OnboardingState.DraftFixedBill bill) {
        state.removeDraftFixedBill(bill);
    }

    /** Fires true once everything is persisted, so the Finish screen can navigate to Home. */
    public LiveData<Boolean> getOnboardingComplete() {
        return onboardingComplete;
    }

    /**
     * Commits all onboarding selections to the database. Called once, from
     * the "All set" screen's "Go to home" button.
     */
    public void completeOnboarding() {
        ioExecutor.execute(() -> {
            salaryRepository.saveSalary(state.getFirstToFifteenth(), state.getSixteenthToEnd());
            settingsRepository.setThemeMode(state.getThemeMode());

            int sortOrder = 0;
            for (OnboardingState.DraftFixedBill draft : state.getDraftFixedBills()) {
                billRepository.addFixedBillTemplate(draft.name, draft.amount, draft.iconKey, sortOrder++);
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
