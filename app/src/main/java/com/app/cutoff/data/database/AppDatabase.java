package com.app.cutoff.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.app.cutoff.data.database.dao.BillDao;
import com.app.cutoff.data.database.dao.CutoffDao;
import com.app.cutoff.data.database.dao.SalaryDao;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;

@Database(
        entities = {BillEntity.class, CutoffEntity.class, SalaryEntity.class},
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "cutoff.db";

    public abstract BillDao billDao();

    public abstract SalaryDao salaryDao();

    public abstract CutoffDao cutoffDao();
}
