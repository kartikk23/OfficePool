package com.agile.officepool.ui.theme


import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.agile.officepool.R // Replace with your actual R

val RobotoCondensed = FontFamily(
    Font(R.font.roboto_condensed_variablefont_wght, FontWeight.Normal),
//    Font(R.font.roboto_condensed_variablefont_wght, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

