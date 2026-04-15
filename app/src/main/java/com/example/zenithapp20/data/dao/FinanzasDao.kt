package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.Transaccion
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanzasDao {
    // Obtenemos todos los movimientos ordenados por fecha
    @Query("SELECT * FROM finanzas ORDER BY fechaMillis DESC")
    fun getAllTransacciones(): Flow<List<Transaccion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaccion(transaccion: Transaccion)

    @Delete
    suspend fun deleteTransaccion(transaccion: Transaccion)

    // En FinanzasDao.kt — agrega esta línea
    @Query("SELECT * FROM finanzas")
    suspend fun getAllTransaccionesSync(): List<Transaccion>
}