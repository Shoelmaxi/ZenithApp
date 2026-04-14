package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.AguaDao
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.model.AguaRegistro
import com.example.zenithapp20.data.model.Habito
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AguaViewModel(
    private val aguaDao: AguaDao,
    private val habitosDao: HabitosDao
) : ViewModel() {

    companion object {
        const val META_ML = 2000
        const val ML_POR_VASO = 250
        // Nombre exacto del hábito gestionado automáticamente
        const val NOMBRE_HABITO_AGUA = "Tomar agua 💧"
    }

    private fun inicioDiaHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val registrosHoy: StateFlow<List<AguaRegistro>> = aguaDao
        .getRegistrosDeHoy(inicioDiaHoy())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMlHoy: StateFlow<Int> = registrosHoy
        .map { lista -> lista.sumOf { it.cantidadMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val metaAlcanzada: StateFlow<Boolean> = totalMlHoy
        .map { it >= META_ML }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Al iniciar el ViewModel, nos aseguramos de que el hábito de agua exista
    init {
        viewModelScope.launch {
            asegurarHabitoAguaExiste()
        }
    }

    fun agregarVaso(ml: Int = ML_POR_VASO) {
        viewModelScope.launch {
            aguaDao.insertRegistro(AguaRegistro(cantidadMl = ml))
            sincronizarHabitoConMeta()
        }
    }

    fun quitarUltimoVaso() {
        viewModelScope.launch {
            val ultimo = registrosHoy.value.lastOrNull()
            ultimo?.let {
                aguaDao.deleteRegistro(it)
                sincronizarHabitoConMeta()
            }
        }
    }

    /**
     * Crea el hábito de agua si no existe todavía en la base de datos.
     * Solo se ejecuta una vez al iniciar el ViewModel.
     */
    private suspend fun asegurarHabitoAguaExiste() {
        val habitos = habitosDao.getAllHabitosSync()
        val yaExiste = habitos.any { esHabitoAgua(it) }
        if (!yaExiste) {
            habitosDao.insertHabito(
                Habito(
                    nombre = NOMBRE_HABITO_AGUA,
                    meta = "${META_ML / 1000}L al día",
                    categoria = "Salud",
                    icono = "💧"
                )
            )
        }
    }

    /**
     * Sincroniza el check del hábito de agua con el estado actual del medidor:
     * - Si se alcanzó la meta y el hábito NO está marcado → lo marca.
     * - Si se quitó agua y ya no llega a la meta pero el hábito SÍ está marcado → lo desmarca.
     */
    private suspend fun sincronizarHabitoConMeta() {
        val totalActual = aguaDao.getTotalMlHoy(inicioDiaHoy()) ?: 0
        val metaCumplida = totalActual >= META_ML
        val hoy = inicioDiaHoy()

        val habitos = habitosDao.getAllHabitosSync()
        val habitoAgua = habitos.find { esHabitoAgua(it) } ?: return

        val yaCompletadoHoy = habitoAgua.checks.any { it >= hoy && it < hoy + 86400000 }

        when {
            metaCumplida && !yaCompletadoHoy -> {
                // Meta alcanzada → marcar
                habitosDao.updateHabito(
                    habitoAgua.copy(checks = habitoAgua.checks + System.currentTimeMillis())
                )
            }
            !metaCumplida && yaCompletadoHoy -> {
                // Meta ya no alcanzada (quitaron un vaso) → desmarcar
                val checksActualizados = habitoAgua.checks.filter {
                    it < hoy || it >= hoy + 86400000
                }
                habitosDao.updateHabito(habitoAgua.copy(checks = checksActualizados))
            }
        }
    }

    private fun esHabitoAgua(habito: Habito): Boolean {
        val nombre = habito.nombre.lowercase()
        return nombre.contains("agua") || nombre.contains("hidrat")
    }
}