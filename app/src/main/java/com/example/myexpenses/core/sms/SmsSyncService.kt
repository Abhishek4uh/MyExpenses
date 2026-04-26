package com.example.myexpenses.core.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.myexpenses.core.common.EntrySource
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.data.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

private const val TAG = "SmsSyncService"

/**
 * Reads the device SMS inbox and saves any detected financial transactions.
 *
 * Uses "sms_<sms_id>" as the transaction ID so re-syncing is idempotent —
 * Room's REPLACE strategy will overwrite the same row without creating duplicates.
 */
@Singleton
class SmsSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TransactionRepository) {
    /**
     * Scans the last [lookbackDays] days of inbox SMS, parses financial ones,
     * and upserts them as confirmed transactions.
     * Returns the number of transactions inserted or updated.
     */
    suspend fun syncInbox(lookbackDays: Int = 90): Int {
        if (!hasPermission()) {
            Timber.tag(TAG).w("syncInbox: READ_SMS permission NOT granted")
            return 0
        }
        Timber.tag(TAG).d("syncInbox: starting (lookback=$lookbackDays days)")

        val since = System.currentTimeMillis() - lookbackDays.toLong() * 24 * 60 * 60 * 1000
        var scanned = 0
        var inserted = 0

        val cursor = context.contentResolver.query(
            "content://sms/inbox".toUri(),
            arrayOf("_id", "address", "body", "date"),
            "date > ?",
            arrayOf(since.toString()),
            "date DESC"
        )
        if (cursor == null) {
            Timber.tag(TAG).w("syncInbox: cursor was null — content://sms/inbox query failed")
            return 0
        }

        cursor.use {
            val idxId   = it.getColumnIndex("_id")
            val idxAddr = it.getColumnIndex("address")
            val idxBody = it.getColumnIndex("body")
            val idxDate = it.getColumnIndex("date")

            while (it.moveToNext()) {
                scanned++
                val smsId   = it.getLong(idxId)
                val address = it.getString(idxAddr) ?: continue
                val body    = it.getString(idxBody) ?: continue
                val dateMs  = it.getLong(idxDate)

                val parsed = SmsBankingParser.parse(body, address)
                if (parsed == null) continue
                val amount = parsed.parsedAmount ?: continue

                val dateTime = Instant.ofEpochMilli(dateMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                repository.insertTransaction(
                    Transaction(
                        id = "sms_$smsId",
                        amount = amount,
                        type = parsed.parsedType ?: TransactionType.EXPENSE,
                        category = parsed.suggestedCategory ?: ExpenseCategory.MISCELLANEOUS,
                        note = address,
                        source = EntrySource.SMS,
                        dateTime = dateTime,
                        isConfirmed = true
                    )
                )
                inserted++
            }
        }
        Timber.tag(TAG).d("syncInbox: scanned=$scanned inserted=$inserted")
        return inserted
    }

    private fun hasPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED
}
