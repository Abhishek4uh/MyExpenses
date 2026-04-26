package com.example.myexpenses.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.common.UserPreferences
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.data.TransactionRepository
import com.example.myexpenses.core.notification.ReminderScheduler
import com.example.myexpenses.core.sms.SmsSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryStatRow(
    val category: ExpenseCategory,
    val type: TransactionType,
    val transactionCount: Int,
    val totalAmount: Double,
)

/**
 * Set of categories that count as INCOME. Mirrors the income filter used
 * in HomeDetailScreen's chip row + AddTransactionViewModel.
 */
private val INCOME_CATEGORIES: Set<ExpenseCategory> = setOf(
    ExpenseCategory.SALARY,
    ExpenseCategory.FREELANCE,
    ExpenseCategory.POCKET_MONEY,
    ExpenseCategory.OTHER_INCOME,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val smsSyncService: SmsSyncService,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository
        .getUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    /**
     * One row per ExpenseCategory enum value with its lifetime totals.
     * Order matches the enum declaration so the UI section grouping is
     * deterministic. Categories with 0 transactions still appear so the
     * user can see the full available palette.
     */
    val categoryStats: StateFlow<List<CategoryStatRow>> = transactionRepository
        .getAllTransactions()
        .map { all ->
            ExpenseCategory.entries.map { cat ->
                val txs = all.filter { it.category == cat }
                CategoryStatRow(
                    category = cat,
                    type = if (cat in INCOME_CATEGORIES) TransactionType.INCOME
                           else TransactionType.EXPENSE,
                    transactionCount = txs.size,
                    totalAmount = txs.sumOf { it.amount },
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun toggleSmsReader(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSmsReaderEnabled(enabled)
            if (enabled) {
                // Backfill: scan inbox for any historical bank/UPI SMS that arrived
                // before the toggle was on. Runtime receiver handles future SMS.
                smsSyncService.syncInbox()
            }
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateRemindersEnabled(enabled)
            // Persist first, then arm/disarm the AlarmManager schedules.
            // ReminderReceiver re-reads the pref on each fire so we never
            // notify after the user disabled — even races are safe.
            if (enabled) reminderScheduler.enable()
            else reminderScheduler.disable()
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateName(name.trim())
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            transactionRepository.deleteAllTransactions()
        }
    }
}
