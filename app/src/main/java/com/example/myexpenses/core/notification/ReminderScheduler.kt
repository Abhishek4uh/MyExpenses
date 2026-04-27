package com.example.myexpenses.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ReminderScheduler"

/**
 * Owns scheduling of the daily reminder notifications.
 *
 * Why AlarmManager (not WorkManager): WorkManager's PeriodicWorkRequest
 * has a 15-minute minimum interval and Doze can delay it arbitrarily —
 * we need exact 12:00 AM and 12:00 PM firing. AlarmManager.setExactAndAllowWhileIdle
 * is the documented best practice for time-of-day notifications.
 */
@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager: AlarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun enable() {
        Timber.tag(TAG).d("enable: scheduling reminders")
        scheduleNext(Slot.MIDNIGHT)
        scheduleNext(Slot.NOON)
    }

    fun disable() {
        Timber.tag(TAG).d("disable: cancelling reminders")
        Slot.entries.forEach { slot ->
            alarmManager.cancel(pendingIntentFor(slot))
        }
    }

    fun scheduleNext(slot: Slot) {
        val triggerAt = slot.nextTriggerMillis()
        val pending = pendingIntentFor(slot)

        // Android 12+ requires exact alarm permission for setExactAndAllowWhileIdle.
        // We use USE_EXACT_ALARM in manifest which is granted by default on many devices.
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pending
                )
                Timber.tag(TAG).d("scheduled EXACT ${slot.name} at $triggerAt")
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pending
                )
                Timber.tag(TAG).d("scheduled INEXACT ${slot.name} at $triggerAt")
            }
        } catch (se: SecurityException) {
            Timber.tag(TAG).w(se, "SecurityException while scheduling exact alarm")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pending
            )
        }
    }

    private fun pendingIntentFor(slot: Slot): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
            putExtra(ReminderReceiver.EXTRA_SLOT, slot.name)
        }
        return PendingIntent.getBroadcast(
            context,
            slot.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    enum class Slot(val hour: Int, val minute: Int, val label: String, val requestCode: Int) {
        MIDNIGHT(0, 0, "Midnight Check-in", 1001),
        NOON(12, 0, "Afternoon Check-in", 1002);

        fun nextTriggerMillis(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    companion object {
        const val ACTION_FIRE_REMINDER = "com.example.myexpenses.ACTION_FIRE_REMINDER"
    }
}
