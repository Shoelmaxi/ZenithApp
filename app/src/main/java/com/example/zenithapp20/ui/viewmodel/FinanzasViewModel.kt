package com.example.zenithapp20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithapp20.data.dao.FinanzasDao
import com.example.zenithapp20.data.model.TipoTransaccion
import com.example.zenithapp20.data.model.Transaccion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanzasViewModel(private val dao: FinanzasDao) : ViewModel() {

    // 1. Estado de la lista de transacciones
    val transacciones: StateFlow<List<Transaccion>> = dao.getAllTransacciones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Estados derivados (Balance, Ingresos, Egresos)
    // Usamos 'map' sobre el flujo de transacciones para calcular valores automáticamente
    val ingresos: StateFlow<Int> = transacciones
        .map { lista -> lista.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val egresos: StateFlow<Int> = transacciones
        .map { lista -> lista.filter { it.tipo == TipoTransaccion.EGRESO }.sumOf { it.monto } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val balance: StateFlow<Int> = combine(ingresos, egresos) { inc, out -> inc - out }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 3. Acciones
    fun guardarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            dao.insertTransaccion(transaccion)
        }
    }

    fun eliminarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            dao.deleteTransaccion(transaccion)
        }
    }
}