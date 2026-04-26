package com.example.myexpenses.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myexpenses.core.data.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "ReminderReceiver"

/**
 * Receives:
 *  • ACTION_BOOT_COMPLETED  → re-arms both daily alarms if user has reminders on
 *  • ACTION_FIRE_REMINDER   → posts the notification and re-schedules for the
 *                             next day (alarms are one-shot by design)
 *
 * Note: the BOOT_COMPLETED intent filter is declared in the manifest. The
 * Hilt-injected [PreferencesRepository] (DataStore) is the single source of
 * truth for the on/off state — no SharedPreferences fallback.
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var scheduler: ReminderScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> handleBoot()
            ReminderScheduler.ACTION_FIRE_REMINDER -> handleFire(context, intent)
        }
    }

    private fun handleBoot() {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val enabled = preferencesRepository.getUserPreferences().first().isRemindersEnabled
                Timber.tag(TAG).d("boot: isRemindersEnabled=$enabled")
                if (enabled) scheduler.enable()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleFire(context: Context, intent: Intent) {
        val slotName = intent.getStringExtra(EXTRA_SLOT) ?: return
        val slot = runCatching { ReminderScheduler.Slot.valueOf(slotName) }.getOrNull() ?: return
        Timber.tag(TAG).d("fire: slot=$slotName")

        // Post the notification first (cheap), then reschedule for tomorrow.
        NotificationHelper.showReminderNotification(context, slot.label)

        // Self-reschedule: alarms with setExactAndAllowWhileIdle are one-shot.
        // We honor the on/off pref so a stale alarm (toggle off mid-flight)
        // doesn't keep re-scheduling itself indefinitely.
        val pendingResult = goAsync()
        scope.launch {
            try {
                val enabled = preferencesRepository.getUserPreferences().first().isRemindersEnabled
                if (enabled) scheduler.scheduleNext(slot)
            }
            finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_SLOT = "EXTRA_SLOT"
    }
}
