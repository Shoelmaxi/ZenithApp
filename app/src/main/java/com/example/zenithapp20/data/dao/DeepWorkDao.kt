package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.SesionDeepWork
import kotlinx.coroutines.flow.Flow

@Dao
interface DeepWorkDao {
    @Query("SELECT * FROM sesiones_deep_work ORDER BY fechaMillis DESC")
    fun getAll(): Flow<List<SesionDeepWork>>

    @Insert
    suspend fun insert(sesion: SesionDeepWork)
}