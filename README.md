# 💸 MyExpenses

**A sophisticated, privacy-first expense tracker built with modern Android standards.**

MyExpenses isn't just another ledger—it's a financial companion designed with a "Liquid UI" philosophy. It combines deep automated tracking with a delightful, tactile user experience.

---

## ✨ Key Features

### 🤖 Intelligent SMS Sync
Stop manual entry. MyExpenses securely parses your bank and UPI SMS alerts locally on your device to automatically categorize and log your spending.
*   **Privacy First:** Your financial data never leaves your device.
*   **Historical Sync:** Pull in past transactions with a single tap.

### 🎭 "Liquid" User Interface
Experience high-fidelity motion design and glassmorphic elements:
*   **Animated Balance:** A dramatic "throw, bounce, and land" animation that makes every period switch feel alive.
*   **Segmented Insights:** Smoothly transition between Daily, Weekly, Monthly, and Yearly views.
*   **Adaptive Theme:** Deep dark mode with vibrant accent "tones" for different expense categories.

### 📊 Deep Analytics
Understand your habits through beautiful, interactive visualizations:
*   **Trend Analysis:** Two-line charts for comparing Income vs. Expenses over time.
*   **Spending Breakdown:** Category-wise heatmaps and percentage distributions.
*   **Contextual Insights:** An "Insight Engine" that surfaces spending anomalies and suggests optimizations.

### 🎙️ Voice-First Entry
Forgot to sync? Just tap the microphone. Natural language processing (NLP) allows you to log expenses like "Spent 500 on dinner" instantly.

---

## 🛠 Tech Stack

*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
*   **Architecture:** Clean Architecture + MVVM + UDF (Unidirectional Data Flow).
*   **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) for persistent local storage.
*   **Async/Streams:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html).
*   **Preferences:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for lightweight key-value storage.
*   **Animations:** Compose Animation API (Springs, Transitions, Graphics Layers).

---

## 🏗 Project Structure

```text
com.example.myexpenses
├── core              # Shared modules (UI components, Base classes, SMS logic)
├── feature
│   ├── home          # Dashboard, Recent activity, Animated balance
│   ├── analytics     # Insights, Trend charts, Category breakdowns
│   ├── settings      # User preferences, SMS permissions
│   └── streak        # Consistency tracking & Calendar view
└── domain            # Business logic and UseCases
```

---

## 🔒 Privacy & Security

We believe your financial data is your business.
- **Offline Core:** All transaction processing happens locally.
- **Zero Tracking:** No external analytics or tracking SDKs.
- **Biometric Lock:** Secure your data behind your device's fingerprint or face unlock.

---

## 🚀 Getting Started

1.  Clone the repository.
2.  Open in **Android Studio Ladybug (or newer)**.
3.  Build & Run on an Android device (API 26+).
4.  Grant SMS permissions to see the magic happen!

---

*Built with ❤️ and Kotlin.*
