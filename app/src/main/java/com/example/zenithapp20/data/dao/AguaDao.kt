package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.AguaRegistro
import kotlinx.coroutines.flow.Flow

@Dao
interface AguaDao {
    @Query("SELECT * FROM agua WHERE fechaMillis >= :inicioDia ORDER BY fechaMillis ASC")
    fun getRegistrosDeHoy(inicioDia: Long): Flow<List<AguaRegistro>>

    @Insert
    suspend fun insertRegistro(registro: AguaRegistro)

    @Delete
    suspend fun deleteRegistro(registro: AguaRegistro)

    @Query("SELECT SUM(cantidadMl) FROM agua WHERE fechaMillis >= :inicioDia")
    suspend fun getTotalMlHoy(inicioDia: Long): Int?
}