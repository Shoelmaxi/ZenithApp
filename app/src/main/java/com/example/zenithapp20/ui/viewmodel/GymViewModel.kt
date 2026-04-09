package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.GymDao
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.RutinaDia
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GymViewModel(private val dao: GymDao) : ViewModel() {

    // Todas las rutinas para referencia rápida
    val todasLasRutinas: StateFlow<List<RutinaDia>> = dao.getAllRutinas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarRutina(rutina: RutinaDia) {
        viewModelScope.launch {
            // Buscamos si ya existe una rutina para ese día para mantener el ID y actualizar
            val existente = todasLasRutinas.value.find { it.dia == rutina.dia }
            if (existente != null) {
                dao.updateRutina(rutina.copy(id = existente.id))
            } else {
                dao.insertRutina(rutina)
            }
        }
    }

    fun actualizarEjerciciosPostEntreno(dia: String, ejerciciosActualizados: List<com.example.zenithapp20.data.model.EjercicioGym>) {
        viewModelScope.launch {
            val rutinaActual = todasLasRutinas.value.find { it.dia == dia }
            rutinaActual?.let {
                dao.updateRutina(it.copy(ejercicios = ejerciciosActualizados))
            }
        }
    }

    fun eliminarEjercicio(dia: String, ejercicio: EjercicioGym) {
        viewModelScope.launch {
            val rutinaActual = todasLasRutinas.value.find { it.dia == dia }
            rutinaActual?.let {
                val nuevaLista = it.ejercicios.filter { e -> e.nombre != ejercicio.nombre }
                dao.updateRutina(it.copy(ejercicios = nuevaLista))
            }
        }
    }
}