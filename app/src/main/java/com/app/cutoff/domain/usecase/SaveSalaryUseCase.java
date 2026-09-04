package com.app.cutoff.domain.usecase;

import com.app.cutoff.data.repository.SalaryRepository;
import com.app.cutoff.utils.ValidationUtils;

import javax.inject.Inject;

public class SaveSalaryUseCase {

    private final SalaryRepository salaryRepository;

    @Inject
    public SaveSalaryUseCase(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    /**
     * @return null on success, or an error message to show the user.
     */
    public String execute(String rawFirstToFifteenth, String rawSixteenthToEnd) {
        if (!ValidationUtils.isPositiveAmount(rawFirstToFifteenth)
                || !ValidationUtils.isPositiveAmount(rawSixteenthToEnd)) {
            return "Enter valid amounts for both periods";
        }
        double first = Double.parseDouble(rawFirstToFifteenth);
        double second = Double.parseDouble(rawSixteenthToEnd);
        salaryRepository.saveSalary(first, second);
        return null;
    }
}
