package com.example.zenithapp20.ui.viewmodel

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.HabitosDao
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.widget.ZenithWidget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitosViewModel(
    private val dao: HabitosDao,
    private val context: Context
) : ViewModel() {

    val habitos: StateFlow<List<Habito>> = dao.getAllHabitos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarHabito(habito: Habito) {
        viewModelScope.launch {
            dao.insertHabito(habito)
            actualizarWidget()
        }
    }

    fun verificarSiCompletadoEnFecha(habito: Habito, fechaMillis: Long): Boolean {
        val inicioDia = inicioDia(fechaMillis)
        return habito.checks.any { it >= inicioDia && it < inicioDia + 86400000 }
    }

    /**
     * Alterna el check de un hábito en una fecha concreta.
     * Mantiene el comportamiento original para hábitos gestionados (agua, gym).
     */
    fun toggleCheckEnFecha(habito: Habito, fechaMillis: Long) {
        viewModelScope.launch {
            val inicioDia = inicioDia(fechaMillis)
            val nuevosChecks = habito.checks.toMutableList()
            val checkEseDia = nuevosChecks.find { it >= inicioDia && it < inicioDia + 86400000 }

            if (checkEseDia != null) {
                nuevosChecks.remove(checkEseDia)
            } else {
                nuevosChecks.add(inicioDia)
            }

            dao.updateHabito(habito.copy(checks = nuevosChecks))
            actualizarWidget()
        }
    }

    /**
     * Establece explícitamente el estado de completado de un hábito en una fecha.
     * Usado por el sheet AAA: si el usuario dice "Sí lo hice" → marca; "No lo hice" → desmarca.
     */
    fun setCheckEnFecha(habito: Habito, fechaMillis: Long, completado: Boolean) {
        viewModelScope.launch {
            val inicioDia = inicioDia(fechaMillis)
            val nuevosChecks = habito.checks.toMutableList()
            val checkEseDia = nuevosChecks.find { it >= inicioDia && it < inicioDia + 86400000 }

            when {
                completado && checkEseDia == null  -> nuevosChecks.add(inicioDia)
                !completado && checkEseDia != null -> nuevosChecks.remove(checkEseDia)
                // Si ya está en el estado correcto, no hace nada
            }

            dao.updateHabito(habito.copy(checks = nuevosChecks))
            actualizarWidget()
        }
    }

    fun eliminarHabito(habito: Habito) {
        viewModelScope.launch { dao.deleteHabito(habito) }
    }

    private fun actualizarWidget() {
        viewModelScope.launch {
            ZenithWidget().updateAll(context)
        }
    }

    private fun inicioDia(fechaMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = fechaMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}