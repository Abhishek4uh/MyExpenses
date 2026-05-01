package com.example.myexpenses.feature.streak.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.StreakData
import com.example.myexpenses.core.domain.GetStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class StreakViewModel @Inject constructor(
    getStreakUseCase: GetStreakUseCase,
) : ViewModel() {

    val streakData: StateFlow<StreakData> = getStreakUseCase()
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StreakData(0, emptySet(), LocalDate.now()),
        )

    private val _displayMonth = MutableStateFlow(YearMonth.now())
    val displayMonth: StateFlow<YearMonth> = _displayMonth.asStateFlow()

    fun goToPreviousMonth() {
        _displayMonth.update { it.minusMonths(1) }
    }

    fun goToNextMonth() {
        _displayMonth.update { m -> if (m < YearMonth.now()) m.plusMonths(1) else m }
    }
}
