package com.example.myexpenses.feature.home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.DashboardStats
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.data.TransactionRepository
import com.example.myexpenses.core.domain.DeleteTransactionUseCase
import com.example.myexpenses.core.domain.GetDashboardStatsUseCase
import com.example.myexpenses.core.domain.GetRecentTransactionsUseCase
import com.example.myexpenses.core.domain.GetTransactionByIdUseCase
import com.example.myexpenses.core.sms.SmsSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "HomeViewModel"

enum class DashboardPeriod(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val stats: DashboardStats,
        val recentTransactions: List<Transaction>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val smsSyncService: SmsSyncService,
    private val preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _period = MutableStateFlow(DashboardPeriod.MONTH)
    val period: StateFlow<DashboardPeriod> = _period.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val userName: StateFlow<String> = preferencesRepository
        .getUserPreferences()
        .map { it.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val isSmsEnabled: StateFlow<Boolean> = preferencesRepository
        .getUserPreferences()
        .map { it.isSmsReaderEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val uiState: StateFlow<HomeUiState> = _period
        .flatMapLatest { period ->
            combine(
                statsFlowFor(period),
                getRecentTransactionsUseCase()
            ) { stats, transactions ->
                HomeUiState.Success(
                    stats = stats,
                    recentTransactions = transactions.take(10)
                ) as HomeUiState
            }.catch { e ->
                emit(HomeUiState.Error(e.message ?: "Something went wrong"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    init {
        // Auto-sync historical SMS on app open if user has already enabled SMS sync.
        // Without this, transactions only flow in for NEW SMS via the receiver —
        // users opening the app expecting a sync would see nothing.
        viewModelScope.launch {
            val enabled = preferencesRepository.getUserPreferences().first().isSmsReaderEnabled
            Log.d(TAG, "init: isSmsReaderEnabled=$enabled — auto-sync ${if (enabled) "starting" else "skipped"}")
            if (enabled) {
                _isSyncing.value = true
                val n = smsSyncService.syncInbox()
                Log.d(TAG, "init: auto-sync complete, inserted=$n")
                _isSyncing.value = false
            }
        }
    }

    fun selectPeriod(period: DashboardPeriod) {
        _period.value = period
    }

    fun getTransactionById(id: String): Flow<Transaction?> =
        getTransactionByIdUseCase(id)

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            deleteTransactionUseCase(id)
        }
    }

    fun syncSmsTransactions() {
        if (_isSyncing.value) return
        Log.d(TAG, "syncSmsTransactions: manual sync requested")
        viewModelScope.launch {
            _isSyncing.value = true
            val n = smsSyncService.syncInbox()
            Log.d(TAG, "syncSmsTransactions: complete, inserted=$n")
            _isSyncing.value = false
        }
    }

    fun toggleSmsReader(enabled: Boolean) {
        Log.d(TAG, "toggleSmsReader: enabled=$enabled")
        viewModelScope.launch {
            preferencesRepository.updateSmsReaderEnabled(enabled)
            if (enabled) {
                _isSyncing.value = true
                val n = smsSyncService.syncInbox()
                Log.d(TAG, "toggleSmsReader: post-enable sync inserted=$n")
                _isSyncing.value = false
            }
        }
    }

    fun updateTransaction(updated: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(updated)
        }
    }

    private fun statsFlowFor(period: DashboardPeriod): Flow<DashboardStats> = when (period) {
        DashboardPeriod.TODAY -> getDashboardStatsUseCase.forToday()
        DashboardPeriod.WEEK -> getDashboardStatsUseCase.forCurrentWeek()
        DashboardPeriod.MONTH -> getDashboardStatsUseCase.forCurrentMonth()
        DashboardPeriod.YEAR -> getDashboardStatsUseCase.forYear(LocalDate.now().year)
    }
}
