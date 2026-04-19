package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.RegistroResiliencia
import kotlinx.coroutines.flow.Flow

@Dao
interface ResilienciaDao {
    @Query("SELECT * FROM registros_resiliencia ORDER BY fechaDia DESC")
    fun getAll(): Flow<List<RegistroResiliencia>>

    @Query("SELECT * FROM registros_resiliencia WHERE fechaDia = :dia LIMIT 1")
    suspend fun getDeHoy(dia: Long): RegistroResiliencia?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroResiliencia)

    @Update
    suspend fun update(registro: RegistroResiliencia)
}