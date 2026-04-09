package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.TareasDao
import com.example.zenithapp20.data.model.TareaItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TareasViewModel(private val dao: TareasDao) : ViewModel() {

    // Fecha actual para el filtro (por defecto hoy a las 00:00)
    private val _fechaFiltro = MutableStateFlow(System.currentTimeMillis())

    // Flow que reacciona cada vez que la fecha de la UI cambia
    val tareasFiltradas: StateFlow<List<TareaItem>> = _fechaFiltro
        .flatMapLatest { fecha ->
            dao.getTareasDesdeFecha(fecha)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun cambiarFechaFiltro(nuevaFechaMillis: Long) {
        _fechaFiltro.value = nuevaFechaMillis
    }

    fun guardarOActualizar(tarea: TareaItem) {
        viewModelScope.launch {
            dao.insertTarea(tarea)
        }
    }

    fun toggleCompletada(tarea: TareaItem) {
        viewModelScope.launch {
            dao.updateTarea(tarea.copy(completada = !tarea.completada))
        }
    }

    fun eliminarTarea(tarea: TareaItem) {
        viewModelScope.launch {
            dao.deleteTarea(tarea)
        }
    }
}