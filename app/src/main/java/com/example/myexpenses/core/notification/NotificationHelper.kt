package com.example.myexpenses.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri

const val CHANNEL_REMINDER = "expense_reminder"
const val CHANNEL_SYNC = "expense_sync"

object NotificationHelper {

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily expense logging reminders"
            enableVibration(true)
        }

        val syncChannel = NotificationChannel(
            CHANNEL_SYNC,
            "SMS Sync",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background SMS scanning notifications"
        }

        nm.createNotificationChannel(reminderChannel)
        nm.createNotificationChannel(syncChannel)
    }

    /**
     * Shows the daily-reminder notification. Tapping the body opens the home
     * screen via deep link; tapping the action goes straight to the Add
     * Transaction screen so the user is one click from logging.
     */
    fun showReminderNotification(context: Context, label: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tap body → home (the recently-added screen surfaces context)
        val homeIntent = Intent(
            Intent.ACTION_VIEW,
            "expensemanager://home".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val homePi = PendingIntent.getActivity(
            context, 0, homeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tap "Add Expense" action → opens Add Transaction screen via deep link
        val addIntent = Intent(
            Intent.ACTION_VIEW,
            "expensemanager://home/detail/new".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val addPi = PendingIntent.getActivity(
            context, 1, addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💰 Time to log expenses!")
            .setContentText("Don't forget to track your spending for today")
            .setColor(0xFF00E5CC.toInt())
            .setAutoCancel(true)
            .setContentIntent(homePi)
            .addAction(android.R.drawable.ic_input_add, "Add Expense", addPi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(label.hashCode(), notification)
    }
}
