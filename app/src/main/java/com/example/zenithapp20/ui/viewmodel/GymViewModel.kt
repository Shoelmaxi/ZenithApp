package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.GymDao
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.RutinaDia
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado del entrenamiento activo — vive en el ViewModel, sobrevive a pantalla apagada
data class WorkoutState(
    val ejIdx: Int = 0,
    val serieActual: Int = 1,
    val ejerciciosFinales: List<EjercicioGym> = emptyList()
)

class GymViewModel(private val dao: GymDao) : ViewModel() {

    val todasLasRutinas: StateFlow<List<RutinaDia>> = dao.getAllRutinas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ESTADO DEL ENTRENAMIENTO ACTIVO ---
    private val _workoutState = MutableStateFlow<WorkoutState?>(null)
    val workoutState: StateFlow<WorkoutState?> = _workoutState.asStateFlow()

    fun iniciarEntrenamiento(ejercicios: List<EjercicioGym>) {
        _workoutState.value = WorkoutState(ejerciciosFinales = ejercicios)
    }

    fun actualizarWorkoutState(state: WorkoutState) {
        _workoutState.value = state
    }

    fun finalizarEntrenamiento() {
        _workoutState.value = null
    }

    // --- CRUD RUTINAS ---
    fun guardarRutina(rutina: RutinaDia) {
        viewModelScope.launch {
            val existente = todasLasRutinas.value.find { it.dia == rutina.dia }
            if (existente != null) {
                dao.updateRutina(rutina.copy(id = existente.id))
            } else {
                dao.insertRutina(rutina)
            }
        }
    }

    fun actualizarEjerciciosPostEntreno(dia: String, ejerciciosActualizados: List<EjercicioGym>) {
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
                // Usamos remove() en vez de filter por nombre para no borrar duplicados
                val nuevaLista = it.ejercicios.toMutableList().also { list -> list.remove(ejercicio) }
                dao.updateRutina(it.copy(ejercicios = nuevaLista))
            }
        }
    }
}