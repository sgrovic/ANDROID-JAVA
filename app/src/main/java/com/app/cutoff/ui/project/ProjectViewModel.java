package com.app.cutoff.ui.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.cutoff.data.database.entity.BudgetProjectEntity;
import com.app.cutoff.data.repository.BudgetProjectRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProjectViewModel extends ViewModel {
    private final BudgetProjectRepository projects;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Inject
    public ProjectViewModel(BudgetProjectRepository projects) { this.projects = projects; }

    public LiveData<List<BudgetProjectEntity>> projects() { return projects.observeAll(); }
    public void addProject(String name) {
        io.execute(() -> projects.add(name));
    }
    public void updateProject(BudgetProjectEntity project) {
        io.execute(() -> projects.update(project));
    }
    public void deleteProject(BudgetProjectEntity project) {
        io.execute(() -> projects.delete(project));
    }
    @Override protected void onCleared() { io.shutdown(); }
}
