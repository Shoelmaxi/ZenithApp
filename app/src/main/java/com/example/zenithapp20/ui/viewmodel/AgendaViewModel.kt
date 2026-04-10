package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.AgendaDao
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.TipoAgenda
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgendaViewModel(private val dao: AgendaDao) : ViewModel() {

    private val _diaSeleccionado = MutableStateFlow("L") // Por defecto Lunes
    private val _fechaSeleccionadaMillis = MutableStateFlow(System.currentTimeMillis())

    // El flujo de la agenda se actualiza cada vez que cambia el día seleccionado
    val agendaFiltrada: StateFlow<List<AgendaItem>> = combine(
        _diaSeleccionado,
        _fechaSeleccionadaMillis
    ) { dia, fechaMillis ->
        Pair(dia, fechaMillis)
    }.flatMapLatest { (dia, fechaMillis) ->
        dao.getAllAgenda().map { lista ->
            lista.filter { item ->
                when (item.tipo) {
                    TipoAgenda.RECURRENTE -> item.dias.contains(dia)
                    TipoAgenda.FECHA_ESPECIFICA -> {
                        item.fechaEspecificaMillis?.let { fecha ->
                            val inicioDia = java.util.Calendar.getInstance().apply {
                                timeInMillis = fechaMillis
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            fecha >= inicioDia && fecha < inicioDia + 86400000
                        } ?: false
                    }
                }
            }.sortedBy { it.hora }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarDia(nuevoDia: String) {
        _diaSeleccionado.value = nuevoDia
    }
    fun cambiarFecha(fechaMillis: Long) {
        _fechaSeleccionadaMillis.value = fechaMillis
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
    val todaLaAgenda: StateFlow<List<AgendaItem>> = dao.getAllAgenda()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}