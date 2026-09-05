package com.app.cutoff.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.app.cutoff.data.database.dao.BillDao;
import com.app.cutoff.data.database.dao.CutoffDao;
import com.app.cutoff.data.database.dao.SalaryDao;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;

@Database(
        entities = {BillEntity.class, CutoffEntity.class, SalaryEntity.class},
        version = 2,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    /** Adds the payment bank to existing bills; legacy rows are assigned BDO. */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE bill ADD COLUMN bank TEXT");
            database.execSQL("UPDATE bill SET bank = 'BDO' WHERE bank IS NULL OR TRIM(bank) = ''");
        }
    };

    public static final String DATABASE_NAME = "cutoff.db";

    public abstract BillDao billDao();

    public abstract SalaryDao salaryDao();

    public abstract CutoffDao cutoffDao();
}
