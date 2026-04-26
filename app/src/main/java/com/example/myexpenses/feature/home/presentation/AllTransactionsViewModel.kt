package com.example.myexpenses.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.data.TransactionRepository
import com.example.myexpenses.core.domain.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

data class TransactionsTabState(
    val transactions: List<Transaction> = emptyList(),
    val totalLoaded: Int = 0,
    val totalAvailable: Int = 0,
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
) {
    val hasMore: Boolean get() = totalLoaded < totalAvailable
}

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    @Suppress("unused") private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    // Independent page counts so the two tabs scroll separately.
    private val _expensePages = MutableStateFlow(1)
    private val _incomePages = MutableStateFlow(1)
    private val _expenseLoadingMore = MutableStateFlow(false)
    private val _incomeLoadingMore = MutableStateFlow(false)
    private val _isInitialLoading = MutableStateFlow(true)

    private val allTransactions: Flow<List<Transaction>> = transactionRepository.getAllTransactions()

    val expenseState: StateFlow<TransactionsTabState> = combine(
        allTransactions, _expensePages, _expenseLoadingMore, _isInitialLoading,
    ) { all, pages, loadingMore, initial ->
        val list = all.filter { it.type == TransactionType.EXPENSE }
        TransactionsTabState(
            transactions = list.take(pages * PAGE_SIZE),
            totalLoaded = minOf(pages * PAGE_SIZE, list.size),
            totalAvailable = list.size,
            isInitialLoading = initial,
            isLoadingMore = loadingMore,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsTabState())

    val incomeState: StateFlow<TransactionsTabState> = combine(
        allTransactions, _incomePages, _incomeLoadingMore, _isInitialLoading,
    ) { all, pages, loadingMore, initial ->
        val list = all.filter { it.type == TransactionType.INCOME }
        TransactionsTabState(
            transactions = list.take(pages * PAGE_SIZE),
            totalLoaded = minOf(pages * PAGE_SIZE, list.size),
            totalAvailable = list.size,
            isInitialLoading = initial,
            isLoadingMore = loadingMore,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsTabState())

    init {
        // Brief shimmer window so the loading state is perceptible even when
        // Room responds in <50ms. Without this the UI feels jumpy.
        viewModelScope.launch {
            delay(450)
            _isInitialLoading.value = false
        }
    }

    fun loadMoreExpense() = loadMore(_expensePages, _expenseLoadingMore, expenseState.value)
    fun loadMoreIncome()  = loadMore(_incomePages, _incomeLoadingMore, incomeState.value)

    private fun loadMore(
        pages: MutableStateFlow<Int>,
        loading: MutableStateFlow<Boolean>,
        state: TransactionsTabState,
    ) {
        if (loading.value) return
        if (!state.hasMore) return
        viewModelScope.launch {
            loading.value = true
            // Brief delay so the bottom spinner is visible — pagination is
            // really just slicing in-memory data, but the spinner gives a
            // sense of progress.
            delay(300)
            pages.value += 1
            loading.value = false
        }
    }
}
