package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WidgetWeather(
    modifier: Modifier = Modifier,
    useNightRed: Boolean = false,
    accentColor: Color = Color(0xFFFF5722)
) {
    // Collect animation offsets for breathing/pulsing sun & clouds
    val infiniteTransition = rememberInfiniteTransition(label = "weather_infinite_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_weather"
    )

    val themeRed = Color(0xFFFF1E1E)
    val labelColor = if (useNightRed) themeRed else accentColor
    val secondaryColor = if (useNightRed) themeRed.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val weatherSunColor = if (useNightRed) themeRed else Color(0xFFFFD54F)
    val weatherCloudColor = if (useNightRed) themeRed.copy(alpha = 0.4f) else Color(0xFF90A4AE)

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left Column: Custom weather drawing
        Box(
            modifier = Modifier
                .size(72.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.4f, size.height * 0.4f)
                val baseRadius = size.width * 0.25f

                // Draw a pulsing sun!
                drawCircle(
                    color = weatherSunColor,
                    radius = baseRadius * pulseScale,
                    center = center
                )

                // Sun rays
                val rayCount = 8
                val rayLength = 8.dp.toPx()
                for (i in 0 until rayCount) {
                    val angleRad = (i * (360f / rayCount)) * (Math.PI / 180f)
                    val rayStart = Offset(
                        (center.x + (baseRadius * 1.15f) * cos(angleRad)).toFloat(),
                        (center.y + (baseRadius * 1.15f) * sin(angleRad)).toFloat()
                    )
                    val rayEnd = Offset(
                        (center.x + (baseRadius * 1.15f + rayLength) * cos(angleRad)).toFloat(),
                        (center.y + (baseRadius * 1.15f + rayLength) * sin(angleRad)).toFloat()
                    )
                    drawLine(
                        color = weatherSunColor,
                        start = rayStart,
                        end = rayEnd,
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Draw overlapping cloud
                val cloudCenter1 = Offset(size.width * 0.6f, size.height * 0.65f)
                val cloudCenter2 = Offset(size.width * 0.75f, size.height * 0.7f)
                drawCircle(
                    color = weatherCloudColor,
                    radius = baseRadius * 0.85f,
                    center = cloudCenter1
                )
                drawCircle(
                    color = weatherCloudColor,
                    radius = baseRadius * 0.65f,
                    center = cloudCenter2
                )
                drawRect(
                    color = weatherCloudColor,
                    topLeft = Offset(cloudCenter1.x, cloudCenter1.y - (baseRadius * 0.1f)),
                    size = Size(baseRadius * 1.5f, baseRadius * 0.95f)
                )
            }
        }

        // Right Column: Weather description strings
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WEATHER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryColor,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "72°",
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Partly Cloudy",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryColor
            )

            Text(
                text = "H: 74°  L: 64°",
                fontSize = 12.sp,
                color = secondaryColor
            )
        }
    }
}
