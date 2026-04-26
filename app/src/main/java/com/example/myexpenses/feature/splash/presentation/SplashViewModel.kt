package com.example.myexpenses.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpenses.core.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SplashDestination { Loading, Onboarding, Main }

@HiltViewModel
class SplashViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val destination: StateFlow<SplashDestination> = preferencesRepository
        .getUserPreferences()
        .map { prefs ->
            if (prefs.onboardingComplete) SplashDestination.Main
            else SplashDestination.Onboarding
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SplashDestination.Loading
        )
}
