package com.app.cutoff.domain.usecase;

import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.utils.ValidationUtils;

import javax.inject.Inject;

/**
 * Validates and adds a variable bill or incentive to a cutoff. Kept as a
 * use case (rather than calling BillRepository directly from the
 * ViewModel) since it has validation rules that could grow independently
 * of persistence concerns.
 */
public class AddBillUseCase {

    public enum Kind {VARIABLE, INCENTIVE, FIXED}

    private final BillRepository billRepository;

    @Inject
    public AddBillUseCase(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * @return null on success, or an error message to show the user.
     */
    public String execute(long cutoffId, Kind kind, String name, String rawAmount) {
        if (!ValidationUtils.isNonEmpty(name)) {
            return "Name is required";
        }
        if (!ValidationUtils.isPositiveAmount(rawAmount)) {
            return "Enter a valid amount";
        }

        double amount = Double.parseDouble(rawAmount);
        if (kind == Kind.VARIABLE) {
            billRepository.addVariableBill(cutoffId, name.trim(), amount);
        } else if (kind == Kind.FIXED) {
            billRepository.addFixedBillToCutoff(cutoffId, name.trim(), amount);
        } else {
            billRepository.addIncentive(cutoffId, name.trim(), amount);
        }
        return null;
    }
}
