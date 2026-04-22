package com.example.zenithapp20.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.GymDao
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.RutinaDia
import com.example.zenithapp20.utils.WorkoutPersistenceManager
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
    private val habitosDao: HabitosDao,
    private val context: Context
) : ViewModel() {

    companion object {
        const val NOMBRE_HABITO_GYM = "Entrenar 💪"
    }

    val todasLasRutinas: StateFlow<List<RutinaDia>> = dao.getAllRutinas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Estado del entrenamiento activo ───────────────────────────────────
    private val _workoutState = MutableStateFlow<WorkoutState?>(null)
    val workoutState: StateFlow<WorkoutState?> = _workoutState.asStateFlow()

    // ── Sesión guardada pendiente de reanudar ─────────────────────────────
    private val _savedWorkoutState = MutableStateFlow<WorkoutState?>(null)
    val savedWorkoutState: StateFlow<WorkoutState?> = _savedWorkoutState.asStateFlow()

    init {
        viewModelScope.launch {
            asegurarHabitoGymExiste()
            // Detectar sesión interrumpida al iniciar
            val saved = WorkoutPersistenceManager.load(context)
            if (saved != null) {
                _savedWorkoutState.value = saved
            }
        }
    }

    // ── Iniciar / Reanudar ────────────────────────────────────────────────

    fun iniciarEntrenamiento(ejercicios: List<EjercicioGym>) {
        // Limpia la sesión guardada si había una pendiente
        WorkoutPersistenceManager.clear(context)
        _savedWorkoutState.value = null
        val state = WorkoutState(ejerciciosFinales = ejercicios)
        _workoutState.value = state
        WorkoutPersistenceManager.save(context, state)
    }

    fun reanudarEntrenamientoGuardado() {
        val saved = _savedWorkoutState.value ?: return
        _workoutState.value = saved
        _savedWorkoutState.value = null
    }

    fun descartarEntrenamientoGuardado() {
        WorkoutPersistenceManager.clear(context)
        _savedWorkoutState.value = null
    }

    // ── Actualizar estado (llamado desde el overlay en cada cambio) ───────

    fun actualizarWorkoutState(state: WorkoutState) {
        _workoutState.value = state
        WorkoutPersistenceManager.save(context, state)
    }

    // ── Finalizar ─────────────────────────────────────────────────────────

    fun finalizarEntrenamiento(dia: String, ejerciciosActualizados: List<EjercicioGym>) {
        viewModelScope.launch {
            val rutinaActual = todasLasRutinas.value.find { it.dia == dia }
            rutinaActual?.let { rutina ->
                // Actualiza pesoAnterior de cada ejercicio con el máximo de esta sesión
                val ejerciciosConPR = ejerciciosActualizados.map { ej ->
                    val maxPeso = ej.registrosRealizados
                        .mapNotNull { it.peso.toDoubleOrNull() }
                        .maxOrNull()
                    if (maxPeso != null && maxPeso > 0) {
                        ej.copy(
                            pesoAnterior = maxPeso.toInt().toString(),
                            // Guardamos los registros para que la preview muestre la última sesión
                            registrosRealizados = ej.registrosRealizados
                        )
                    } else {
                        ej
                    }
                }
                dao.updateRutina(rutina.copy(ejercicios = ejerciciosConPR))
            }
            marcarHabitoGymHoy()
            WorkoutPersistenceManager.clear(context)
            _workoutState.value = null
        }
    }

    // ── CRUD Rutinas ──────────────────────────────────────────────────────

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

    fun eliminarEjercicio(dia: String, ejercicio: EjercicioGym) {
        viewModelScope.launch {
            val rutinaActual = todasLasRutinas.value.find { it.dia == dia }
            rutinaActual?.let {
                val nuevaLista = it.ejercicios.toMutableList().also { l -> l.remove(ejercicio) }
                dao.updateRutina(it.copy(ejercicios = nuevaLista))
            }
        }
    }

    // ── Hábito gym ────────────────────────────────────────────────────────

    private suspend fun asegurarHabitoGymExiste() {
        val habitos = habitosDao.getAllHabitosSync()
        if (habitos.none { esHabitoGym(it) }) {
            habitosDao.insertHabito(
                Habito(
                    nombre    = NOMBRE_HABITO_GYM,
                    meta      = "Completar rutina del día",
                    categoria = "Salud",
                    icono     = "💪"
                )
            )
        }
    }

    private suspend fun marcarHabitoGymHoy() {
        val hoy = inicioDiaHoy()
        val habitos = habitosDao.getAllHabitosSync()
        val habitoGym = habitos.find { esHabitoGym(it) } ?: return
        if (habitoGym.checks.none { it >= hoy && it < hoy + 86400000 }) {
            habitosDao.updateHabito(
                habitoGym.copy(checks = habitoGym.checks + System.currentTimeMillis())
            )
        }
    }

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