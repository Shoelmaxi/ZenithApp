package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.SesionLectura
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionLecturaDao {

    @Query("SELECT * FROM sesiones_lectura WHERE libroId = :libroId ORDER BY fecha DESC")
    fun getSesionesByLibro(libroId: Long): Flow<List<SesionLectura>>

    // Solo sesiones con lección o estrategia — para el "Filtro de Auditoría"
    @Query("""
        SELECT * FROM sesiones_lectura
        WHERE libroId = :libroId
          AND (leccionClave != '' OR aplicacionEstrategica != '')
        ORDER BY fecha DESC
    """)
    fun getSesionesConInsightByLibro(libroId: Long): Flow<List<SesionLectura>>

    // Total de páginas leídas en toda la app (para stats)
    @Query("SELECT SUM(paginaFin - paginaInicio) FROM sesiones_lectura")
    suspend fun getTotalPaginasLeidas(): Int?

    // Sesiones de esta semana (para stats)
    @Query("SELECT COUNT(*) FROM sesiones_lectura WHERE fecha >= :inicioSemana")
    suspend fun getSesionesDesde(inicioSemana: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSesion(sesion: SesionLectura)

    @Delete
    suspend fun deleteSesion(sesion: SesionLectura)
}