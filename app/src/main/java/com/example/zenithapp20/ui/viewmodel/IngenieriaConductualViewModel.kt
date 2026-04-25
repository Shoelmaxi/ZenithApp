package com.example.zenithapp20.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.zenithapp20.data.dao.*
import com.example.zenithapp20.data.model.*
import com.example.zenithapp20.ui.utils.NotificacionSuenoWorker
import com.example.zenithapp20.utils.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ── Rango de Integridad ───────────────────────────────────────────────────────

enum class RangoIntegridad(
    val letra: String,
    val label: String,
    val color: Long,
    val minPct: Int
) {
    S("S", "Élite",         0xFF00C853, 90),
    A("A", "Alto",          0xFF4CAF50, 80),
    B("B", "Sólido",        0xFFFFD700, 70),
    C("C", "Regular",       0xFFFF9800, 55),
    D("D", "Inconsistente", 0xFFFF4444, 40),
    E("E", "Crítico",       0xFF888888,  0)
}

data class IntegrityStats(
    val rango:          RangoIntegridad,
    val porcentaje:     Int,
    val completados:    Int,
    val totalRegistros: Int,
    val diasAnalizados: Int = 30
)

fun calcularRango(pct: Int): RangoIntegridad =
    RangoIntegridad.entries.first { pct >= it.minPct }

// ── Estado del timer de Deep Work ─────────────────────────────────────────────

data class DeepWorkTimerState(
    val isRunning:           Boolean = false,
    val isPaused:            Boolean = false,
    val timeLeftSeg:         Long    = 3600L,
    val duracionObjetivoMin: Int     = 60,
    val duracionRealSeg:     Long    = 0L,
    val distracciones:       Int     = 0,
    val intencion:           String  = "",
    val showResult:          Boolean = false
)

class IngenieriaConductualViewModel(
    private val analisisDao:    AnalisisHabitoDao,
    private val deepWorkDao:    DeepWorkDao,
    private val resilienciaDao: ResilienciaDao,
    private val context:        Context
) : ViewModel() {

    // ── Prefs compartidas ─────────────────────────────────────────────────────
    private val prefs: SharedPreferences =
        context.getSharedPreferences("zenith_ic_prefs", Context.MODE_PRIVATE)

    // ── AAA ───────────────────────────────────────────────────────────────────
    val analisis: StateFlow<List<AnalisisHabito>> = analisisDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarAnalisis(analisis: AnalisisHabito) {
        viewModelScope.launch { analisisDao.insert(analisis) }
    }

    // ── Rango de Integridad ───────────────────────────────────────────────────
    val integrityStats: StateFlow<IntegrityStats?> = analisis
        .map { lista ->
            if (lista.isEmpty()) return@map null
            val hace30Dias = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val ultimos30  = lista.filter { it.fechaMillis >= hace30Dias }
            if (ultimos30.isEmpty()) return@map null
            val completados = ultimos30.count { it.completado }
            val total       = ultimos30.size
            val pct         = ((completados.toFloat() / total) * 100).toInt()
            IntegrityStats(
                rango          = calcularRango(pct),
                porcentaje     = pct,
                completados    = completados,
                totalRegistros = total
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Sombra Predictiva — lógica pura ──────────────────────────────────────
    /**
     * Cuenta cuántos registros del historial AAA del mismo día de la semana
     * tuvieron completado=false o focusLevel<5.
     */
    fun calcularFallosPatronDiaActual(registros: List<AnalisisHabito>): Int {
        val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return registros.count { r ->
            val cal = Calendar.getInstance().apply { timeInMillis = r.fechaMillis }
            cal.get(Calendar.DAY_OF_WEEK) == hoy && (!r.completado || r.focusLevel < 5)
        }
    }

    // ── Deep Work ─────────────────────────────────────────────────────────────
    val sesionesDeepWork: StateFlow<List<SesionDeepWork>> = deepWorkDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dwTimerState = MutableStateFlow(DeepWorkTimerState())
    val dwTimerState: StateFlow<DeepWorkTimerState> = _dwTimerState.asStateFlow()

    private var dwTimerJob: Job? = null

    fun iniciarDeepWork(duracionMin: Int, intencion: String) {
        dwTimerJob?.cancel()
        _dwTimerState.value = DeepWorkTimerState(
            isRunning           = true,
            isPaused            = false,
            timeLeftSeg         = duracionMin * 60L,
            duracionObjetivoMin = duracionMin,
            duracionRealSeg     = 0L,
            distracciones       = 0,
            intencion           = intencion,
            showResult          = false
        )
        DeepWorkForegroundService.iniciar(context)
        lanzarTimerJob()
    }

    fun togglePausaDeepWork() {
        val state = _dwTimerState.value
        if (!state.isRunning) return
        if (state.isPaused) {
            _dwTimerState.value = state.copy(isPaused = false)
            lanzarTimerJob()
        } else {
            dwTimerJob?.cancel()
            _dwTimerState.value = state.copy(isPaused = true)
            actualizarNotifDeepWork()
        }
    }

    fun registrarDistraccionDeepWork() {
        _dwTimerState.value = _dwTimerState.value.copy(
            distracciones = _dwTimerState.value.distracciones + 1
        )
        actualizarNotifDeepWork()
    }

    fun terminarDeepWorkManual() {
        dwTimerJob?.cancel()
        val state = _dwTimerState.value
        if (state.duracionRealSeg > 0) {
            guardarSesionDeepWork(state.duracionObjetivoMin, state.duracionRealSeg, state.distracciones)
            _dwTimerState.value = state.copy(isRunning = false, isPaused = false, showResult = true)
        } else {
            _dwTimerState.value = DeepWorkTimerState()
        }
        DeepWorkForegroundService.detener(context)
    }

    fun cerrarResultadoDeepWork() {
        _dwTimerState.value = DeepWorkTimerState()
    }

    private fun lanzarTimerJob() {
        dwTimerJob?.cancel()
        dwTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _dwTimerState.value
                if (!state.isRunning || state.isPaused) break
                val nuevaTimeLeft = state.timeLeftSeg - 1
                val nuevaRealSeg  = state.duracionRealSeg + 1
                _dwTimerState.value = state.copy(
                    timeLeftSeg     = nuevaTimeLeft,
                    duracionRealSeg = nuevaRealSeg
                )
                actualizarNotifDeepWork()
                if (nuevaTimeLeft <= 0) {
                    guardarSesionDeepWork(state.duracionObjetivoMin, nuevaRealSeg, state.distracciones)
                    _dwTimerState.value = _dwTimerState.value.copy(
                        isRunning = false, isPaused = false, showResult = true
                    )
                    DeepWorkForegroundService.detener(context)
                    break
                }
            }
        }
    }

    private fun actualizarNotifDeepWork() {
        val state = _dwTimerState.value
        DeepWorkForegroundService.actualizar(
            context,
            DeepWorkNotifState(
                timeLeftStr   = "%02d:%02d".format(state.timeLeftSeg / 60, state.timeLeftSeg % 60),
                intencion     = state.intencion,
                distracciones = state.distracciones,
                isPaused      = state.isPaused
            )
        )
    }

    fun guardarSesionDeepWork(duracionObjetivoMin: Int, duracionRealSegundos: Long, distracciones: Int) {
        val calidad = duracionRealSegundos.toFloat() / (distracciones + 1)
        viewModelScope.launch {
            deepWorkDao.insert(
                SesionDeepWork(
                    duracionObjetivoMin  = duracionObjetivoMin,
                    duracionRealSegundos = duracionRealSegundos,
                    distracciones        = distracciones,
                    calidadSesion        = calidad
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
        var r   = resilienciaDao.getDeHoy(hoy)
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

    private fun calcularRachaPoder(registros: List<RegistroResiliencia>): Float {
        var racha = 0f
        registros.sortedBy { it.fechaDia }.forEach { registro ->
            racha = if (registro.diaPerfecto) racha + 1f else maxOf(racha * 0.5f, 0f)
        }
        return racha
    }

    // ── Sleep Calculator ──────────────────────────────────────────────────────
    fun calcularHorasDormir(horaDespertar: Int, minDespertar: Int): List<Triple<Int, Int, Int>> {
        return listOf(6, 5, 4).map { ciclos ->
            val totalMin   = ciclos * 90 + 15
            var minTotales = horaDespertar * 60 + minDespertar - totalMin
            if (minTotales < 0) minTotales += 24 * 60
            Triple((minTotales / 60) % 24, minTotales % 60, ciclos)
        }
    }

    fun programarNotificacionSueno(horaDormir: Int, minDormir: Int) {
        val ahora    = Calendar.getInstance()
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

    // ── MODO BLACKOUT ─────────────────────────────────────────────────────────

    private val PREFS_BLACKOUT_ACTIVO = "blackout_activo"

    private val _blackoutActivo = MutableStateFlow(
        prefs.getBoolean(PREFS_BLACKOUT_ACTIVO, false)
    )
    val blackoutActivo: StateFlow<Boolean> = _blackoutActivo.asStateFlow()

    /**
     * Activa el blackout inmediatamente.
     * Requiere que el usuario haya concedido el permiso SYSTEM_ALERT_WINDOW.
     */
    fun activarBlackout(ctx: Context) {
        prefs.edit().putBoolean(PREFS_BLACKOUT_ACTIVO, true).apply()
        _blackoutActivo.value = true
        BlackoutOverlayService.iniciar(ctx)
    }

    fun desactivarBlackout(ctx: Context) {
        prefs.edit().putBoolean(PREFS_BLACKOUT_ACTIVO, false).apply()
        _blackoutActivo.value = false
        BlackoutOverlayService.detener(ctx)
    }

    /**
     * Programa el blackout para que se active automáticamente a la hora de dormir
     * calculada por el Optimizador Circadiano.
     * [horaDormir] y [minDormir] = hora a la que debería apagarse la pantalla.
     */
    fun programarBlackoutParaDormir(horaDormir: Int, minDormir: Int) {
        val ahora    = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, horaDormir)
            set(Calendar.MINUTE, minDormir)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (ahora.after(objetivo)) objetivo.add(Calendar.DAY_OF_YEAR, 1)
        val delay = objetivo.timeInMillis - ahora.timeInMillis

        WorkManager.getInstance(context).enqueueUniqueWork(
            "blackout_activation",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<BlackoutActivationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
        )
    }

    // ── SOMBRA PREDICTIVA — programar worker diario ───────────────────────────

    /**
     * Programa el worker de Sombra Predictiva para las 22:00 cada día.
     * Llamar una sola vez desde BootReceiver o ZenithApp.
     */
    fun programarSombraPredictiva() {
        val wm = WorkManager.getInstance(context)
        wm.enqueueUniquePeriodicWork(
            "sombra_predictiva",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SombraPredictiveWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayHasta(22, 0), TimeUnit.MILLISECONDS)
                .build()
        )
    }

    private fun delayHasta(hora: Int, minuto: Int): Long {
        val ahora    = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }
        if (ahora.after(objetivo)) objetivo.add(Calendar.DAY_OF_YEAR, 1)
        return objetivo.timeInMillis - ahora.timeInMillis
    }

    override fun onCleared() {
        super.onCleared()
        dwTimerJob?.cancel()
        if (_dwTimerState.value.isRunning) {
            DeepWorkForegroundService.detener(context)
        }
    }

    private fun inicioDiaHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}