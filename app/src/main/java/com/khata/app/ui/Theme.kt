package com.khata.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F3E9E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E4FF),
    onPrimaryContainer = Color(0xFF0B1450),
    secondary = Color(0xFF585E7E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE1F5),
    onSecondaryContainer = Color(0xFF151A37),
    background = Color(0xFFF5F6FB),
    onBackground = Color(0xFF1A1B24),
    surface = Color.White,
    onSurface = Color(0xFF1A1B24),
    surfaceVariant = Color(0xFFE6E8F2),
    onSurfaceVariant = Color(0xFF454859),
    outline = Color(0xFF767A8E),
    outlineVariant = Color(0xFFC6C9DA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FE),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF0F1F8),
    surfaceContainerHighest = Color(0xFFE9EBF4),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7C1FF),
    onPrimary = Color(0xFF0B1450),
    primaryContainer = Color(0xFF2A3785),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC0C5E4),
    onSecondary = Color(0xFF2A2F4D),
    secondaryContainer = Color(0xFF363B58),
    onSecondaryContainer = Color(0xFFDDE1F5),
    background = Color(0xFF0F1220),
    onBackground = Color(0xFFE4E5F1),
    surface = Color(0xFF171A2B),
    onSurface = Color(0xFFE4E5F1),
    surfaceVariant = Color(0xFF262A3F),
    onSurfaceVariant = Color(0xFFC3C6DA),
    outline = Color(0xFF8D90A5),
    outlineVariant = Color(0xFF3B3F57),
    surfaceContainerLowest = Color(0xFF0B0D19),
    surfaceContainerLow = Color(0xFF141726),
    surfaceContainer = Color(0xFF1A1D2F),
    surfaceContainerHigh = Color(0xFF232739),
    surfaceContainerHighest = Color(0xFF2D3145),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun KhataTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}

@Composable
fun isDarkScheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun receivedColor(): Color = if (isDarkScheme()) Color(0xFF5FD38D) else Color(0xFF17803F)

@Composable
fun givenColor(): Color = if (isDarkScheme()) Color(0xFFFF8A80) else Color(0xFFC62828)

@Composable
fun receivedContainer(): Color = if (isDarkScheme()) Color(0xFF15382A) else Color(0xFFE3F4E9)

@Composable
fun givenContainer(): Color = if (isDarkScheme()) Color(0xFF4A2226) else Color(0xFFFBE7E6)

@Composable
fun balanceColor(balance: Long): Color = when {
    balance > 0 -> receivedColor()
    balance < 0 -> givenColor()
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** Rounded card with a hairline border, used across the app. */
@Composable
fun KCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    val shape = RoundedCornerShape(20.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = actions,
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

/** Calculator glyph (Material "calculate" shape) so we don't need the big extended icon set. */
val CalcIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Calc", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(
            "M19,3H5C3.9,3 3,3.9 3,5V19C3,20.1 3.9,21 5,21H19C20.1,21 21,20.1 21,19V5C21,3.9 20.1,3 19,3Z " +
            "M6.25,7.72H11.5V9.22H6.25Z M13,15.75H18V17.25H13Z M13,12.25H18V13.75H13Z " +
            "M8,18H9.5V16.5H11V15H9.5V13.5H8V15H6.5V16.5H8Z " +
            "M13.03,8.5L14.09,9.56L15.5,8.15L16.91,9.56L17.97,8.5L16.56,7.09L17.97,5.68L16.91,4.62L15.5,6.03L14.09,4.62L13.03,5.68L14.44,7.09Z"
        ),
        pathFillType = PathFillType.EvenOdd,
        fill = SolidColor(Color.Black)
    ).build()
}
