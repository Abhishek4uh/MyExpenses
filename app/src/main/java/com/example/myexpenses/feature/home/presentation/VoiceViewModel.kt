package com.example.myexpenses.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.common.EntrySource
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.domain.AddTransactionUseCase
import com.example.myexpenses.core.voice.ParsedVoiceIntent
import com.example.myexpenses.core.voice.VoiceInputHandler
import com.example.myexpenses.core.voice.VoiceNLPParser
import com.example.myexpenses.core.voice.VoiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VoiceUiState {
    data object Idle : VoiceUiState
    data object Listening : VoiceUiState
    data class Partial(val text: String) : VoiceUiState
    data class Recognized(val rawText: String, val parsed: ParsedVoiceIntent) : VoiceUiState
    data class SpeechError(val message: String) : VoiceUiState
    data object Saved : VoiceUiState
}

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val voiceInputHandler: VoiceInputHandler,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    private var listeningJob: Job? = null

    fun startListening() {
        if (_state.value is VoiceUiState.Listening) return
        listeningJob?.cancel()
        listeningJob = viewModelScope.launch(Dispatchers.Main) {
            voiceInputHandler.startListening().collect { voiceState ->
                when (voiceState) {
                    VoiceState.Idle -> _state.value = VoiceUiState.Idle
                    VoiceState.Listening -> _state.value = VoiceUiState.Listening
                    is VoiceState.Partial -> _state.value = VoiceUiState.Partial(voiceState.text)
                    is VoiceState.Result -> {
                        val parsed = VoiceNLPParser.parse(voiceState.text)
                        _state.value = VoiceUiState.Recognized(voiceState.text, parsed)
                    }
                    is VoiceState.Error -> _state.value = VoiceUiState.SpeechError(voiceState.message)
                }
            }
        }
    }

    fun saveVoiceTransaction(onSaved: () -> Unit) {
        val current = _state.value as? VoiceUiState.Recognized ?: return
        val parsed = current.parsed
        val amount = parsed.amount ?: run {
            _state.value = VoiceUiState.SpeechError("Couldn't detect an amount — try again")
            return
        }
        viewModelScope.launch {
            addTransactionUseCase(
                Transaction(
                    amount = amount,
                    type = parsed.type ?: TransactionType.EXPENSE,
                    category = parsed.category ?: ExpenseCategory.MISCELLANEOUS,
                    note = parsed.note ?: "",
                    source = EntrySource.VOICE,
                    dateTime = parsed.dateTime
                )
            )
            _state.value = VoiceUiState.Saved
            onSaved()
        }
    }

    /**
     * Tells the underlying recognizer to finalize with whatever it has
     * captured so far. Used when the user releases a press-and-hold mic
     * button. The flow keeps running so it can still emit Result/Error.
     */
    fun stopListening() {
        if (_state.value is VoiceUiState.Listening || _state.value is VoiceUiState.Partial) {
            voiceInputHandler.stopListening()
        }
    }

    fun reset() {
        listeningJob?.cancel()
        _state.value = VoiceUiState.Idle
    }

    override fun onCleared() {
        listeningJob?.cancel()
    }
}
