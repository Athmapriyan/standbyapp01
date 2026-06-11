package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StandbyRepository(private val standbyDao: StandbyDao) {

    val settings: Flow<ClockSettings> = standbyDao.getSettings().map { it ?: ClockSettings() }
    val allNotes: Flow<List<BedsideNote>> = standbyDao.getAllNotes()

    suspend fun saveSettings(clockSettings: ClockSettings) {
        standbyDao.insertSettings(clockSettings)
    }

    suspend fun insertNote(note: BedsideNote) {
        standbyDao.insertNote(note)
    }

    suspend fun updateNote(note: BedsideNote) {
        standbyDao.updateNote(note)
    }

    suspend fun deleteNoteById(id: Int) {
        standbyDao.deleteNoteById(id)
    }
}
