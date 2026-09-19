package com.app.cutoff.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.cutoff.data.database.entity.BudgetProjectEntity;

import java.util.List;

@Dao
public interface BudgetProjectDao {
    @Insert long insert(BudgetProjectEntity project);
    @Update void update(BudgetProjectEntity project);
    @Delete void delete(BudgetProjectEntity project);
    @Query("SELECT * FROM budget_project ORDER BY id DESC")
    LiveData<List<BudgetProjectEntity>> observeAll();
    @Query("SELECT * FROM budget_project WHERE id = :projectId LIMIT 1")
    LiveData<BudgetProjectEntity> observeById(long projectId);
}
