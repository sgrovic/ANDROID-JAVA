package com.app.cutoff.ui.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.BudgetProjectEntity;
import com.app.cutoff.data.database.entity.PlannedItemEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.data.repository.BillRepository;
import com.app.cutoff.data.repository.BudgetProjectRepository;
import com.app.cutoff.data.repository.PlannedItemRepository;
import com.app.cutoff.data.repository.SalaryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProjectDetailViewModel extends ViewModel {
    private final SalaryRepository salary;
    private final BillRepository bills;
    private final PlannedItemRepository plans;
    private final BudgetProjectRepository projects;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Inject
    public ProjectDetailViewModel(SalaryRepository salary, BillRepository bills,
                                  PlannedItemRepository plans,
                                  BudgetProjectRepository projects) {
        this.salary = salary;
        this.bills = bills;
        this.plans = plans;
        this.projects = projects;
    }

    public LiveData<SalaryEntity> salary() { return salary.observeSalary(); }
    public LiveData<List<BillEntity>> fixed() { return bills.observeFixedBillTemplates(); }
    public LiveData<BudgetProjectEntity> project(long id) { return projects.observeById(id); }
    public LiveData<List<PlannedItemEntity>> plans(long id) { return plans.observeForProject(id); }
    public void add(long projectId, String name, double amount, boolean first, boolean second,
                    int firstPercent) {
        io.execute(() -> plans.add(projectId, name, amount, first, second, firstPercent));
    }
    public void delete(PlannedItemEntity item) { io.execute(() -> plans.delete(item)); }
    public void update(PlannedItemEntity item) { io.execute(() -> plans.update(item)); }
    public void applyOptimization(List<PlannedItemEntity> items, int firstPercent) {
        io.execute(() -> {
            for (PlannedItemEntity item : items) {
                item.setFirstCutoffPercent(firstPercent);
                item.setApplyFirstCutoff(firstPercent > 0);
                item.setApplySecondCutoff(firstPercent < 100);
            }
            plans.updateAll(items);
        });
    }
    public void updateProject(BudgetProjectEntity project) {
        io.execute(() -> projects.update(project));
    }
    public void deleteProject(BudgetProjectEntity project) {
        io.execute(() -> projects.delete(project));
    }
    @Override protected void onCleared() { io.shutdown(); }
}
