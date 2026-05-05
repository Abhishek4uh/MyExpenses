package com.example.myexpenses.app

import android.app.Application
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.notification.NotificationHelper
import com.example.myexpenses.core.notification.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application() {

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        NotificationHelper.createChannels(this)
        reArmRemindersIfEnabled()
    }

    // Re-arm alarms every time the app process starts. AlarmManager alarms survive
    // normal app restarts, but are cleared when the user force-stops the app. This
    // ensures daily reminders come back automatically the next time the app is opened.
    private fun reArmRemindersIfEnabled() {
        appScope.launch {
            try {
                val prefs = preferencesRepository.getUserPreferences().first()
                if (prefs.isRemindersEnabled) {
                    reminderScheduler.enable()
                    Timber.tag("BaseApp").d("re-armed reminder alarms on startup")
                }
            }
            catch (e: Exception) {
                Timber.tag("BaseApp").e(e, "failed to re-arm reminders")
            }
        }
    }
}