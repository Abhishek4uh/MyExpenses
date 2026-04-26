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
 *
 * Each alarm is one-shot. The receiver re-schedules itself for the next
 * day after firing. This is the standard pattern — `setRepeating` was
 * deprecated for inexactness, and `setExactAndAllowWhileIdle` is one-shot
 * by design.
 */
@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager: AlarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Turn reminders on. Schedules both 12 AM and 12 PM alarms for today/tomorrow. */
    fun enable() {
        Timber.tag(TAG).d("enable: scheduling 00:00 + 12:00 reminders")
        scheduleNext(Slot.MIDNIGHT)
        scheduleNext(Slot.NOON)
    }

    /** Turn reminders off. Cancels both pending alarms. */
    fun disable() {
        Timber.tag(TAG).d("disable: cancelling all reminders")
        Slot.entries.forEach { slot ->
            alarmManager.cancel(pendingIntentFor(slot))
        }
    }

    /**
     * Schedule the next firing of [slot]. If the slot's time has already
     * passed today, schedules for tomorrow.
     *
     * Use `setExactAndAllowWhileIdle` so the alarm fires through Doze mode.
     * Falls back to `setExact` on older devices that don't have the new API.
     */
    fun scheduleNext(slot: Slot) {
        val triggerAt = slot.nextTriggerMillis()
        val pending = pendingIntentFor(slot)

        // On Android 12+ exact alarms require the runtime permission. We
        // declared SCHEDULE_EXACT_ALARM in the manifest; check at runtime
        // before calling the exact API to avoid SecurityException on
        // devices where the user revoked it.
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
                Timber.tag(TAG).d("scheduled INEXACT ${slot.name} at $triggerAt (no exact perm)")
            }
        } catch (se: SecurityException) {
            // Race: permission revoked between check and call
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pending
            )
            Timber.tag(TAG).w(se, "SecurityException — fell back to inexact for ${slot.name}")
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
