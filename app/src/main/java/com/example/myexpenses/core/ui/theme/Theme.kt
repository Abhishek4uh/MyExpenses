package com.example.myexpenses.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAccent = staticCompositionLocalOf { Accents.Amber }

@Composable
fun MyExpensesTheme(
    accent: Color = Accents.Amber,
    content: @Composable () -> Unit,
) {
    val scheme = darkColorScheme(
        primary           = accent,
        onPrimary         = BgBase,
        background        = BgBase,
        onBackground      = TextPrimary,
        surface           = BgElev1,
        onSurface         = TextPrimary,
        surfaceVariant    = BgElev3,
        onSurfaceVariant  = TextSecondary,
        outline           = BorderDefault,
        outlineVariant    = BorderSubtle,
        error             = Danger,
        onError           = TextPrimary,
        secondaryContainer = BgElev3,
        onSecondaryContainer = TextSecondary,
    )

    val expenseColors = ExpenseColors(
        income = Success,
        incomeContainer = Color(0xFF0F2A1A),
        expense = Danger,
        expenseContainer = Color(0xFF3D1F1A),
        saving = Accents.Cyan,
        chartColors = AppColors.ChartColors,
    )

    CompositionLocalProvider(
        LocalAccent provides accent,
        LocalExpenseColors provides expenseColors,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = MyTypography,
            shapes      = MyShapes,
            content     = content,
        )
    }
}

// ─── Semantic token bag via CompositionLocal ──────────────────

data class ExpenseColors(
    val income: Color,
    val incomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val saving: Color,
    val chartColors: List<Color>,
)

val LocalExpenseColors = staticCompositionLocalOf {
    ExpenseColors(
        income = Success,
        incomeContainer = Color(0xFF0F2A1A),
        expense = Danger,
        expenseContainer = Color(0xFF3D1F1A),
        saving = Accents.Cyan,
        chartColors = AppColors.ChartColors,
    )
}

val MaterialTheme.expenseColors: ExpenseColors
    @Composable get() = LocalExpenseColors.current
