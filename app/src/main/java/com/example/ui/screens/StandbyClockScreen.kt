package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.ui.SimulatedNotification
import com.example.ui.StandbyViewModel
import com.example.ui.components.*
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StandbyClockScreen(
    viewModel: StandbyViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val notes by viewModel.bedsideNotes.collectAsState()
    val notificationList by viewModel.notifications.collectAsState()
    val isNightRed = settings.isNightModeEnabled

    // UI options
    val widgetOptions = listOf("digital_clock", "calendar", "weather", "notes")

    // Local theme mapping
    val selectedAccent = when (settings.clockTheme) {
        "sunset_orange" -> Color(0xFFFF5722)
        "electric_blue" -> Color(0xFF00B0FF)
        "cyber_green" -> Color(0xFF00E676)
        "neon_red" -> Color(0xFFFF1744)
        "minimal_white" -> Color(0xFFF5F5F5)
        else -> Color(0xFFFF5722)
    }

    // Controls visibility fade timer
    var controlsVisible by remember { mutableStateOf(true) }
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            kotlinx.coroutines.delay(5000)
            controlsVisible = false
        }
    }

    // Screen color styles
    val bgGradient = if (isNightRed) {
        Brush.radialGradient(
            colors = listOf(Color(0xFF150000), Color(0xFF000000)),
            radius = 1200f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF000000)),
            radius = 1200f
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .pointerInput(Unit) {
                // Clicking is a form of wake-up or interactive layout reveal
                detectVerticalDragGestures { change, dragAmount ->
                    controlsVisible = true
                }
            }
            .clickable {
                controlsVisible = !controlsVisible
                viewModel.resetTouch() // Tickle user activity
            }
            .testTag("standby_clock_screen_container")
    ) {
        // --- 1. Background Photo simulation for Photo clock ---
        if (settings.layoutType == "photo_clock" && !isNightRed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D47A1).copy(alpha = 0.3f),
                                Color(0xFFE0F7FA).copy(alpha = 0.15f),
                                Color(0xFF1E3A8A).copy(alpha = 0.4f)
                            )
                        )
                    )
            ) {
                // Ambient particle canvas glow
                CanvasGlowParticles()
            }
        }

        // --- 2. Main Content Screens ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (settings.layoutType) {
                "dual_widget" -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Widget Frame
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(
                                    if (isNightRed) Color(0xFF0A0101) else Color(0xFF18181B)
                                )
                                .border(
                                    1.dp,
                                    if (isNightRed) Color(0xFF350404) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(36.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render chosen left widget
                            WidgetDispatcher(
                                widgetType = settings.leftWidgetType,
                                isNightRed = isNightRed,
                                accentColor = selectedAccent,
                                notes = notes,
                                onToggleNote = { viewModel.toggleNoteCompleted(it) }
                            )

                            // Hover arrow overrides for emulator accessibility (cycles up/down)
                            if (controlsVisible) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            val currIdx = widgetOptions.indexOf(settings.leftWidgetType)
                                            val nextIdx = (currIdx + 1) % widgetOptions.size
                                            viewModel.updateWidgets(left = widgetOptions[nextIdx])
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (isNightRed) Color(0xFF350404) else Color(0xFF313646),
                                                CircleShape
                                            )
                                            .testTag("cycle_left_widget")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Cycle left widget upward",
                                            tint = if (isNightRed) Color(0xFFFF1E1E) else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Widget Frame
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(
                                    if (isNightRed) Color(0xFF0A0101) else Color(0xFF18181B)
                                )
                                .border(
                                    1.dp,
                                    if (isNightRed) Color(0xFF350404) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(36.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render chosen right widget
                            WidgetDispatcher(
                                widgetType = settings.rightWidgetType,
                                isNightRed = isNightRed,
                                accentColor = selectedAccent,
                                notes = notes,
                                onToggleNote = { viewModel.toggleNoteCompleted(it) }
                            )

                            // Hover arrow overrides (cycles up/down)
                            if (controlsVisible) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            val currIdx = widgetOptions.indexOf(settings.rightWidgetType)
                                            val nextIdx = (currIdx + 1) % widgetOptions.size
                                            viewModel.updateWidgets(right = widgetOptions[nextIdx])
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (isNightRed) Color(0xFF350404) else Color(0xFF313646),
                                                CircleShape
                                            )
                                            .testTag("cycle_right_widget")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Cycle right widget upward",
                                            tint = if (isNightRed) Color(0xFFFF1E1E) else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "full_analogue" -> {
                    WidgetAnalogueClock(
                        useNightRed = isNightRed,
                        accentColor = selectedAccent,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .testTag("live_analogue_clock_g")
                    )
                }

                "flip_clock" -> {
                    WidgetFlipClock(
                        useNightRed = isNightRed,
                        accentColor = selectedAccent,
                        is24Hour = settings.is24HourFormat,
                        showSeconds = settings.showSeconds,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                "photo_clock" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        WidgetDigitalClock(
                            useNightRed = isNightRed,
                            accentColor = Color.White, // Overlay looks stunning in high contrast white
                            is24Hour = settings.is24HourFormat,
                            showSeconds = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- 3. Bedside Real-time Notifications Toast Overlays ---
        AnimatedVisibility(
            visible = notificationList.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 32.dp, end = 32.dp)
                .widthIn(max = 480.dp)
        ) {
            notificationList.firstOrNull()?.let { notif ->
                NotificationBanner(
                    notif = notif,
                    useNightRed = isNightRed,
                    accentColor = selectedAccent,
                    onDismiss = { viewModel.dismissNotification(notif.id) }
                )
            }
        }

        // --- 4. Floating AOD Bedside Settings UI overlay ---
        // Hidden after 5 seconds of inactivity. Tapping wakes them up!
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                color = if (isNightRed) Color(0xFF1F0404) else Color(0xEC1E212B),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (isNightRed) Color(0xFF5A0808) else Color(0xFF3E4357)
                ),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(symmetricPaddingValues()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Layout cycler
                    IconButton(
                        onClick = {
                            val layouts = listOf("dual_widget", "full_analogue", "flip_clock", "photo_clock")
                            val nextIdx = (layouts.indexOf(settings.layoutType) + 1) % layouts.size
                            viewModel.updateLayoutType(layouts[nextIdx])
                        },
                        modifier = Modifier.testTag("clock_layout_cycler")
                    ) {
                        Icon(
                            imageVector = when (settings.layoutType) {
                                "dual_widget" -> Icons.Default.Dashboard
                                "full_analogue" -> Icons.Default.AccessTime
                                "flip_clock" -> Icons.Default.HourglassTop
                                "photo_clock" -> Icons.Default.Image
                                else -> Icons.Default.Dashboard
                            },
                            contentDescription = "Switch layout type",
                            tint = if (isNightRed) Color(0xFFFF1E1E) else Color.White
                        )
                    }

                    // Night monochrome red toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleNightMode() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = "Toggle night mode monochrome glow",
                            tint = if (isNightRed) Color(0xFFFF1E1E) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NIGHT RED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightRed) Color(0xFFFF1E1E) else Color.White
                        )
                    }

                    // Simulated Ambient Sensor Toggler
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.isBedsideAmbientDim.value = !viewModel.isBedsideAmbientDim.value }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (viewModel.isBedsideAmbientDim.value) Icons.Default.SensorsOff else Icons.Default.Sensors,
                            contentDescription = "Simulate dark ambient lighting",
                            tint = if (isNightRed) Color(0xFFFF1E1E) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (viewModel.isBedsideAmbientDim.value) "AMB: DARK" else "AMB: NORMAL",
                            fontSize = 11.sp,
                            color = if (isNightRed) Color(0xFFFF1E1E) else Color.White
                        )
                    }

                    // Wake-up active exit button
                    Button(
                        onClick = { viewModel.exitStandby() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNightRed) Color(0xFFFF1E1E) else selectedAccent,
                            contentColor = if (isNightRed) Color.Black else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("wake_up_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WAKE UP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetDispatcher(
    widgetType: String,
    isNightRed: Boolean,
    accentColor: Color,
    notes: List<com.example.data.BedsideNote>,
    onToggleNote: (com.example.data.BedsideNote) -> Unit
) {
    when (widgetType) {
        "digital_clock" -> WidgetDigitalClock(
            useNightRed = isNightRed,
            accentColor = accentColor,
            showSeconds = false,
            compactMode = true
        )
        "calendar" -> WidgetCalendar(
            useNightRed = isNightRed,
            accentColor = accentColor
        )
        "weather" -> WidgetWeather(
            useNightRed = isNightRed,
            accentColor = accentColor
        )
        "notes" -> WidgetNotes(
            notes = notes,
            onToggleNote = onToggleNote,
            useNightRed = isNightRed,
            accentColor = accentColor
        )
        else -> WidgetDigitalClock(
            useNightRed = isNightRed,
            accentColor = accentColor,
            showSeconds = false,
            compactMode = true
        )
    }
}

@Composable
fun NotificationBanner(
    notif: SimulatedNotification,
    useNightRed: Boolean,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val themeRed = Color(0xFFFF1E1E)
    val cardBg = if (useNightRed) Color(0xFF2E0505) else Color(0xFF1E2638)
    val textColor = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface
    val dimTextColor = if (useNightRed) themeRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (useNightRed) themeRed.copy(alpha = 0.3f) else Color(0xFF343E54)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDismiss() }
            .testTag("notification_banner_item")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (useNightRed) themeRed.copy(alpha = 0.1f) else accentColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notif.iconName) {
                        "sms" -> Icons.Default.Sms
                        "mail" -> Icons.Default.Mail
                        "alarm" -> Icons.Default.Alarm
                        "favorite" -> Icons.Default.Favorite
                        else -> Icons.Default.NotificationsActive
                    },
                    contentDescription = null,
                    tint = if (useNightRed) themeRed else accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textColor
                    )
                    Text(
                        text = notif.timeReceived,
                        fontSize = 11.sp,
                        color = dimTextColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notif.text,
                    fontSize = 12.sp,
                    color = if (useNightRed) themeRed.copy(alpha = 0.8f) else textColor.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun CanvasGlowParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles_transition")
    val animFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_drift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Draw soft ambient drift background glows
        drawCircle(
            color = Color(0xFF0D47A1).copy(alpha = 0.15f),
            radius = w * 0.35f,
            center = Offset(
                w * 0.3f + (w * 0.1f * cos(animFactor * 2 * Math.PI)).toFloat(),
                h * 0.4f + (h * 0.1f * sin(animFactor * 2 * Math.PI)).toFloat()
            )
        )

        drawCircle(
            color = Color(0xFF311B92).copy(alpha = 0.15f),
            radius = w * 0.4f,
            center = Offset(
                w * 0.7f + (w * 0.1f * sin(animFactor * 2 * Math.PI)).toFloat(),
                h * 0.6f + (h * 0.1f * cos(animFactor * 2 * Math.PI)).toFloat()
            )
        )
    }
}

private fun symmetricPaddingValues(): PaddingValues {
    return PaddingValues(horizontal = 16.dp, vertical = 8.dp)
}
