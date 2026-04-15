package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.dao.LibroDao
import com.example.zenithapp20.data.dao.SesionLecturaDao
import com.example.zenithapp20.data.model.EstadoLibro
import com.example.zenithapp20.data.model.Libro
import com.example.zenithapp20.data.model.SesionLectura
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class LecturaViewModel(
    private val libroDao: LibroDao,
    private val sesionDao: SesionLecturaDao,
    private val habitosDao: HabitosDao
) : ViewModel() {

    // ── Libros ──────────────────────────────────────────────────────────────

    val libros: StateFlow<List<Libro>> = libroDao.getAllLibros()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getLibroById(id: Long): Flow<Libro?> = libroDao.getLibroById(id)

    fun guardarLibro(libro: Libro) {
        viewModelScope.launch { libroDao.insertLibro(libro) }
    }

    fun eliminarLibro(libro: Libro) {
        viewModelScope.launch { libroDao.deleteLibro(libro) }
    }

    fun actualizarEstado(libro: Libro, nuevoEstado: EstadoLibro) {
        viewModelScope.launch {
            libroDao.updateLibro(libro.copy(estado = nuevoEstado))
        }
    }

    // ── Sesiones ─────────────────────────────────────────────────────────────

    fun getSesionesDeLibro(libroId: Long): Flow<List<SesionLectura>> =
        sesionDao.getSesionesByLibro(libroId)

    fun getSesionesConInsight(libroId: Long): Flow<List<SesionLectura>> =
        sesionDao.getSesionesConInsightByLibro(libroId)

    /**
     * Guarda la sesión, actualiza la página actual del libro, dispara el
     * trigger de finalización si corresponde, y marca el hábito de lectura.
     */
    fun guardarSesion(sesion: SesionLectura, libro: Libro) {
        viewModelScope.launch {
            sesionDao.insertSesion(sesion)

            val nuevaPagina = sesion.paginaFin
            val terminado = nuevaPagina >= libro.paginasTotales
            val nuevoEstado = if (terminado) EstadoLibro.TERMINADO else EstadoLibro.LEYENDO
            val ahora = System.currentTimeMillis()

            libroDao.updateLibro(
                libro.copy(
                    paginaActual = nuevaPagina,
                    estado = nuevoEstado,
                    // Primera sesión → se setea fechaInicio
                    fechaInicio = libro.fechaInicio ?: ahora,
                    fechaFin = if (terminado) ahora else libro.fechaFin
                )
            )

            marcarHabitoLecturaHoy()
        }
    }

    fun eliminarSesion(sesion: SesionLectura) {
        viewModelScope.launch { sesionDao.deleteSesion(sesion) }
    }

    // ── Hábito de lectura ─────────────────────────────────────────────────────

    private suspend fun marcarHabitoLecturaHoy() {
        val hoy = inicioDiaHoy()
        val habitos = habitosDao.getAllHabitosSync()
        val habitoLeer = habitos.find {
            val n = it.nombre.lowercase()
            n.contains("leer") || n.contains("lectura") || n.contains("libro")
        } ?: return

        val yaCompletado = habitoLeer.checks.any { it >= hoy && it < hoy + 86400000L }
        if (!yaCompletado) {
            habitosDao.updateHabito(
                habitoLeer.copy(checks = habitoLeer.checks + System.currentTimeMillis())
            )
        }
    }

    private fun inicioDiaHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}