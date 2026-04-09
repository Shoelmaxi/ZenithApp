package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.AgendaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {

    // Trae todos los eventos para tener una copia completa si se necesita
    @Query("SELECT * FROM agenda ORDER BY hora ASC")
    fun getAllAgenda(): Flow<List<AgendaItem>>

    // FILTRO CRÍTICO: Trae solo los eventos que contienen el día actual en su lista
    // Room usará el TypeConverter para buscar dentro del JSON de días
    @Query("SELECT * FROM agenda WHERE dias LIKE :diaBusqueda ORDER BY hora ASC")
    fun getAgendaPorDia(diaBusqueda: String): Flow<List<AgendaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItem(item: AgendaItem)

    @Delete
    suspend fun deleteAgendaItem(item: AgendaItem)

    @Update
    suspend fun updateAgendaItem(item: AgendaItem)
}