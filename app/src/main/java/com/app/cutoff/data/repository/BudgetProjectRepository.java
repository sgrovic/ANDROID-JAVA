package com.app.cutoff.data.repository;

import androidx.lifecycle.LiveData;

import com.app.cutoff.data.database.dao.BudgetProjectDao;
import com.app.cutoff.data.database.entity.BudgetProjectEntity;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BudgetProjectRepository {
    private final BudgetProjectDao dao;

    @Inject
    public BudgetProjectRepository(BudgetProjectDao dao) {
        this.dao = dao;
    }

    public LiveData<List<BudgetProjectEntity>> observeAll() { return dao.observeAll(); }
    public LiveData<BudgetProjectEntity> observeById(long id) { return dao.observeById(id); }
    public long add(String name) {
        return dao.insert(new BudgetProjectEntity(name, 0, 0,
                LocalDate.now().toEpochDay()));
    }
    public void update(BudgetProjectEntity project) { dao.update(project); }
    public void delete(BudgetProjectEntity project) { dao.delete(project); }
}
