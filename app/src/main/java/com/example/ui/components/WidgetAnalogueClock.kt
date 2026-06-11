package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WidgetAnalogueClock(
    modifier: Modifier = Modifier,
    useNightRed: Boolean = false,
    accentColor: Color = Color(0xFFFF5722)
) {
    var timeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            timeMillis = System.currentTimeMillis()
            delay(16) // Smooth 60 FPS update loop!
        }
    }

    val calendar = remember(timeMillis) {
        Calendar.getInstance().apply { timeInMillis = timeMillis }
    }

    val milliseconds = calendar.get(Calendar.MILLISECOND)
    val seconds = calendar.get(Calendar.SECOND) + (milliseconds / 1000f)
    val minutes = calendar.get(Calendar.MINUTE) + (seconds / 60f)
    val hours = calendar.get(Calendar.HOUR) + (minutes / 60f)

    // Select color palette
    val themeRed = Color(0xFFFF1E1E)
    val clockFaceColor = if (useNightRed) Color(0xFF1E0303) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    val tickColor = if (useNightRed) themeRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val hourHandColor = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface
    val minuteHandColor = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface
    val secondHandColor = if (useNightRed) themeRed else accentColor
    val ringStrokeColor = if (useNightRed) themeRed.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val density = LocalDensity.current
    // Pre-calculate px values at layout stage, avoiding @Composable inside DrawScope!
    val ringStrokePx = remember(density) { with(density) { 3.dp.toPx() } }
    val hourTickPx = remember(density) { with(density) { 14.dp.toPx() } }
    val normalTickPx = remember(density) { with(density) { 8.dp.toPx() } }
    val hourTickWidthPx = remember(density) { with(density) { 3.dp.toPx() } }
    val normalTickWidthPx = remember(density) { with(density) { 1.5.dp.toPx() } }

    val hourOffsetBackShadowPx = remember(density) { with(density) { 12.dp.toPx() } }
    val hourHandWidthPx = remember(density) { with(density) { 6.dp.toPx() } }

    val minuteOffsetBackShadowPx = remember(density) { with(density) { 16.dp.toPx() } }
    val minuteHandWidthPx = remember(density) { with(density) { 4.dp.toPx() } }

    val secondOffsetBackShadowPx = remember(density) { with(density) { 20.dp.toPx() } }
    val secondHandWidthPx = remember(density) { with(density) { 2.dp.toPx() } }

    val corePinOuterPx = remember(density) { with(density) { 5.dp.toPx() } }
    val corePinInnerPx = remember(density) { with(density) { 2.dp.toPx() } }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize(0.9f)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            // 1. Draw outermost tick track (ring)
            drawCircle(
                color = clockFaceColor,
                radius = radius,
                center = center
            )
            drawCircle(
                color = ringStrokeColor,
                radius = radius,
                center = center,
                style = Stroke(width = ringStrokePx)
            )

            // 2. Draw hours ticks (1 to 12)
            for (i in 0 until 12) {
                val angleRad = (i * 30 - 90) * (Math.PI / 180f)
                val lineLength = if (i % 3 == 0) hourTickPx else normalTickPx
                val lineWidth = if (i % 3 == 0) hourTickWidthPx else normalTickWidthPx

                val outerTouch = Offset(
                    (center.x + radius * cos(angleRad)).toFloat(),
                    (center.y + radius * sin(angleRad)).toFloat()
                )
                val innerPoint = Offset(
                    (center.x + (radius - lineLength) * cos(angleRad)).toFloat(),
                    (center.y + (radius - lineLength) * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = if (i % 3 == 0 && !useNightRed) secondHandColor else tickColor,
                    start = innerPoint,
                    end = outerTouch,
                    strokeWidth = lineWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw Hour Hand
            val hourAngle = hours * 30f // 360 / 12
            withTransform({
                rotate(hourAngle, center)
            }) {
                drawLine(
                    color = hourHandColor,
                    start = Offset(center.x, center.y + hourOffsetBackShadowPx), // Back shadow overlap
                    end = Offset(center.x, center.y - (radius * 0.5f)),
                    strokeWidth = hourHandWidthPx,
                    cap = StrokeCap.Round
                )
            }

            // 4. Draw Minute Hand
            val minuteAngle = minutes * 6f // 360 / 60
            withTransform({
                rotate(minuteAngle, center)
            }) {
                drawLine(
                    color = minuteHandColor,
                    start = Offset(center.x, center.y + minuteOffsetBackShadowPx),
                    end = Offset(center.x, center.y - (radius * 0.75f)),
                    strokeWidth = minuteHandWidthPx,
                    cap = StrokeCap.Round
                )
            }

            // 5. Draw Second Hand (Smooth animated sweep!)
            val secondAngle = seconds * 6f // 360 / 60
            withTransform({
                rotate(secondAngle, center)
            }) {
                drawLine(
                    color = secondHandColor,
                    start = Offset(center.x, center.y + secondOffsetBackShadowPx),
                    end = Offset(center.x, center.y - (radius * 0.85f)),
                    strokeWidth = secondHandWidthPx,
                    cap = StrokeCap.Round
                )
            }

            // 6. Draw center core pin
            drawCircle(
                color = secondHandColor,
                radius = corePinOuterPx,
                center = center
            )
            drawCircle(
                color = if (useNightRed) Color.Black else Color.White,
                radius = corePinInnerPx,
                center = center
            )
        }
    }
}
