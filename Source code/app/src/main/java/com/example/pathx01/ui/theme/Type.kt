package com.example.pathx01.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.example.pathx01.R

// Custom Font Families
val MacondoFamily = FontFamily(
    Font(R.font.macondo_regular, FontWeight.Normal)
)

val BitcountFamily = FontFamily(
    Font(R.font.bitcountpropsingleink_variable, FontWeight.Normal)
)

// Special font families for different use cases
val HeadingFontFamily = MacondoFamily
val BodyFontFamily = MacondoFamily
val DecorativeFontFamily = BitcountFamily

// DayFlow Typography based on the design system with custom fonts
val Typography = Typography(
    // Headlines (DayFlow uses larger, bolder headings with Macondo font)
    displayLarge = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp, // 5xl
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp, // 4xl
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp, // 3xl
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    
    // Headlines
    headlineLarge = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp, // 3xl
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, // 2xl
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, // xl
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // Titles
    titleLarge = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, // lg
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, // md
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp, // base
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    
    // Body text
    bodyLarge = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // md
        lineHeight = 26.sp, // relaxed
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp, // base
        lineHeight = 24.sp, // normal
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp, // sm
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    
    // Labels
    labelLarge = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp, // base
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp, // sm
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MacondoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, // xs
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)