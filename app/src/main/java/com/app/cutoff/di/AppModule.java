package com.app.cutoff.di;

import android.content.Context;

import androidx.room.Room;

import com.app.cutoff.data.database.AppDatabase;
import com.app.cutoff.data.database.dao.BillDao;
import com.app.cutoff.data.database.dao.CutoffDao;
import com.app.cutoff.data.database.dao.SalaryDao;
import com.app.cutoff.data.database.dao.PlannedItemDao;
import com.app.cutoff.data.database.dao.BudgetProjectDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Single Hilt module for the whole app, per the agreed structure — one
 * place to look for how the DB, DAOs, and preference wrappers are wired up.
 * Repositories/use cases are constructor-injected directly via @Inject and
 * don't need explicit @Provides methods here.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, AppDatabase.DATABASE_NAME)
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5,
                        AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7)
                .build();
    }

    @Provides
    @Singleton
    public BillDao provideBillDao(AppDatabase db) {
        return db.billDao();
    }

    @Provides
    @Singleton
    public SalaryDao provideSalaryDao(AppDatabase db) {
        return db.salaryDao();
    }

    @Provides
    @Singleton
    public CutoffDao provideCutoffDao(AppDatabase db) {
        return db.cutoffDao();
    }
    @Provides @Singleton public PlannedItemDao providePlannedItemDao(AppDatabase db) { return db.plannedItemDao(); }
    @Provides @Singleton public BudgetProjectDao provideBudgetProjectDao(AppDatabase db) { return db.budgetProjectDao(); }

    @Provides
    @Singleton
    public Context provideContext(@ApplicationContext Context context) {
        return context;
    }
}
