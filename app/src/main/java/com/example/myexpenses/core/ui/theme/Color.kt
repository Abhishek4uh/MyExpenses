package com.example.myexpenses.core.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Surface scale ─────────────────────────────────────────
val BgBase    = Color(0xFF0A0A0A)
val BgElev1   = Color(0xFF0E0E0E)
val BgElev2   = Color(0xFF141414)
val BgElev3   = Color(0xFF161616)
val BgElev4   = Color(0xFF1A1A1A)
val BgElev5   = Color(0xFF1C1C1C)

val BorderSubtle  = Color(0xFF1A1A1A)
val BorderDefault = Color(0xFF222222)
val BorderStrong  = Color(0xFF333333)

val TextPrimary   = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFCCCCCC)
val TextTertiary  = Color(0xFF888888)
val TextMuted     = Color(0xFF666666)
val TextFaint     = Color(0xFF555555)
val TextDisabled  = Color(0xFF444444)

// ─── Accent presets (user-selectable) ──────────────────────
object Accents {
    val Amber  = Color(0xFFF2B23A)
    val Green  = Color(0xFF3BC97A)
    val Violet = Color(0xFFA086F5)
    val Cyan   = Color(0xFF6FCDDB)
    val Pink   = Color(0xFFE889B0)
}

// ─── Semantic ────────────────────────────────────────────────
val Success = Color(0xFF3BC97A)
val Danger  = Color(0xFFF08877)

// ─── Category tones (bg = dark hue, fg = light hue) ─────────
data class CategoryTone(val bg: Color, val fg: Color)

object CategoryTones {
    val Rent          = CategoryTone(Color(0xFF1B2C36), Color(0xFF7FBED7))
    val Commute       = CategoryTone(Color(0xFF2D2A1F), Color(0xFFD7B97F))
    val Food          = CategoryTone(Color(0xFF332620), Color(0xFFE0A78A))
    val OnlineFood    = CategoryTone(Color(0xFF362320), Color(0xFFE39B85))
    val Utilities     = CategoryTone(Color(0xFF2D2B1B), Color(0xFFD7BD7F))
    val Insurance     = CategoryTone(Color(0xFF1F2A36), Color(0xFF8FB6D8))
    val Healthcare    = CategoryTone(Color(0xFF362020), Color(0xFFE38585))
    val Saving        = CategoryTone(Color(0xFF1F302A), Color(0xFF7FD7BD))
    val Personal      = CategoryTone(Color(0xFF36202F), Color(0xFFE385C2))
    val Entertainment = CategoryTone(Color(0xFF2A203A), Color(0xFFB58FE3))
    val Misc          = CategoryTone(Color(0xFF22262E), Color(0xFFA1B0C8))
}

// ─── AppColors — backward-compat wrapper ────────────────────
object AppColors {
    val Background       = BgBase
    val SurfaceCard      = BgElev2
    val SurfaceElevated  = BgElev4
    val Primary          = Accents.Amber
    val PrimaryDim       = Color(0xFFC4902E)
    val PrimaryContainer = Color(0xFF2A1F0A)
    val Income           = Success
    val IncomeContainer  = Color(0xFF0F2A1A)
    val Expense          = Danger
    val ExpenseContainer = Color(0xFF2A110D)
    val Saving           = Accents.Cyan
    val Warning          = Accents.Amber
    val TextPrimary      = Color(0xFFFFFFFF)
    val TextSecondary    = Color(0xFFCCCCCC)
    val TextDisabled     = Color(0xFF444444)
    val Border           = BorderDefault
    val Divider          = BorderSubtle
    val ChartColors = listOf(
        Accents.Amber, Success, Accents.Violet, Accents.Cyan, Accents.Pink,
        CategoryTones.Rent.fg, CategoryTones.Food.fg, CategoryTones.Utilities.fg,
        CategoryTones.Healthcare.fg, CategoryTones.Entertainment.fg,
        CategoryTones.Personal.fg, CategoryTones.Misc.fg,
    )
}
