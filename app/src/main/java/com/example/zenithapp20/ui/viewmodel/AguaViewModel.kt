package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.AguaDao
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.model.AguaRegistro
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

    fun agregarVaso(ml: Int = ML_POR_VASO) {
        viewModelScope.launch {
            aguaDao.insertRegistro(AguaRegistro(cantidadMl = ml))
            verificarMetaYActualizarHabito()
        }
    }

    fun quitarUltimoVaso() {
        viewModelScope.launch {
            val ultimo = registrosHoy.value.lastOrNull()
            ultimo?.let { aguaDao.deleteRegistro(it) }
        }
    }

    private suspend fun verificarMetaYActualizarHabito() {
        val total = aguaDao.getTotalMlHoy(inicioDiaHoy()) ?: 0
        if (total < META_ML) return

        // busca un hábito de agua y lo marca como completado hoy
        val habitos = habitosDao.getAllHabitosSync()
        val habitoAgua = habitos.find { habito ->
            habito.nombre.lowercase().contains("agua") ||
                    habito.nombre.lowercase().contains("hidrat")
        }

        habitoAgua?.let { habito ->
            val hoy = inicioDiaHoy()
            val yaCompletado = habito.checks.any { it >= hoy && it < hoy + 86400000 }
            if (!yaCompletado) {
                habitosDao.updateHabito(
                    habito.copy(checks = habito.checks + System.currentTimeMillis())
                )
            }
        }
    }
}