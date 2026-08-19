package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CrmBlueLight,
    onPrimary = Color(0xFF001D35),
    primaryContainer = Color(0xFF00458E),
    onPrimaryContainer = Color(0xFFDDE2F1),
    secondary = CrmEmeraldLight,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFFDCFCE7),
    tertiary = CrmAmberLight,
    onTertiary = Color(0xFF491400),
    tertiaryContainer = Color(0xFF6B2000),
    onTertiaryContainer = Color(0xFFFFEDD5),
    background = CrmBgDark,
    onBackground = CrmTextPrimaryDark,
    surface = CrmSurfaceDark,
    onSurface = CrmTextPrimaryDark,
    surfaceVariant = CrmSurfaceVariantDark,
    onSurfaceVariant = CrmTextSecondaryDark,
    outline = CrmOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CrmPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = CrmPrimaryContainer,
    onPrimaryContainer = CrmOnPrimaryContainer,
    secondary = CrmPrimaryBlue,
    onSecondary = Color.White,
    secondaryContainer = CrmPrimaryContainer,
    onSecondaryContainer = CrmOnPrimaryContainer,
    tertiary = CrmAmber,
    onTertiary = Color.White,
    tertiaryContainer = CrmAmberContainer,
    onTertiaryContainer = CrmOnAmberContainer,
    background = CrmBgLight,
    onBackground = CrmTextPrimaryLight,
    surface = CrmSurfaceLight,
    onSurface = CrmTextPrimaryLight,
    surfaceVariant = CrmSurfaceVariantLight,
    onSurfaceVariant = CrmTextSecondaryLight,
    outline = CrmOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
