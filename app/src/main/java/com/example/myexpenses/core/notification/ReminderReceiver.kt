package com.example.myexpenses.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myexpenses.core.data.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "ReminderReceiver"

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var scheduler: ReminderScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Timber.tag(TAG).d("onReceive: action=$action")
        
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                handleReArm()
            }
            ReminderScheduler.ACTION_FIRE_REMINDER -> {
                handleFire(context, intent)
            }
        }
    }

    private fun handleReArm() {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val prefs = preferencesRepository.getUserPreferences().firstOrNull()
                val enabled = prefs?.isRemindersEnabled ?: false
                Timber.tag(TAG).d("re-arm: isRemindersEnabled=$enabled")
                if (enabled) {
                    scheduler.enable()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error during re-arm")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleFire(context: Context, intent: Intent) {
        val slotName = intent.getStringExtra(EXTRA_SLOT) ?: return
        val slot = runCatching { ReminderScheduler.Slot.valueOf(slotName) }.getOrNull() ?: return
        Timber.tag(TAG).d("fire: slot=$slotName")

        // Post the notification
        NotificationHelper.showReminderNotification(context, slot.label)

        // Reschedule for tomorrow
        val pendingResult = goAsync()
        scope.launch {
            try {
                val prefs = preferencesRepository.getUserPreferences().firstOrNull()
                val enabled = prefs?.isRemindersEnabled ?: false
                if (enabled) {
                    scheduler.scheduleNext(slot)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error during rescheduling")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_SLOT = "EXTRA_SLOT"
    }
}
