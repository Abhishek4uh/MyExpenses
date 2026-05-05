package com.example.myexpenses.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myexpenses.core.common.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>) : PreferencesRepository {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val SMS_READER_ENABLED = booleanPreferencesKey("sms_reader_enabled")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NAME = stringPreferencesKey("user_name")
        val REGISTRATION_EPOCH_MS = longPreferencesKey("registration_epoch_ms")
        val INSIGHTS_RANGE = stringPreferencesKey("insights_range")
        val INSIGHTS_CUSTOM_START = longPreferencesKey("insights_custom_start")
        val INSIGHTS_CUSTOM_END = longPreferencesKey("insights_custom_end")
    }

    override fun getUserPreferences(): Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            UserPreferences(
                isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
                isSmsReaderEnabled = prefs[Keys.SMS_READER_ENABLED] ?: false,
                isRemindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
                onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
                name = prefs[Keys.NAME] ?: "",
            )
        }

    override suspend fun updateDarkTheme(isDark: Boolean) {
        dataStore.edit { it[Keys.IS_DARK_THEME] = isDark }
    }

    override suspend fun updateSmsReaderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SMS_READER_ENABLED] = enabled }
    }

    override suspend fun updateRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    override suspend fun updateName(name: String) {
        dataStore.edit { it[Keys.NAME] = name }
    }

    override suspend fun setOnboardingComplete() {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = true
            // Write-once: never shift the window on subsequent calls
            if (prefs[Keys.REGISTRATION_EPOCH_MS] == null) {
                prefs[Keys.REGISTRATION_EPOCH_MS] = System.currentTimeMillis()
            }
        }
    }

    override fun getRegistrationEpochMs(): Flow<Long> =
        dataStore.data.map { prefs -> prefs[Keys.REGISTRATION_EPOCH_MS] ?: 0L }

    override fun getInsightsRange(): Flow<Triple<String, Long?, Long?>> =
        dataStore.data.map { prefs ->
            Triple(
                prefs[Keys.INSIGHTS_RANGE] ?: "month",
                prefs[Keys.INSIGHTS_CUSTOM_START],
                prefs[Keys.INSIGHTS_CUSTOM_END],
            )
        }

    override suspend fun updateInsightsRange(
        rangeKey: String,
        customStartEpochDay: Long?,
        customEndEpochDay: Long?) {
        dataStore.edit { prefs ->
            prefs[Keys.INSIGHTS_RANGE] = rangeKey
            if (customStartEpochDay != null) {
                prefs[Keys.INSIGHTS_CUSTOM_START] = customStartEpochDay
            }
            else {
                prefs.remove(Keys.INSIGHTS_CUSTOM_START)
            }
            if (customEndEpochDay != null) {
                prefs[Keys.INSIGHTS_CUSTOM_END] = customEndEpochDay
            }
            else {
                prefs.remove(Keys.INSIGHTS_CUSTOM_END)
            }
        }
    }
}
