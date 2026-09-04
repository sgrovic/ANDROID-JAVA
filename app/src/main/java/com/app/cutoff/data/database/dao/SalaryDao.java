package com.app.cutoff.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.app.cutoff.data.database.entity.SalaryEntity;

@Dao
public interface SalaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SalaryEntity salary);

    @Query("SELECT * FROM salary WHERE id = 'salary_singleton' LIMIT 1")
    LiveData<SalaryEntity> observeSalary();

    @Query("SELECT * FROM salary WHERE id = 'salary_singleton' LIMIT 1")
    SalaryEntity getSalarySync();
}
