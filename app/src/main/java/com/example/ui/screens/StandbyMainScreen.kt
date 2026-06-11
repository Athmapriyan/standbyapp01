package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BedsideNote
import com.example.data.ClockSettings
import com.example.ui.StandbyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StandbyMainScreen(
    viewModel: StandbyViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val notes by viewModel.bedsideNotes.collectAsState()

    val isSimCharging by viewModel.isSimulatedCharging.collectAsState()
    val isSimLandscape by viewModel.isSimulatedLandscape.collectAsState()
    val isActCharging by viewModel.isActualCharging.collectAsState()
    val isActLandscape by viewModel.isActualLandscape.collectAsState()

    val countdown by viewModel.idleCountdown.collectAsState()
    val isTimerActive by viewModel.isAutostartTimerRunning.collectAsState()

    // Local inputs
    var noteInput by remember { mutableStateOf("") }
    var mockNotifTitle by remember { mutableStateOf("Cozy Alert") }
    var mockNotifText by remember { mutableStateOf("Wind down, bedside mode has been customized!") }

    val activeAccentColor = when (settings.clockTheme) {
        "sunset_orange" -> Color(0xFFFF5722)
        "electric_blue" -> Color(0xFF00B0FF)
        "cyber_green" -> Color(0xFF00E676)
        "neon_red" -> Color(0xFFFF1744)
        "minimal_white" -> Color(0xFFECEFF1)
        else -> Color(0xFFFF5722)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06080C))
            .clickable { viewModel.resetTouch() } // Interacting resets the idle countdown!
            .testTag("standby_dashboard_root")
    ) {
        Scaffold(
            topBar = {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Brush.linearGradient(listOf(activeAccentColor, activeAccentColor.copy(alpha = 0.4f))),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "STANDBY MODE",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Bedside Desk Display Assistant",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8B949E)
                                )
                            }
                        }

                        // Big launching action button
                        Button(
                            onClick = { viewModel.forceLaunchStandby() },
                            colors = ButtonDefaults.buttonColors(containerColor = activeAccentColor),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("launch_standby_now_btn_tag")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RUN NOW", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isWideLayout = maxWidth > 600.dp

                if (isWideLayout) {
                    // Landscape dashboard splits into two horizontal panes
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LandscapePanelScroll(
                                countdown = countdown,
                                isTimerActive = isTimerActive,
                                activeAccentColor = activeAccentColor,
                                settings = settings,
                                viewModel = viewModel,
                                isSimCharging = isSimCharging,
                                isSimLandscape = isSimLandscape,
                                isActCharging = isActCharging,
                                isActLandscape = isActLandscape,
                                mockNotifTitle = mockNotifTitle,
                                mockNotifText = mockNotifText,
                                onTitleChange = { mockNotifTitle = it },
                                onTextChange = { mockNotifText = it }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NotesLogContainer(
                                notes = notes,
                                noteInput = noteInput,
                                onInputChange = { noteInput = it },
                                viewModel = viewModel,
                                activeAccentColor = activeAccentColor
                            )
                        }
                    }
                } else {
                    // Vertical Portrait display (standard phones)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // 1. Idle Auto-Start Banner (Shows the 3-seconds idle indicator)
                            AodCountdownBanner(
                                countdown = countdown,
                                isTimerActive = isTimerActive,
                                activeAccentColor = activeAccentColor,
                                settings = settings,
                                viewModel = viewModel,
                                isSimCharging = isSimCharging
                            )
                        }

                        item {
                            // 2. Desk Display Customization Panel
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                                border = BorderStroke(1.dp, Color(0xFF21262D)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Palette, contentDescription = null, tint = activeAccentColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("CUSTOMIZATION", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Clock layout choose
                                    Text("Pick Default AOD Screen Layout", fontSize = 12.sp, color = Color(0xFF8B949E))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LayoutSelectorButton("Dual Widget", "dual_widget", settings.layoutType, activeAccentColor) {
                                            viewModel.updateLayoutType("dual_widget")
                                        }
                                        LayoutSelectorButton("Sweep Analogue", "full_analogue", settings.layoutType, activeAccentColor) {
                                            viewModel.updateLayoutType("full_analogue")
                                        }
                                        LayoutSelectorButton("Retro Flip Clock", "flip_clock", settings.layoutType, activeAccentColor) {
                                            viewModel.updateLayoutType("flip_clock")
                                        }
                                        LayoutSelectorButton("Scenic Photo", "photo_clock", settings.layoutType, activeAccentColor) {
                                            viewModel.updateLayoutType("photo_clock")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Accent color choose
                                    Text("Dock Aesthetic Theme Accent", fontSize = 12.sp, color = Color(0xFF8B949E))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ThemeColorPill("sunset_orange", Color(0xFFFF5722), settings.clockTheme) { viewModel.updateTheme("sunset_orange") }
                                        ThemeColorPill("electric_blue", Color(0xFF00B0FF), settings.clockTheme) { viewModel.updateTheme("electric_blue") }
                                        ThemeColorPill("cyber_green", Color(0xFF00E676), settings.clockTheme) { viewModel.updateTheme("cyber_green") }
                                        ThemeColorPill("neon_red", Color(0xFFFF1744), settings.clockTheme) { viewModel.updateTheme("neon_red") }
                                        ThemeColorPill("minimal_white", Color(0xFFF5F5F5), settings.clockTheme) { viewModel.updateTheme("minimal_white") }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Custom Toggles
                                    Text("Features", fontSize = 12.sp, color = Color(0xFF8B949E))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FeatureTogglerRow("AOD Night mode (Soft Red Overlay)", settings.isNightModeEnabled) {
                                        viewModel.toggleNightMode()
                                    }
                                    FeatureTogglerRow("Show Seconds Hand/Count", settings.showSeconds) {
                                        viewModel.toggleShowSeconds()
                                    }
                                    FeatureTogglerRow("24-Hour Time Format", settings.is24HourFormat) {
                                        viewModel.toggle24HourFormat()
                                    }
                                }
                            }
                        }

                        // Widget Stack Customizer for Dual-Widget Column layout
                        if (settings.layoutType == "dual_widget") {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                                    border = BorderStroke(1.dp, Color(0xFF21262D)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = activeAccentColor)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("DUAL WIDGET GRID", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Select active panels showing when docked on bedside flat stand.", fontSize = 11.sp, color = Color(0xFF8B949E))

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1.5f)) {
                                                Text("LEFT SIDE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = activeAccentColor)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                WidgetDropdownMenu(settings.leftWidgetType, activeAccentColor) {
                                                    viewModel.updateWidgets(left = it)
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1.5f)) {
                                                Text("RIGHT SIDE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = activeAccentColor)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                WidgetDropdownMenu(settings.rightWidgetType, activeAccentColor) {
                                                    viewModel.updateWidgets(right = it)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // 3. Bedsides Live Simulator controllers
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                                border = BorderStroke(1.dp, Color(0xFF21262D)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = activeAccentColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("BEDSIDE SIMULATOR PANEL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Test active StandBy rules directly inside this AI Studio Emulator.", fontSize = 11.sp, color = Color(0xFF8B949E))

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Hardware simulation criteria
                                    SimulatorSwitchRow(
                                        label = "Device Power Status (Charging)",
                                        isChecked = isSimCharging,
                                        icon = Icons.Default.Power,
                                        activeColor = activeAccentColor,
                                        onCheckedChange = { viewModel.isSimulatedCharging.value = it }
                                    )

                                    SimulatorSwitchRow(
                                        label = "Flat Bedside Angle / Horizontal (Landscape)",
                                        isChecked = isSimLandscape,
                                        icon = Icons.Default.ScreenRotation,
                                        activeColor = activeAccentColor,
                                        onCheckedChange = { viewModel.isSimulatedLandscape.value = it }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Real sensor trigger
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF161B22))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = settings.isActualSensorEnabled,
                                            onCheckedChange = { viewModel.toggleActualSensorEnabled() },
                                            colors = CheckboxDefaults.colors(checkedColor = activeAccentColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("Use Real Hardware Sensors", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Reads actual device charging status (${if (isActCharging) "is charging" else "not charging"}) & landscape (${if (isActLandscape) "yes" else "no"})",
                                                fontSize = 10.sp,
                                                color = Color(0xFF8B949E)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Simulate Ambient Notification Alert", fontSize = 12.sp, color = Color(0xFF8B949E))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = mockNotifTitle,
                                        onValueChange = { mockNotifTitle = it },
                                        label = { Text("Notification Title", fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = activeAccentColor,
                                            unfocusedBorderColor = Color(0xFF30363D)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = mockNotifText,
                                        onValueChange = { mockNotifText = it },
                                        label = { Text("Alert Body Text", fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = activeAccentColor,
                                            unfocusedBorderColor = Color(0xFF30363D)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            viewModel.postSimulatedNotification(
                                                mockNotifTitle,
                                                mockNotifText
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = activeAccentColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("simulate_notif_push_btn")
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Push Simulated Alert", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            // 4. Notes and Tasks manager list (Room Persistence)
                            NotesLogContainer(
                                notes = notes,
                                noteInput = noteInput,
                                onInputChange = { noteInput = it },
                                viewModel = viewModel,
                                activeAccentColor = activeAccentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.LandscapePanelScroll(
    countdown: Int,
    isTimerActive: Boolean,
    activeAccentColor: Color,
    settings: ClockSettings,
    viewModel: StandbyViewModel,
    isSimCharging: Boolean,
    isSimLandscape: Boolean,
    isActCharging: Boolean,
    isActLandscape: Boolean,
    mockNotifTitle: String,
    mockNotifText: String,
    onTitleChange: (String) -> Unit,
    onTextChange: (String) -> Unit
) {
    ScrollableColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Idle auto start countdown card
        AodCountdownBanner(
            countdown = countdown,
            isTimerActive = isTimerActive,
            activeAccentColor = activeAccentColor,
            settings = settings,
            viewModel = viewModel,
            isSimCharging = isSimCharging
        )

        // 2. Dock settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = activeAccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CLOCK DESIGN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("AOD Layout style", fontSize = 11.sp, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LayoutSelectorButton("Dual", "dual_widget", settings.layoutType, activeAccentColor) {
                        viewModel.updateLayoutType("dual_widget")
                    }
                    LayoutSelectorButton("Sweep", "full_analogue", settings.layoutType, activeAccentColor) {
                        viewModel.updateLayoutType("full_analogue")
                    }
                    LayoutSelectorButton("Flip", "flip_clock", settings.layoutType, activeAccentColor) {
                        viewModel.updateLayoutType("flip_clock")
                    }
                    LayoutSelectorButton("Photo", "photo_clock", settings.layoutType, activeAccentColor) {
                        viewModel.updateLayoutType("photo_clock")
                    }
                }

                if (settings.layoutType == "dual_widget") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Widget stacks choices", fontSize = 11.sp, color = Color(0xFF8B949E))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            WidgetDropdownMenu(settings.leftWidgetType, activeAccentColor) {
                                viewModel.updateWidgets(left = it)
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            WidgetDropdownMenu(settings.rightWidgetType, activeAccentColor) {
                                viewModel.updateWidgets(right = it)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dock Aesthetic Theme Accent
                Text("Theme Accent", fontSize = 11.sp, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ThemeColorPill("sunset_orange", Color(0xFFFF5722), settings.clockTheme) { viewModel.updateTheme("sunset_orange") }
                    ThemeColorPill("electric_blue", Color(0xFF00B0FF), settings.clockTheme) { viewModel.updateTheme("electric_blue") }
                    ThemeColorPill("cyber_green", Color(0xFF00E676), settings.clockTheme) { viewModel.updateTheme("cyber_green") }
                    ThemeColorPill("neon_red", Color(0xFFFF1744), settings.clockTheme) { viewModel.updateTheme("neon_red") }
                    ThemeColorPill("minimal_white", Color(0xFFF5F5F5), settings.clockTheme) { viewModel.updateTheme("minimal_white") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FeatureTogglerRow("AOD Night Red Monochrome", settings.isNightModeEnabled) { viewModel.toggleNightMode() }
                FeatureTogglerRow("Show Seconds Display", settings.showSeconds) { viewModel.toggleShowSeconds() }
                FeatureTogglerRow("24h Format", settings.is24HourFormat) { viewModel.toggle24HourFormat() }
            }
        }

        // 3. Bedside Simulator control
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = activeAccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIMULATION SETTINGS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                SimulatorSwitchRow(
                    label = "Charging Attached",
                    isChecked = isSimCharging,
                    icon = Icons.Default.Power,
                    activeColor = activeAccentColor,
                    onCheckedChange = { viewModel.isSimulatedCharging.value = it }
                )

                SimulatorSwitchRow(
                    label = "Horizontal Dock Position",
                    isChecked = isSimLandscape,
                    icon = Icons.Default.ScreenRotation,
                    activeColor = activeAccentColor,
                    onCheckedChange = { viewModel.isSimulatedLandscape.value = it }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161B22))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = settings.isActualSensorEnabled,
                        onCheckedChange = { viewModel.toggleActualSensorEnabled() },
                        colors = CheckboxDefaults.colors(checkedColor = activeAccentColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Actual Hardware Sensors Listeners", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Charging (${if (isActCharging) "ON" else "OFF"}) & Landscape (${if (isActLandscape) "YES" else "NO"})",
                            fontSize = 9.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Simulate Real-time Notification Banner", fontSize = 11.sp, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mockNotifTitle,
                        onValueChange = onTitleChange,
                        label = { Text("Title", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeAccentColor,
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = mockNotifText,
                        onValueChange = onTextChange,
                        label = { Text("Body Text", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeAccentColor,
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.postSimulatedNotification(mockNotifTitle, mockNotifText) },
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Push To AOD Screen", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun AodCountdownBanner(
    countdown: Int,
    isTimerActive: Boolean,
    activeAccentColor: Color,
    settings: ClockSettings,
    viewModel: StandbyViewModel,
    isSimCharging: Boolean
) {
    val isCharging = if (settings.isActualSensorEnabled) viewModel.isActualCharging.collectAsState().value else isSimCharging

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isTimerActive) activeAccentColor.copy(alpha = 0.15f) else Color(0xFF161B22)
        ),
        border = BorderStroke(
            1.dp,
            if (isTimerActive) activeAccentColor else Color(0xFF30363D)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("aod_countdown_card_tag")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = if (isCharging) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCharging) "DOCK IS PLUGGED IN" else "PLUG IN TO ENABLE AUTOSTART",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (isTimerActive) Color(0xFF00E676) else Color(0xFF8B949E),
                            CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isTimerActive) {
                // Large visual countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Standby starts automatically when idle: ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8B949E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(activeAccentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countdown.toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Don't touch the screen for 3 seconds!",
                    fontSize = 10.sp,
                    color = activeAccentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = if (isCharging) {
                        "Standby idle countdown will resume as soon as you stop interacting with the device settings."
                    } else {
                        "Standby engages dynamically when the device is locked, charging, and horizontal."
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun NotesLogContainer(
    notes: List<BedsideNote>,
    noteInput: String,
    onInputChange: (String) -> Unit,
    viewModel: StandbyViewModel,
    activeAccentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
        border = BorderStroke(1.dp, Color(0xFF21262D)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sticky_bedside_notes_panel")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.StickyNote2, contentDescription = null, tint = activeAccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BEDSIDE LOGS DATABASE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Badge(containerColor = activeAccentColor, contentColor = Color.Black) {
                    Text(notes.size.toString(), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Add logs or alarms checkable right on your AOD screen.", fontSize = 11.sp, color = Color(0xFF8B949E))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("Log reminder e.g. Drink water", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeAccentColor,
                        unfocusedBorderColor = Color(0xFF30363D)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bedside_note_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        viewModel.addBedsideNote(noteInput)
                        onInputChange("")
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(activeAccentColor, RoundedCornerShape(12.dp))
                        .testTag("add_bedside_note_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add bedside todo log", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                color = Color(0xFF080B0F),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Empty list.\nWrite sticky alarms above!",
                            fontSize = 12.sp,
                            color = Color(0xFF8B949E),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0D1117))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = note.isCompleted,
                                    onCheckedChange = { viewModel.toggleNoteCompleted(note) },
                                    colors = CheckboxDefaults.colors(checkedColor = activeAccentColor)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = note.content,
                                    fontSize = 13.sp,
                                    color = if (note.isCompleted) Color(0xFF8B949E) else Color.White,
                                    textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { viewModel.deleteNote(note.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete item",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutSelectorButton(
    label: String,
    layoutId: String,
    currentLayoutId: String,
    activeColor: Color,
    onClick: () -> Unit
) {
    val isSelected = layoutId == currentLayoutId
    Surface(
        color = if (isSelected) activeColor else Color(0xFF161B22),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) activeColor else Color(0xFF30363D)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("layout_selector_$layoutId")
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun ThemeColorPill(
    id: String,
    color: Color,
    selectedId: String,
    onClick: () -> Unit
) {
    val isSelected = id == selectedId
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) Color.White else Color.Transparent
                ),
                shape = CircleShape
            )
            .clickable { onClick() }
            .testTag("theme_color_pill_$id")
    )
}

@Composable
fun FeatureTogglerRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() }
        )
    }
}

@Composable
fun SimulatorSwitchRow(
    label: String,
    isChecked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) activeColor else Color(0xFF8B949E),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isChecked) Color.White else Color(0xFF8B949E)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = activeColor)
        )
    }
}

@Composable
fun WidgetDropdownMenu(
    currentValue: String,
    accentColor: Color,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        "digital_clock" to "Digital Clock",
        "calendar" to "Calendar Grid",
        "weather" to "Bedside Climate",
        "notes" to "Sticky Tasks"
    )

    val label = items.firstOrNull { it.first == currentValue }?.second ?: "Digital Clock"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161B22))
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF161B22))
        ) {
            items.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = Color.White, fontSize = 12.sp) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                    modifier = Modifier.testTag("widget_dropdown_$id")
                )
            }
        }
    }
}

private fun symmetricPaddingValues(): PaddingValues {
    return PaddingValues(horizontal = 16.dp, vertical = 8.dp)
}
