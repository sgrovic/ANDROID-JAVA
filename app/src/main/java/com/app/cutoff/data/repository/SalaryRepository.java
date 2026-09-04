package com.app.cutoff.data.repository;

import androidx.lifecycle.LiveData;

import com.app.cutoff.data.database.dao.SalaryDao;
import com.app.cutoff.data.database.entity.SalaryEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SalaryRepository {

    private final SalaryDao salaryDao;

    @Inject
    public SalaryRepository(SalaryDao salaryDao) {
        this.salaryDao = salaryDao;
    }

    public LiveData<SalaryEntity> observeSalary() {
        return salaryDao.observeSalary();
    }

    public SalaryEntity getSalarySync() {
        return salaryDao.getSalarySync();
    }

    public void saveSalary(double firstToFifteenth, double sixteenthToEnd) {
        salaryDao.upsert(new SalaryEntity(firstToFifteenth, sixteenthToEnd));
    }

    /** The amount that applies for the given day-of-month, per the 1st-15th / 16th-end split. */
    public double amountForDay(int dayOfMonth) {
        SalaryEntity salary = getSalarySync();
        if (salary == null) return 0;
        return dayOfMonth <= com.app.cutoff.utils.DateUtils.SPLIT_DAY
                ? salary.getFirstToFifteenth()
                : salary.getSixteenthToEnd();
    }
}
