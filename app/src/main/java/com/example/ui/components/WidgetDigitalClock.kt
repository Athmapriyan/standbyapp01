package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WidgetDigitalClock(
    modifier: Modifier = Modifier,
    useNightRed: Boolean = false,
    accentColor: Color = Color(0xFFFF5722),
    is24Hour: Boolean = false,
    showSeconds: Boolean = true,
    compactMode: Boolean = false
) {
    var timeString by remember { mutableStateOf("") }
    var secondsString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var dayOfWeekString by remember { mutableStateOf("") }

    LaunchedEffect(is24Hour, showSeconds) {
        while (true) {
            val date = Date()
            val timeFormatPattern = if (is24Hour) "HH:mm" else "h:mm"
            val timeFormatter = SimpleDateFormat(timeFormatPattern, Locale.getDefault())
            val secondsFormatter = SimpleDateFormat("ss", Locale.getDefault())
            val dateFormatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())

            timeString = timeFormatter.format(date)
            secondsString = secondsFormatter.format(date)
            dateString = dateFormatter.format(date)
            dayOfWeekString = dayFormatter.format(date).uppercase()

            delay(250)
        }
    }

    val themeColor = if (useNightRed) Color(0xFFFF1E1E) else accentColor
    val secondaryColor = if (useNightRed) Color(0xFFFF1E1E).copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(if (compactMode) 8.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Day of Week
        Text(
            text = dayOfWeekString,
            fontSize = if (compactMode) 14.sp else 18.sp,
            fontWeight = FontWeight.Medium,
            color = themeColor,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Time (Hour & Minute)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeString,
                fontSize = if (compactMode) 64.sp else 96.sp,
                fontWeight = FontWeight.Light,
                color = if (useNightRed) Color(0xFFFF1E1E) else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                letterSpacing = (-2).sp
            )

            if (showSeconds && !compactMode) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = secondsString,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Date String
        Text(
            text = dateString,
            fontSize = if (compactMode) 12.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            color = secondaryColor,
            textAlign = TextAlign.Center
        )
    }
}
