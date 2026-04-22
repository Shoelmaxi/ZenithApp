package com.example.zenithapp20.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.zenithapp20.data.dao.*
import com.example.zenithapp20.data.model.*
import com.example.zenithapp20.ui.utils.NotificacionSuenoWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class IngenieriaConductualViewModel(
    private val analisisDao: AnalisisHabitoDao,
    private val deepWorkDao: DeepWorkDao,
    private val resilienciaDao: ResilienciaDao,
    private val context: Context
) : ViewModel() {

    // ── AAA ──────────────────────────────────────────────────────────────────
    val analisis: StateFlow<List<AnalisisHabito>> = analisisDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarAnalisis(analisis: AnalisisHabito) {
        viewModelScope.launch { analisisDao.insert(analisis) }
    }

    // ── Deep Work ─────────────────────────────────────────────────────────────
    val sesionesDeepWork: StateFlow<List<SesionDeepWork>> = deepWorkDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarSesionDeepWork(duracionObjetivoMin: Int, duracionRealSegundos: Long, distracciones: Int) {
        val calidad = duracionRealSegundos.toFloat() / (distracciones + 1)
        viewModelScope.launch {
            deepWorkDao.insert(
                SesionDeepWork(
                    duracionObjetivoMin    = duracionObjetivoMin,
                    duracionRealSegundos   = duracionRealSegundos,
                    distracciones          = distracciones,
                    calidadSesion          = calidad
                )
            )
        }
    }

    // ── Resiliencia ───────────────────────────────────────────────────────────
    val registrosResiliencia: StateFlow<List<RegistroResiliencia>> = resilienciaDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _registroHoy = MutableStateFlow<RegistroResiliencia?>(null)
    val registroHoy: StateFlow<RegistroResiliencia?> = _registroHoy.asStateFlow()

    val rachaPoder: StateFlow<Float> = registrosResiliencia
        .map { lista -> calcularRachaPoder(lista) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init {
        viewModelScope.launch { cargarRegistroHoy() }
    }

    private suspend fun cargarRegistroHoy() {
        val hoy = inicioDiaHoy()
        var r = resilienciaDao.getDeHoy(hoy)
        if (r == null) {
            resilienciaDao.insert(RegistroResiliencia(fechaDia = hoy))
            r = resilienciaDao.getDeHoy(hoy)
        }
        _registroHoy.value = r
    }

    private suspend fun actualizarRegistro(updated: RegistroResiliencia) {
        resilienciaDao.update(updated)
        _registroHoy.value = updated
    }

    fun toggleDuchaFria() {
        viewModelScope.launch {
            _registroHoy.value?.let { actualizarRegistro(it.copy(duchaFria = !it.duchaFria)) }
        }
    }

    fun toggleAyunoDopamina() {
        viewModelScope.launch {
            _registroHoy.value?.let { actualizarRegistro(it.copy(ayunoDopamina = !it.ayunoDopamina)) }
        }
    }

    fun toggleEntrenamiento() {
        viewModelScope.launch {
            _registroHoy.value?.let {
                actualizarRegistro(it.copy(entrenamientoResistencia = !it.entrenamientoResistencia))
            }
        }
    }

    /**
     * Día perfecto → racha + 1
     * Día fallido   → racha × 0.5 (nunca a 0, mantiene impulso psicológico)
     */
    private fun calcularRachaPoder(registros: List<RegistroResiliencia>): Float {
        var racha = 0f
        registros.sortedBy { it.fechaDia }.forEach { registro ->
            racha = if (registro.diaPerfecto) racha + 1f else maxOf(racha * 0.5f, 0f)
        }
        return racha
    }

    // ── Sleep Calculator ──────────────────────────────────────────────────────
    /** Retorna lista de Triple(hora, minuto, ciclos) para dormir */
    fun calcularHorasDormir(horaDespertar: Int, minDespertar: Int): List<Triple<Int, Int, Int>> {
        return listOf(6, 5, 4).map { ciclos ->
            val totalMin = ciclos * 90 + 15
            var minTotales = horaDespertar * 60 + minDespertar - totalMin
            if (minTotales < 0) minTotales += 24 * 60
            Triple((minTotales / 60) % 24, minTotales % 60, ciclos)
        }
    }

    /** Programa notificación 60 min antes de la hora de dormir elegida */
    fun programarNotificacionSueno(horaDormir: Int, minDormir: Int) {
        val ahora = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, horaDormir)
            set(Calendar.MINUTE, minDormir)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE, -60)
        }
        if (ahora.after(objetivo)) objetivo.add(Calendar.DAY_OF_YEAR, 1)
        val delay = objetivo.timeInMillis - ahora.timeInMillis

        WorkManager.getInstance(context).enqueueUniqueWork(
            "notif_sueno",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NotificacionSuenoWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
        )
    }

    private fun inicioDiaHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}