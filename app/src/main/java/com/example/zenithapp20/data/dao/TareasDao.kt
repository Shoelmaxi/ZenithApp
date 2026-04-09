package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.TareaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TareasDao {

    // Trae las tareas que vencen a partir de una fecha específica
    // Orden: Primero las urgentes, luego por estado (pendientes arriba)
    @Query("SELECT * FROM tareas WHERE fechaLimiteMillis >= :fechaInicio ORDER BY completada ASC, prioridad DESC")
    fun getTareasDesdeFecha(fechaInicio: Long): Flow<List<TareaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaItem)

    @Update
    suspend fun updateTarea(tarea: TareaItem)

    @Delete
    suspend fun deleteTarea(tarea: TareaItem)
}