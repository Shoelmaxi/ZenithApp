package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.RutinaDia
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {
    @Query("SELECT * FROM rutinas")
    fun getAllRutinas(): Flow<List<RutinaDia>>

    @Query("SELECT * FROM rutinas WHERE dia = :dia LIMIT 1")
    fun getRutinaPorDia(dia: String): Flow<RutinaDia?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRutina(rutina: RutinaDia)

    @Update
    suspend fun updateRutina(rutina: RutinaDia)
}