package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.ReflexionDiaria
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflexionDao {
    @Query("SELECT * FROM reflexiones_diarias ORDER BY fechaMillis DESC")
    fun getAll(): Flow<List<ReflexionDiaria>>

    @Query("SELECT * FROM reflexiones_diarias WHERE fechaMillis >= :inicioDia AND fechaMillis < :finDia LIMIT 1")
    suspend fun getDeHoy(inicioDia: Long, finDia: Long): ReflexionDiaria?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reflexion: ReflexionDiaria)
}