package com.example.myexpenses.core.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.myexpenses.core.common.EntrySource
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.common.Transaction
import com.example.myexpenses.core.common.TransactionType
import com.example.myexpenses.core.data.PreferencesRepository
import com.example.myexpenses.core.data.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

// ─── SMS Receiver ─────────────────────────────────────────────────────────────

private const val TAG = "SmsReceiver"

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TransactionRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).d("onReceive action=${intent.action}")
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        Timber.tag(TAG).d("Got ${messages.size} message(s) from intent")
        val pendingResult = goAsync()

        scope.launch {
            try {
                val prefs = preferencesRepository.getUserPreferences().first()
                Timber.tag(TAG).d("isSmsReaderEnabled=${prefs.isSmsReaderEnabled}")
                if (!prefs.isSmsReaderEnabled) return@launch

                val registrationEpochMs = preferencesRepository.getRegistrationEpochMs().first()

                messages.forEach { sms ->
                    // Drop any SMS that predates app registration (delayed carrier delivery, etc.)
                    if (sms.timestampMillis < registrationEpochMs) {
                        Timber.tag(TAG).d("Skipped (before registration) ts=${sms.timestampMillis}")
                        return@forEach
                    }

                    val sender = sms.originatingAddress ?: "Unknown"
                    val body = sms.messageBody ?: ""
                    Timber.tag(TAG).d("Processing SMS from=$sender bodyLen=${body.length}")
                    val parsed = SmsBankingParser.parse(body, sender)
                    if (parsed == null) {
                        Timber.tag(TAG).d("Skipped (no pattern match) from=$sender")
                        return@forEach
                    }
                    val amount = parsed.parsedAmount ?: return@forEach
                    Timber.tag(TAG)
                        .d("Inserting txn amount=$amount type=${parsed.parsedType} from=$sender")

                    val dateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(sms.timestampMillis),
                        java.time.ZoneId.systemDefault(),
                    )
                    repository.insertTransaction(
                        Transaction(
                            amount = amount,
                            type = parsed.parsedType ?: TransactionType.EXPENSE,
                            category = parsed.suggestedCategory ?: ExpenseCategory.MISCELLANEOUS,
                            note = sender,
                            source = EntrySource.SMS,
                            dateTime = dateTime,
                            isConfirmed = true
                        )
                    )
                }
            } catch (t: Throwable) {
                Timber.tag(TAG).e(t, "Error processing SMS")
            }
            finally {
                pendingResult.finish()
            }
        }
    }
}

// ─── Banking SMS Parser ───────────────────────────────────────────────────────

object SmsBankingParser {

    // Known bank/UPI sender codes. SMS senders in India use 6-letter DLT codes
    // prefixed with a 2-letter operator id (e.g. "AD-HDFCBK", "JM-SBIPSG").
    // We strip the prefix and match the core code against this allow-list.
    private val BANK_SENDER_CODES = setOf(
        // Public sector banks
        "SBIINB", "SBIPSG", "SBIBNK", "SBIATM", "SBICRD", "SBICARD", "SBIUPI",
        "PNBSMS", "PNBBNK", "PNBINB", "BOBSMS", "BOBBNK", "BOBTXN", "BOIIND", "BOIBNK",
        "CANBNK", "CANARA", "UCOBNK", "UNIONB", "UBOFIN", "IOBCHN", "INDBNK", "ANDHRA",
        "CENTBK", "CBOIBK", "CORPBK", "DENABNK", "OBCBNK", "PSBSMS", "VIJBNK", "SYNBNK",
        // Private sector banks
        "HDFCBK", "HDFCCC", "HDFCBN",
        "ICICIB", "ICICI", "ICICIBANK", "ICICIT",
        "AXISBK", "AXISCC", "AXISBN",
        "KOTAKB", "KOTAKM", "KOTAK",
        "INDUSB", "INDIND",
        "YESBNK", "YESBANK",
        "FEDBNK", "FEDERAL", "RBLBNK", "RBL",
        "IDBIBANK", "IDBIBN", "IDFCFB", "IDFC",
        "DCBBNK", "DCBNK", "BANDHAN", "KARURB", "KVBANK",
        "CITIBN", "CITIBK", "CITI",
        "HSBCBK", "HSBC", "STANCH", "DEUTCH",
        // UPI / wallets
        "BHIMUPI", "UPIINB", "BHIM",
        "PAYTMB", "PAYTMP", "PAYTM",
        "GPAYIN", "GPAY", "GOOGLEPAY",
        "PHONPE", "PHONEPE", "AMZNPAY", "MOBKWIK",
        "FRECHARGE", "OLAFIN"
    )

    // Currency prefix that must precede or follow an amount
    // Matches: "Rs", "Rs.", "INR", "₹", "rs"
    private const val CURRENCY = """(?:rs\.?|inr|₹|inr\.?)"""

    // Amount: digits with optional commas and 0-2 decimals
    private const val AMOUNT = """([\d,]+(?:\.\d{1,2})?)"""

    // ── Debit / Expense keywords ──────────────────────────────────────────────
    // The amount can be in any position relative to these keywords.
    // We use two-direction matching to be robust against word order variations.
    private val DEBIT_KEYWORDS = listOf(
        "debited", "debit", "withdrawn", "deducted", "spent", "paid", "sent",
        "transferred", "purchase", "withdraw", "charge", "charged", "made"
    )

    // ── Credit / Income keywords (used only to SKIP these messages) ───────────
    // We deliberately do not auto-classify income from SMS — heuristics produce
    // too many false positives (refund offers, "credit limit" mentions, etc).
    // Users add income manually; AI-based income detection is planned later.
    // These keywords let us recognise a likely-income SMS so we can drop it
    // instead of misclassifying it as an expense.
    private val CREDIT_KEYWORDS = listOf(
        "credited", "deposited", "refund", "refunded", "cashback received",
        "salary credited"
    )

    // Captures amount whether it appears before or after the currency token.
    // Tries: "<currency> <amount>" then falls back to "<amount> <currency>".
    private val AMOUNT_REGEX_CURRENCY_FIRST =
        Regex("""$CURRENCY\.?\s*$AMOUNT""", RegexOption.IGNORE_CASE)
    private val AMOUNT_REGEX_AMOUNT_FIRST =
        Regex("""$AMOUNT\s*$CURRENCY""", RegexOption.IGNORE_CASE)

    // OTP detection — most banks send "OTP is 123456" type messages we must skip
    private val OTP_PATTERN = Regex("""\b(otp|one\s*time\s*password|verification\s*code)\b""", RegexOption.IGNORE_CASE)

    private val MERCHANT_CATEGORY_MAP = mapOf(
        setOf("swiggy", "zomato", "dunzo") to ExpenseCategory.ONLINE_FOOD,
        setOf("uber", "ola", "rapido", "metro", "irctc", "redbus") to ExpenseCategory.COMMUTE,
        setOf("netflix", "spotify", "amazon prime", "hotstar", "bookmyshow", "prime video") to ExpenseCategory.ENTERTAINMENT,
        setOf("hospital", "pharmacy", "medplus", "apollo", "1mg", "netmeds") to ExpenseCategory.HEALTHCARE,
        setOf("salary", "payroll", "stipend") to ExpenseCategory.SALARY,
        setOf("grocery", "groceries", "bigbasket", "blinkit", "zepto", "grofers", "dmart") to ExpenseCategory.GROCERY,
        setOf("amazon", "flipkart", "myntra", "ajio", "meesho", "nykaa") to ExpenseCategory.SHOPPING,
        setOf("zerodha", "groww", "kuvera", "upstox", "mutual fund", "sip") to ExpenseCategory.INVESTMENT
    )

    // Promotional / spam / phishing keywords — any presence disqualifies the
    // SMS regardless of sender or amount. Real bank transaction SMS never use
    // these phrases (transactions are factual, not persuasive).
    private val PROMOTIONAL_KEYWORDS = listOf(
        // Generic promos
        "offer", "discount", "%off", "% off", "cashback offer", "voucher", "coupon",
        "recharge", "subscribe", "subscription pack", "validity", "data pack",
        "lowest price", "deal", "buy now", "shop now", "limited period",
        // Loan / pre-approval phishing
        "loan offer", "pre-approved", "pre approved", "preapproved",
        "loan approved", "approved loan", "instant loan", "personal loan",
        "apply now", "apply for loan", "get loan", "credit card offer",
        "interest rate", "% interest", "%p.a", "% p.a", "%pa", "p.a.",
        "emi", "tenure",
        // Lottery / scam
        "congratulation", "congrats", "winner", "won by", "you have won",
        "lucky", "lucky draw", "prize", "claim now", "claim your", "free gift",
        "click here", "click below", "visit", "tap here",
        // Bank promos
        "fixed deposit offer", "investment opportunity", "earn interest",
        "open account", "free credit", "get cashback", "rewards await"
    )

    // Loan/scam often have "Rs.500000" or "5 lakh" without context — even though
    // they pass the currency check, they don't have a transactional verb in the
    // bank-statement sense. The promo guard above catches most. This regex
    // catches "Get Rs.5,00,000 loan" or "Avail Rs.10,00,000" patterns.
    private val LOAN_OFFER_PATTERN = Regex(
        """\b(get|avail|grab|secure|unlock)\b.*\b(loan|credit|funding|finance)\b""",
        RegexOption.IGNORE_CASE
    )

    fun isFinancialSender(address: String?): Boolean {
        if (address.isNullOrBlank()) return false
        val upper = address.uppercase()
        // Strip DLT operator prefix ("AD-", "JM-", "VM-", etc.) if present
        val core = if (upper.contains('-')) upper.substringAfter('-') else upper
        return BANK_SENDER_CODES.any { code -> core.contains(code) }
    }

    fun parse(body: String, sender: String): com.example.myexpenses.core.common.PendingSmsTransaction? {
        // 1. Sender must be a known bank / UPI service. This is the single most
        //    important filter — promo SMS, recharge offers, telecom alerts, and
        //    spam are all sent by senders that aren't on this list.
        if (!isFinancialSender(sender)) return null

        // 2. Skip OTP / verification messages — they contain numbers we'd misread
        if (OTP_PATTERN.containsMatchIn(body)) return null

        val lower = body.lowercase()

        // 3. Even if from a bank sender, banks send promotional SMS too
        //    (loan offers, credit-card offers, "congratulations" pre-approvals).
        //    Drop anything with promo / scam keywords.
        if (PROMOTIONAL_KEYWORDS.any { lower.contains(it) }) return null
        if (LOAN_OFFER_PATTERN.containsMatchIn(body)) return null

        // 4. Find a currency-anchored amount. Real bank SMS always have one.
        val amountStr = AMOUNT_REGEX_CURRENCY_FIRST.find(body)?.groupValues?.get(1)
            ?: AMOUNT_REGEX_AMOUNT_FIRST.find(body)?.groupValues?.get(1)
            ?: return null

        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: return null
        // Reject implausible amounts (0, negative, or larger than 10M)
        if (amount <= 0 || amount > 10_000_000) return null

        // 5. Income SMS are intentionally NOT auto-imported — too many false
        //    positives from refund alerts, credit-limit notices, etc. User
        //    adds income manually for now (AI-based detection planned later).
        val hasCredit = CREDIT_KEYWORDS.any { lower.contains(it) }
        val hasDebit = DEBIT_KEYWORDS.any { lower.contains(it) }
        if (hasCredit && !hasDebit) return null    // skip pure-income SMS
        if (!hasDebit) return null                 // skip non-transactional SMS

        val type = TransactionType.EXPENSE
        val suggestedCategory = guessCategory(lower, type)

        return com.example.myexpenses.core.common.PendingSmsTransaction(
            rawSmsBody = body,
            parsedAmount = amount,
            parsedType = type,
            suggestedCategory = suggestedCategory,
            senderName = sender
        )
    }

    private fun guessCategory(body: String, type: TransactionType): ExpenseCategory {
        // Currently only EXPENSE flows through — income detection is deferred.
        MERCHANT_CATEGORY_MAP.forEach { (keywords, category) ->
            if (keywords.any { body.contains(it) }) return category
        }
        return ExpenseCategory.MISCELLANEOUS
    }
}
