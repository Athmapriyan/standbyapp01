package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun WidgetFlipClock(
    modifier: Modifier = Modifier,
    useNightRed: Boolean = false,
    accentColor: Color = Color(0xFFFF5722),
    is24Hour: Boolean = false,
    showSeconds: Boolean = true
) {
    var hour by remember { mutableStateOf("12") }
    var minute by remember { mutableStateOf("00") }
    var second by remember { mutableStateOf("00") }

    LaunchedEffect(is24Hour) {
        while (true) {
            val c = Calendar.getInstance()
            val hh = if (is24Hour) {
                c.get(Calendar.HOUR_OF_DAY)
            } else {
                c.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            }.toString().padStart(2, '0')

            val mm = c.get(Calendar.MINUTE).toString().padStart(2, '0')
            val ss = c.get(Calendar.SECOND).toString().padStart(2, '0')

            hour = hh
            minute = mm
            second = ss
            delay(250)
        }
    }

    val systemAccent = if (useNightRed) Color(0xFFFF1E1E) else accentColor

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlipCard(value = hour, useNightRed = useNightRed, accentColor = systemAccent)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = ":",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = if (useNightRed) Color(0xFFFF1E1E) else Color.White,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        FlipCard(value = minute, useNightRed = useNightRed, accentColor = systemAccent)

        if (showSeconds) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = ":",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (useNightRed) Color(0xFFFF1E1E) else Color.White,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            FlipCard(
                value = second,
                scale = 0.75f,
                useNightRed = useNightRed,
                accentColor = systemAccent
            )
        }
    }
}

@Composable
fun FlipCard(
    value: String,
    scale: Float = 1.0f,
    useNightRed: Boolean = false,
    accentColor: Color
) {
    val cardBg = if (useNightRed) Color(0xFF1E0303) else Color(0xFF1A1A1A)
    val textColor = if (useNightRed) Color(0xFFFF1E1E) else Color.White
    val creaseColor = if (useNightRed) Color(0xFF3E0505) else Color(0xFF121212)

    val heightDp = (100 * scale).dp
    val widthDp = (80 * scale).dp
    val fontSp = (64 * scale).sp

    Box(
        modifier = Modifier
            .width(widthDp)
            .height(heightDp)
            .background(cardBg, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // We simulate the flip with Jetpack Compose Animating Content Transition
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(150))).togetherWith(
                    slideOutVertically { height -> height } + fadeOut(animationSpec = tween(150))
                )
            },
            label = "flip_card_anim"
        ) { text ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = fontSp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Horizontal fold line (physical divider) in middle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(2.dp)
                .background(creaseColor)
        )
    }
}
