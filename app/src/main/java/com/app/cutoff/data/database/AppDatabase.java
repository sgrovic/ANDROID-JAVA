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
        version = 4,
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

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE bill ADD COLUMN paymentStatus TEXT DEFAULT 'PENDING'");
            long today = java.time.LocalDate.now().toEpochDay();
            db.execSQL("UPDATE bill SET paymentStatus = CASE WHEN cutoffId IN (SELECT id FROM cutoff WHERE periodEndEpochDay < " + today + ") THEN 'PAID' ELSE 'PENDING' END, paid = CASE WHEN cutoffId IN (SELECT id FROM cutoff WHERE periodEndEpochDay < " + today + ") THEN 1 ELSE 0 END WHERE isTemplate = 0 AND type IN ('FIXED','VARIABLE') AND (paymentStatus IS NULL OR TRIM(paymentStatus) = '')");
        }
    };

    /** Existing templates used to be copied into every cutoff. Keep that
     * behavior when upgrading by defaulting their schedule to both cutoffs. */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE bill ADD COLUMN recurrenceSchedule TEXT NOT NULL DEFAULT 'BOTH_CUTOFFS'");
        }
    };

    public static final String DATABASE_NAME = "cutoff.db";

    public abstract BillDao billDao();

    public abstract SalaryDao salaryDao();

    public abstract CutoffDao cutoffDao();
}
