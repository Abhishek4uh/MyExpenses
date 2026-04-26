package com.example.myexpenses.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GFont
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.myexpenses.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private fun gfont(name: String, w: FontWeight, italic: Boolean = false) =
    GFont(GoogleFont(name), provider, weight = w,
          style = if (italic) FontStyle.Italic else FontStyle.Normal)

val InterFamily = FontFamily(
    gfont("Inter", FontWeight.Normal),
    gfont("Inter", FontWeight.Medium),
    gfont("Inter", FontWeight.SemiBold),
    gfont("Inter", FontWeight.Bold),
)

val SerifFamily = FontFamily(
    gfont("Instrument Serif", FontWeight.Normal),
    gfont("Instrument Serif", FontWeight.Normal, italic = true),
)

val MyTypography = Typography(
    // Big balance / amounts — Inter
    displayLarge = TextStyle(
        fontFamily = InterFamily, fontSize = 56.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-2.24).sp, lineHeight = 56.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = InterFamily, fontSize = 64.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-2.56).sp, lineHeight = 64.sp,
    ),
    // Serif headlines — Instrument Serif
    headlineLarge = TextStyle(
        fontFamily = SerifFamily, fontSize = 36.sp, fontWeight = FontWeight.Normal,
        letterSpacing = (-0.72).sp, lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifFamily, fontSize = 32.sp, fontWeight = FontWeight.Normal,
        letterSpacing = (-0.64).sp, lineHeight = 35.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = SerifFamily, fontSize = 28.sp, fontWeight = FontWeight.Normal,
        letterSpacing = (-0.56).sp, lineHeight = 34.sp,
    ),
    // Inter UI text
    titleLarge = TextStyle(
        fontFamily = InterFamily, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.44).sp, lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.48.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.32.sp,
    ),
)
