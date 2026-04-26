package com.example.myexpenses.feature.home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.EntrySource
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.domain.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    var amountText by mutableStateOf("")
        private set
    var selectedType by mutableStateOf(TransactionType.EXPENSE)
        private set
    var selectedCategory by mutableStateOf(ExpenseCategory.MISCELLANEOUS)
        private set
    var note by mutableStateOf("")
        private set

    val canSave: Boolean
        get() = amountText.toDoubleOrNull()?.let { it > 0 } == true

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("""^\d{0,10}(\.\d{0,2})?$"""))) {
            amountText = value
        }
    }

    fun onTypeChange(type: TransactionType) {
        selectedType = type
        // Reset category when switching type so income categories appear for INCOME
        selectedCategory = if (type == TransactionType.INCOME) ExpenseCategory.SALARY
        else ExpenseCategory.DINEIN
    }

    fun onCategoryChange(category: ExpenseCategory) { selectedCategory = category }
    fun onNoteChange(value: String) { note = value.take(120) }

    fun save(onSaved: () -> Unit) {
        val amount = amountText.toDoubleOrNull() ?: return
        viewModelScope.launch {
            addTransactionUseCase(
                Transaction(
                    amount = amount,
                    type = selectedType,
                    category = selectedCategory,
                    note = note.trim(),
                    source = EntrySource.MANUAL
                )
            )
            onSaved()
        }
    }
}
