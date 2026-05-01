# 💸 MyExpenses

**A sophisticated, privacy-first expense tracker built with modern Android standards.**

MyExpenses is designed with a "Liquid UI" philosophy, combining high-fidelity motion design with deep automated tracking to create a tactile and delightful financial companion.

---

## ✨ Key Features

### 🚀 Seamless Onboarding
The journey begins with a fluid, multi-step onboarding experience designed to reduce friction and showcase the app's personality.
*   **Visual Storytelling:** Custom illustrations featuring a core "Currency Hub" and floating transaction cards (Swiggy, Uber, Salary) that preview the app's ecosystem.
*   **Feature Discovery Grid:** Interactive, high-depth tiles that introduce core capabilities like **Voice Input**, **SMS Sync**, and **Advanced Analytics** with animated borders.
*   **Tactile Navigation:** Animated pager indicators and a signature "Impact FAB" with a heavy, colored shadow that guides users through the setup.

### 🤖 Intelligent SMS Sync
Stop manual entry. MyExpenses securely parses your bank and UPI SMS alerts locally on your device to automatically categorize and log your spending.
*   **Privacy First:** Your financial data never leaves your device.
*   **Historical Sync:** Pull in past transactions with a single tap using the built-in Inbox Sync service.

### 🎭 "Liquid" User Interface
Experience high-fidelity motion design and glassmorphic elements:
*   **Animated Balance:** A dramatic "throw, bounce, and land" animation that makes every period switch feel alive.
*   **Segmented Insights:** Smoothly transition between Daily, Weekly, Monthly, and Yearly views with spring-animated tab switches.
*   **Adaptive Theme:** Deep dark mode with vibrant accent "tones" for different expense categories.

### 📊 Deep Analytics & Insights
Understand your habits through beautiful, interactive visualizations:
*   **Trend Series:** Sophisticated line charts and grouped bar charts comparing Income vs. Expenses.
*   **Category Breakdown:** Dynamic sorting of spending groups with percentage-of-total and period-over-period change tracking.
*   **Insight Engine:** Automated analysis that surfaces spending anomalies and consistency streaks.

---

## 🎨 Design Language & System
Inspired by the **Claude Design Handoff**, MyExpenses employs a "Studio Dark" aesthetic—an OLED-first design system that balances deep immersion with vibrant semantic clarity.

### 🌈 Color System
A custom-engineered surface scale and accent palette:
- **OLED Surface Scale:** A precise monochromatic ramp from `BgBase` (#0A0A0A) for total immersion to `BgElev5` (#1C1C1C) for foreground elements.
- **Vibrant Accents:** High-saturation presets including **Amber** (#F2B23A), **Violet** (#A086F5), and **Cyan** (#6FCDDB) that provide life to the dark canvas.
- **Category Tones:** Each expense category (Rent, Food, Commute, etc.) has its own background-foreground "Tone" pair, ensuring that transactions are instantly recognizable by color alone.

### 🖋 Typography
A dual-type system that blends utility with expressive financial character:
- **Inter (Utility):** Used for all functional UI, analytical data, and dense information grids. Focuses on legibility and technical precision.
- **Instrument Serif (Expression):** Used for headlines and large financial figures. Its elegant, high-contrast strokes give the app a "premium ledger" and editorial feel.

### 🌪 Motion & Physics
Motion is treated as a first-class citizen, providing tactile feedback to every user action:
- **The "Blob" Canvas:** Dynamic, drifting radial gradients in the background create a sense of depth and life.
- **Spring-Loaded UI:** Instead of rigid durations, all animations use **Spring Physics** (`dampingRatio = 0.42f`, `stiffness = 180f`). This results in "bouncy" interactions that feel organic and responsive.
- **Impact Haptics:** Key actions like balance updates or FAB expansions are reinforced with coordinated haptic pulses.

---

## 🛠 Tech Stack

*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
*   **Architecture:** Clean Architecture + MVVM + UDF (Unidirectional Data Flow).
*   **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) for persistent local storage.
*   **Async/Streams:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html).

---

## 🏗 Project Structure

```text
com.example.myexpenses
├── core              # Shared UI components, Design tokens, SMS & Voice logic
├── feature
│   ├── onboarding    # Guided welcome flow & feature discovery
│   ├── home          # Dashboard, Recent activity, Animated balance
│   ├── analytics     # Insights, Trend charts, Category breakdowns
│   └── streak        # Consistency tracking & Calendar view
└── domain            # Business logic and cross-feature UseCases
```

---

## 🔒 Privacy & Security
We believe your financial data is your business.
- **Offline Core:** All transaction processing and SMS parsing happen locally on-device.
- **Zero Tracking:** No external analytics or tracking SDKs.
- **Biometric Lock:** Secure your data behind your device's fingerprint or face unlock.

---

*Built with ❤️ and Kotlin.*
