package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clock_settings")
data class ClockSettings(
    @PrimaryKey val id: Int = 1,
    val clockTheme: String = "sunset_orange", // "sunset_orange", "electric_blue", "cyber_green", "neon_red", "minimal_white"
    val layoutType: String = "dual_widget", // "dual_widget", "full_analogue", "flip_clock", "photo_clock"
    val leftWidgetType: String = "digital_clock", // "digital_clock", "calendar", "weather", "notes"
    val rightWidgetType: String = "calendar", // "digital_clock", "calendar", "weather", "notes"
    val isNightModeEnabled: Boolean = true, // Turns monochrome red in ultra-dim environments
    val showSeconds: Boolean = true,
    val is24HourFormat: Boolean = false,
    val isActualSensorEnabled: Boolean = false // If true, listen to actual battery & orientation intents
)
