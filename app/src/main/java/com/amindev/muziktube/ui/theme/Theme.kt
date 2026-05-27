package com.amindev.muziktube.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgColor        = Color(0xFF080808)
val SurfaceColor   = Color(0xFF1C1C1E)
val SurfaceVariant = Color(0xFF2C2C2E)
val RedPrimary     = Color(0xFFFF4444)
val TextSecondary  = Color(0xFFAAAAAA)

private val Colors = darkColorScheme(
    primary         = RedPrimary,
    background      = BgColor,
    surface         = SurfaceColor,
    surfaceVariant  = SurfaceVariant,
    onBackground    = Color.White,
    onSurface       = Color.White,
    onPrimary       = Color.White,
)

@Composable
fun MuzikTubeTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Colors, content = content)
