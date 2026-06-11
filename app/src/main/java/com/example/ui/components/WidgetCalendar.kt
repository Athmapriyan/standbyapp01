package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WidgetCalendar(
    modifier: Modifier = Modifier,
    useNightRed: Boolean = false,
    accentColor: Color = Color(0xFFFF5722)
) {
    val calendar = remember { Calendar.getInstance() }
    val currentDay = remember { calendar.get(Calendar.DAY_OF_MONTH) }
    val monthYearString = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    // Compute the days grid items for this month
    val gridItems = remember {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.

        val itemsList = mutableListOf<String>()
        // Pad days before month starts
        for (i in 1 until firstDayOfWeek) {
            itemsList.add("")
        }
        for (i in 1..daysInMonth) {
            itemsList.add(i.toString())
        }
        itemsList
    }

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    val themeRed = Color(0xFFFF1E1E)
    val headerColor = if (useNightRed) themeRed else accentColor
    val activeDayBgColor = if (useNightRed) themeRed else accentColor
    val activeDayTextColor = if (useNightRed) Color.Black else Color.White
    val textColor = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface
    val dimTextColor = if (useNightRed) themeRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Month, Year Header
        Text(
            text = monthYearString.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = dimTextColor,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = dimTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days Grid Calendar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                userScrollEnabled = false
            ) {
                items(gridItems) { day ->
                    val isToday = day == currentDay.toString()

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) activeDayBgColor else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.isNotEmpty()) {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) activeDayTextColor else textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
