package com.example.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BedsideNote
import com.example.data.ClockSettings
import com.example.data.StandbyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenState {
    DASHBOARD, // Configure widgets, simulator options, notes database
    AOD_STAGES, // Active Standby Screen Saver Mode
}

data class SimulatedNotification(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val title: String,
    val text: String,
    val timeReceived: String,
    val iconName: String = "sms"
)

class StandbyViewModel(
    private val application: Application,
    private val repository: StandbyRepository
) : AndroidViewModel(application) {

    // Persistent State
    val settings: StateFlow<ClockSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockSettings())

    val bedsideNotes: StateFlow<List<BedsideNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulator states for AI Studio Streaming Emulator & device compatibility
    val isSimulatedCharging = MutableStateFlow(true) // Start with true so it engages immediately on load
    val isSimulatedLandscape = MutableStateFlow(true) // StandBy defaults to landscape view

    val isActualCharging = MutableStateFlow(false)
    val isActualLandscape = MutableStateFlow(false)

    // Current screen layout
    val screenState = MutableStateFlow(ScreenState.DASHBOARD)

    // Idle countdown state (the 3 seconds rule!)
    val idleCountdown = MutableStateFlow(3)
    val isAutostartTimerRunning = MutableStateFlow(false)
    val lastTouchTime = MutableStateFlow(System.currentTimeMillis())

    // Ambient Sensor dim level (Simulates dim bedside light level to trigger monochrome Red iOS mode!)
    val isBedsideAmbientDim = MutableStateFlow(false)

    // Temporary Notifications Queue for AOD Display
    private val _notifications = MutableStateFlow<List<SimulatedNotification>>(emptyList())
    val notifications: StateFlow<List<SimulatedNotification>> = _notifications.asStateFlow()

    // Battery percentage (actual)
    val actualBatteryPct = MutableStateFlow(100)

    private var countdownJob: Job? = null
    private var actualStateReceiver: BroadcastReceiver? = null

    init {
        // Initial actual sensor check
        checkActualSensors()
        // Register receivers for real charging & power
        registerSystemReceivers()
        // Start monitoring interaction for the 3-second idle countdown!
        startIdleTimerMonitoring()
    }

    private fun checkActualSensors() {
        // Check battery charging status
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = application.registerReceiver(null, intentFilter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        isActualCharging.value = isCharging

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) {
            actualBatteryPct.value = (level * 100 / scale)
        }

        // Check if device orientation is landscape
        val orientation = application.resources.configuration.orientation
        isActualLandscape.value = orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun registerSystemReceivers() {
        actualStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        isActualCharging.value = true
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        isActualCharging.value = false
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        isActualCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        if (level >= 0 && scale > 0) {
                            actualBatteryPct.value = (level * 100 / scale)
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        application.registerReceiver(actualStateReceiver, filter)
    }

    // Called on Configuration Changes in MainActivity
    fun onConfigurationChanged(newConfig: Configuration) {
        isActualLandscape.value = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun resetTouch() {
        lastTouchTime.value = System.currentTimeMillis()
        // If we were in AOD stage, we might awake on touch
        if (screenState.value == ScreenState.AOD_STAGES) {
            screenState.value = ScreenState.DASHBOARD
        }
        idleCountdown.value = 3
    }

    private fun startIdleTimerMonitoring() {
        viewModelScope.launch {
            while (true) {
                delay(200)
                val settingsVal = settings.value
                val isCharging = if (settingsVal.isActualSensorEnabled) isActualCharging.value else isSimulatedCharging.value
                val isLandscape = if (settingsVal.isActualSensorEnabled) isActualLandscape.value else isSimulatedLandscape.value

                // StandBy triggers when phone is:
                // 1. Charging
                // 2. Landscape (iOS rules, though we adapt to portrait too if chosen, but default requires charging + idle!)
                // 3. User is idle (3 seconds after lastTouchTime)
                if (screenState.value == ScreenState.DASHBOARD && isCharging) {
                    val timeSinceTouch = System.currentTimeMillis() - lastTouchTime.value
                    if (timeSinceTouch >= 3000) {
                        idleCountdown.value = 0
                        isAutostartTimerRunning.value = false
                        // Automatically transit to AOD screen!
                        screenState.value = ScreenState.AOD_STAGES
                    } else {
                        // Countdown active
                        isAutostartTimerRunning.value = true
                        val remainingSec = 3 - (timeSinceTouch / 1000).toInt()
                        idleCountdown.value = remainingSec.coerceAtLeast(0)
                    }
                } else {
                    isAutostartTimerRunning.value = false
                    idleCountdown.value = 3
                }
            }
        }
    }

    // Interaction handlers
    fun forceLaunchStandby() {
        screenState.value = ScreenState.AOD_STAGES
    }

    fun exitStandby() {
        screenState.value = ScreenState.DASHBOARD
        lastTouchTime.value = System.currentTimeMillis()
    }

    fun updateTheme(themeName: String) {
        viewModelScope.launch {
            repository.saveSettings(settings.value.copy(clockTheme = themeName))
        }
    }

    fun updateLayoutType(layoutName: String) {
        viewModelScope.launch {
            repository.saveSettings(settings.value.copy(layoutType = layoutName))
        }
    }

    fun updateWidgets(left: String? = null, right: String? = null) {
        viewModelScope.launch {
            var curr = settings.value
            if (left != null) curr = curr.copy(leftWidgetType = left)
            if (right != null) curr = curr.copy(rightWidgetType = right)
            repository.saveSettings(curr)
        }
    }

    fun toggleNightMode() {
        viewModelScope.launch {
            repository.saveSettings(settings.value.copy(isNightModeEnabled = !settings.value.isNightModeEnabled))
        }
    }

    fun toggleShowSeconds() {
        viewModelScope.launch {
            repository.saveSettings(settings.value.copy(showSeconds = !settings.value.showSeconds))
        }
    }

    fun toggle24HourFormat() {
        viewModelScope.launch {
            repository.saveSettings(settings.value.copy(is24HourFormat = !settings.value.is24HourFormat))
        }
    }

    fun toggleActualSensorEnabled() {
        viewModelScope.launch {
            repository.saveSettings(settings.value.copy(isActualSensorEnabled = !settings.value.isActualSensorEnabled))
        }
    }

    // --- Notes operations ---
    fun addBedsideNote(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.insertNote(BedsideNote(content = content))
        }
    }

    fun toggleNoteCompleted(note: BedsideNote) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isCompleted = !note.isCompleted))
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }

    // --- Notification Simulation ---
    fun postSimulatedNotification(title: String, text: String) {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        val timeStr = formatter.format(java.util.Date())
        val icons = listOf("chat", "mail", "alarm", "favorite", "calendar_today")
        val notification = SimulatedNotification(
            title = title,
            text = text,
            timeReceived = timeStr,
            iconName = icons.random()
        )
        _notifications.update { listOf(notification) + it }

        // Auto-dismiss notification from display in 6 seconds
        viewModelScope.launch {
            delay(6000)
            _notifications.update { it.filter { n -> n.id != notification.id } }
        }
    }

    fun dismissNotification(id: Long) {
        _notifications.update { it.filter { n -> n.id != id } }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            if (actualStateReceiver != null) {
                application.unregisterReceiver(actualStateReceiver)
            }
        } catch (e: Exception) {
            // Receiver already unregistered or not registered
        }
    }
}

class StandbyViewModelFactory(
    private val application: Application,
    private val repository: StandbyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StandbyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StandbyViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
