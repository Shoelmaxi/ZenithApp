package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.model.Habito
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitosViewModel(private val dao: HabitosDao) : ViewModel() {

    val habitos: StateFlow<List<Habito>> = dao.getAllHabitos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarHabito(habito: Habito) {
        viewModelScope.launch { dao.insertHabito(habito) }
    }

    fun toggleCheckHoy(habito: Habito) {
        viewModelScope.launch {
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0);
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val nuevosChecks = habito.checks.toMutableList()
            // Buscamos si ya existe un check hoy (margen de 24h)
            val checkHoy = nuevosChecks.find { it >= hoy && it < hoy + 86400000 }

            if (checkHoy != null) {
                nuevosChecks.remove(checkHoy)
            } else {
                nuevosChecks.add(System.currentTimeMillis())
            }

            dao.updateHabito(habito.copy(checks = nuevosChecks))
        }
    }

    fun verificarSiCompletadoHoy(habito: Habito): Boolean {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0);
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return habito.checks.any { it >= hoy && it < hoy + 86400000 }
    }
}