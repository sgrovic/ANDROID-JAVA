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
import com.app.cutoff.data.database.entity.PlannedItemEntity;
import com.app.cutoff.data.database.dao.PlannedItemDao;
import com.app.cutoff.data.database.dao.BudgetProjectDao;
import com.app.cutoff.data.database.entity.BudgetProjectEntity;

@Database(
        entities = {BillEntity.class, CutoffEntity.class, SalaryEntity.class,
                PlannedItemEntity.class, BudgetProjectEntity.class},
        version = 7,
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
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS planned_item (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, totalAmount REAL NOT NULL, monthsToPay INTEGER NOT NULL, applyFirstCutoff INTEGER NOT NULL, applySecondCutoff INTEGER NOT NULL)");
        }
    };
    /** Adds independent projects and places legacy planned items in one default project. */
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS budget_project (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, targetAmount REAL NOT NULL, savedAmount REAL NOT NULL, createdAtEpochDay INTEGER NOT NULL)");
            db.execSQL("INSERT INTO budget_project (id, name, targetAmount, savedAmount, createdAtEpochDay) "
                    + "SELECT 1, 'My Project', CASE WHEN SUM(totalAmount) > 0 THEN SUM(totalAmount) ELSE 1 END, 0, 0 "
                    + "FROM planned_item HAVING COUNT(*) > 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS planned_item_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, totalAmount REAL NOT NULL, monthsToPay INTEGER NOT NULL, applyFirstCutoff INTEGER NOT NULL, applySecondCutoff INTEGER NOT NULL, projectId INTEGER NOT NULL, FOREIGN KEY(projectId) REFERENCES budget_project(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("INSERT INTO planned_item_new (id, name, totalAmount, monthsToPay, applyFirstCutoff, applySecondCutoff, projectId) SELECT id, name, totalAmount, monthsToPay, applyFirstCutoff, applySecondCutoff, 1 FROM planned_item");
            db.execSQL("DROP TABLE planned_item");
            db.execSQL("ALTER TABLE planned_item_new RENAME TO planned_item");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_planned_item_projectId ON planned_item(projectId)");
        }
    };
    /** Stores the selected contribution split. Existing rows retain their old behavior. */
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE planned_item ADD COLUMN firstCutoffPercent INTEGER NOT NULL DEFAULT 50");
            db.execSQL("UPDATE planned_item SET firstCutoffPercent = 100 WHERE applyFirstCutoff = 1 AND applySecondCutoff = 0");
            db.execSQL("UPDATE planned_item SET firstCutoffPercent = 0 WHERE applyFirstCutoff = 0 AND applySecondCutoff = 1");
        }
    };

    public static final String DATABASE_NAME = "cutoff.db";

    public abstract BillDao billDao();

    public abstract SalaryDao salaryDao();

    public abstract CutoffDao cutoffDao();
    public abstract PlannedItemDao plannedItemDao();
    public abstract BudgetProjectDao budgetProjectDao();
}
