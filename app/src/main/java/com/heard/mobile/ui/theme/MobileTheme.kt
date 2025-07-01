package com.heard.mobile.ui.theme

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
import com.hear.mobile.ui.theme.EarthBrown
import com.hear.mobile.ui.theme.EarthBrownDark
import com.hear.mobile.ui.theme.PineGreen
import com.hear.mobile.ui.theme.PineGreenDark
import com.hear.mobile.ui.theme.RockGray
import com.hear.mobile.ui.theme.RockGrayDark
import com.hear.mobile.ui.theme.SkyGray
import com.hear.mobile.ui.theme.SkyGrayDark
import com.hear.mobile.ui.theme.SnowWhite
import com.hear.mobile.ui.theme.SnowWhiteDark

private val LightColorScheme = lightColorScheme(
    primary = EarthBrown,
    onPrimary = SnowWhite,
    secondary = PineGreen,
    onSecondary = SnowWhite,
    background = SnowWhite,
    onBackground = Color.Black,
    surface = SkyGray,
    onSurface = Color.Black,
    outline = RockGray
)

private val DarkColorScheme = darkColorScheme(
    primary = EarthBrownDark,
    onPrimary = SnowWhiteDark,
    secondary = PineGreenDark,
    onSecondary = SnowWhiteDark,
    background = SkyGrayDark,
    onBackground = Color.White,
    surface = RockGrayDark,
    onSurface = Color.White,
    outline = RockGrayDark,
)

@Composable
fun MobileTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(), // <-- nome coerente con MainActivity
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
