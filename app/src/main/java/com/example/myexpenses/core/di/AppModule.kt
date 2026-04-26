package com.example.myexpenses.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.myexpenses.core.data.ExpenseDatabase
import com.example.myexpenses.core.data.PendingSmsDao
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.data.PreferencesRepositoryImpl
import com.example.myexpenses.core.data.TransactionDao
import com.example.myexpenses.core.data.TransactionRepository
import com.example.myexpenses.core.data.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ─── DataStore extension ──────────────────────────────────────────────────────
// Defined at file level per DataStore docs — one instance per process

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "expense_manager_prefs")

// ─── Database Module ──────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseDatabase =
        Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            ExpenseDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideTransactionDao(db: ExpenseDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun providePendingSmsDao(db: ExpenseDatabase): PendingSmsDao = db.pendingSmsDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}

// ─── Repository Bindings ──────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule{

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}
