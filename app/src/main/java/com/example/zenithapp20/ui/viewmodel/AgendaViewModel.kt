package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.AgendaDao
import com.example.zenithapp20.data.model.AgendaItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgendaViewModel(private val dao: AgendaDao) : ViewModel() {

    private val _diaSeleccionado = MutableStateFlow("L") // Por defecto Lunes

    // El flujo de la agenda se actualiza cada vez que cambia el día seleccionado
    val agendaFiltrada: StateFlow<List<AgendaItem>> = _diaSeleccionado
        .flatMapLatest { dia ->
            // Buscamos el día dentro del JSON que guarda Room
            dao.getAgendaPorDia("%$dia%")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarDia(nuevoDia: String) {
        _diaSeleccionado.value = nuevoDia
    }

    fun guardarOActualizar(item: AgendaItem) {
        viewModelScope.launch {
            dao.insertAgendaItem(item) // REPLACE se encarga si el ID ya existe
        }
    }

    fun toggleCompletado(item: AgendaItem, diaActual: String) {
        viewModelScope.launch {
            val nuevos = item.diasCompletados.toMutableList()
            if (nuevos.contains(diaActual)) {
                nuevos.remove(diaActual)
            } else {
                nuevos.add(diaActual)
            }
            dao.updateAgendaItem(item.copy(diasCompletados = nuevos))
        }
    }

    fun eliminarItem(item: AgendaItem) {
        viewModelScope.launch {
            dao.deleteAgendaItem(item)
        }
    }
}