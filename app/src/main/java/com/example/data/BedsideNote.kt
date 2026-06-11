package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bedside_notes")
data class BedsideNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
