package com.example.myexpenses.feature.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    val canProceed: Boolean
        get() = name.trim().length >= 2

    fun onNameChange(value: String) {
        name = value.take(50)
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.updateName(name.trim())
            preferencesRepository.setOnboardingComplete()
            onDone()
        }
    }
}
