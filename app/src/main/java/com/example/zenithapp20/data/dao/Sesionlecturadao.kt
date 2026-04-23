package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.SesionLectura
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionLecturaDao {

    @Query("SELECT * FROM sesiones_lectura WHERE libroId = :libroId ORDER BY fecha DESC")
    fun getSesionesByLibro(libroId: Long): Flow<List<SesionLectura>>

    // Para El Oráculo — lectura directa sin Flow
    @Query("SELECT * FROM sesiones_lectura WHERE libroId = :libroId ORDER BY fecha DESC")
    suspend fun getSesionesByLibroSync(libroId: Long): List<SesionLectura>

    @Query("""
        SELECT * FROM sesiones_lectura
        WHERE libroId = :libroId
          AND (leccionClave != '' OR aplicacionEstrategica != '')
        ORDER BY fecha DESC
    """)
    fun getSesionesConInsightByLibro(libroId: Long): Flow<List<SesionLectura>>

    @Query("SELECT SUM(paginaFin - paginaInicio) FROM sesiones_lectura")
    suspend fun getTotalPaginasLeidas(): Int?

    @Query("SELECT COUNT(*) FROM sesiones_lectura WHERE fecha >= :inicioSemana")
    suspend fun getSesionesDesde(inicioSemana: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSesion(sesion: SesionLectura)

    @Delete
    suspend fun deleteSesion(sesion: SesionLectura)
}