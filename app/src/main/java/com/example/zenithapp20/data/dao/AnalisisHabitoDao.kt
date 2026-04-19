package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.AnalisisHabito
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalisisHabitoDao {
    @Query("SELECT * FROM analisis_habito ORDER BY fechaMillis DESC LIMIT 50")
    fun getAll(): Flow<List<AnalisisHabito>>

    @Insert
    suspend fun insert(analisis: AnalisisHabito)
}