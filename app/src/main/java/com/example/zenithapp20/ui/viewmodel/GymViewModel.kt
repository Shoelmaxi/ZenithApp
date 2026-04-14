package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.GymDao
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.RutinaDia
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class WorkoutState(
    val ejIdx: Int = 0,
    val serieActual: Int = 1,
    val ejerciciosFinales: List<EjercicioGym> = emptyList()
)

class GymViewModel(
    private val dao: GymDao,
    private val habitosDao: HabitosDao
) : ViewModel() {

    companion object {
        const val NOMBRE_HABITO_GYM = "Entrenar 💪"
    }

    val todasLasRutinas: StateFlow<List<RutinaDia>> = dao.getAllRutinas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _workoutState = MutableStateFlow<WorkoutState?>(null)
    val workoutState: StateFlow<WorkoutState?> = _workoutState.asStateFlow()

    init {
        viewModelScope.launch {
            asegurarHabitoGymExiste()
        }
    }

    fun iniciarEntrenamiento(ejercicios: List<EjercicioGym>) {
        _workoutState.value = WorkoutState(ejerciciosFinales = ejercicios)
    }

    fun actualizarWorkoutState(state: WorkoutState) {
        _workoutState.value = state
    }

    /**
     * Versión principal: guarda los resultados del entrenamiento
     * Y marca el hábito de gym automáticamente al terminar.
     */
    fun finalizarEntrenamiento(dia: String, ejerciciosActualizados: List<EjercicioGym>) {
        viewModelScope.launch {
            // 1. Persistir resultados (PRs y registros de series)
            val rutinaActual = todasLasRutinas.value.find { it.dia == dia }
            rutinaActual?.let {
                dao.updateRutina(it.copy(ejercicios = ejerciciosActualizados))
            }
            // 2. Marcar hábito automáticamente
            marcarHabitoGymHoy()
            // 3. Limpiar estado del overlay
            _workoutState.value = null
        }
    }

    // Sobrecarga sin parámetros para casos donde solo se necesita limpiar el estado
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
                val nuevaLista = it.ejercicios.toMutableList().also { list -> list.remove(ejercicio) }
                dao.updateRutina(it.copy(ejercicios = nuevaLista))
            }
        }
    }

    // --- LÓGICA HÁBITO GYM ---

    private suspend fun asegurarHabitoGymExiste() {
        val habitos = habitosDao.getAllHabitosSync()
        if (habitos.none { esHabitoGym(it) }) {
            habitosDao.insertHabito(
                Habito(
                    nombre = NOMBRE_HABITO_GYM,
                    meta = "Completar rutina del día",
                    categoria = "Salud",
                    icono = "💪"
                )
            )
        }
    }

    private suspend fun marcarHabitoGymHoy() {
        val hoy = inicioDiaHoy()
        val habitos = habitosDao.getAllHabitosSync()
        val habitoGym = habitos.find { esHabitoGym(it) } ?: return
        val yaCompletado = habitoGym.checks.any { it >= hoy && it < hoy + 86400000 }
        if (!yaCompletado) {
            habitosDao.updateHabito(
                habitoGym.copy(checks = habitoGym.checks + System.currentTimeMillis())
            )
        }
    }

    // Detecta el hábito de gym por palabras clave en el nombre
    fun esHabitoGym(habito: Habito): Boolean {
        val nombre = habito.nombre.lowercase()
        return nombre.contains("entrenar") ||
                nombre.contains("entrenamiento") ||
                nombre.contains("gym") ||
                nombre.contains("ejercicio")
    }

    private fun inicioDiaHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}