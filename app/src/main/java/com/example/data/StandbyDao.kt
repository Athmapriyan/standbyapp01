package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StandbyDao {
    @Query("SELECT * FROM clock_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<ClockSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ClockSettings)

    @Query("SELECT * FROM bedside_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<BedsideNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: BedsideNote)

    @Update
    suspend fun updateNote(note: BedsideNote)

    @Query("DELETE FROM bedside_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}
