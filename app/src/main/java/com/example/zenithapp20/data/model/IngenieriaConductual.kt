package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── AAA ──────────────────────────────────────────────────────────────────────
enum class FrictionFactor(val label: String, val emoji: String) {
    DISTRACCIONES("Distracciones", "📱"),
    FATIGA("Fatiga", "😴"),
    FALTA_TIEMPO("Falta de Tiempo", "⏰"),
    ENTORNO("Entorno", "🏠")
}

enum class FactorPositivo(val label: String, val emoji: String) {
    SIN_FRICCION("Sin fricción", "✨"),
    ALTA_ENERGIA("Alta energía", "⚡"),
    ENTORNO_IDEAL("Entorno ideal", "🏠"),
    MOTIVACION("Alta motivación", "🎯"),
    HABITO_AUTOMATICO("Ya es automático", "🔄"),
    MOMENTUM("Momentum del día", "🚀")
}

enum class RazonNoCompletado(val label: String, val emoji: String) {
    OLVIDO("Lo olvidé", "🧠"),
    FALTA_TIEMPO("Falta de tiempo", "⏰"),
    FALTA_ENERGIA("Sin energía", "😴"),
    OTRA_PRIORIDAD("Otra prioridad", "📋"),
    ENTORNO("Entorno no ayudó", "🏠"),
    RESISTENCIA_MENTAL("Resistencia mental", "🧗")
}

@Entity(tableName = "analisis_habito")
data class AnalisisHabito(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitoId: Long,
    val habitoNombre: String,
    val completado: Boolean = true,
    val focusLevel: Int,
    val frictionFactor: String,
    val factorPositivo: String = "",          // ← NUEVO: factor cuando focus es alto
    val razonNoCompletado: String = "",
    val adjustmentNote: String = "",
    val fechaMillis: Long = System.currentTimeMillis()
)

// ── Deep Work ─────────────────────────────────────────────────────────────────
@Entity(tableName = "sesiones_deep_work")
data class SesionDeepWork(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val duracionObjetivoMin: Int,
    val duracionRealSegundos: Long,
    val distracciones: Int,
    val calidadSesion: Float,
    val fechaMillis: Long = System.currentTimeMillis()
) {
    val duracionRealMin: Int get() = (duracionRealSegundos / 60).toInt()
    val eficienciaPct: Int get() = if (duracionObjetivoMin > 0)
        ((duracionRealMin.toFloat() / duracionObjetivoMin) * 100).toInt().coerceAtMost(100) else 0
}

// ── Resiliencia ───────────────────────────────────────────────────────────────
@Entity(tableName = "registros_resiliencia")
data class RegistroResiliencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaDia: Long,
    val duchaFria: Boolean = false,
    val ayunoDopamina: Boolean = false,
    val entrenamientoResistencia: Boolean = false
) {
    val completados: Int get() = listOf(duchaFria, ayunoDopamina, entrenamientoResistencia).count { it }
    val diaPerfecto: Boolean get() = completados == 3
}

// ── Reflexión Diaria ──────────────────────────────────────────────────────────
@Entity(tableName = "reflexiones_diarias")
data class ReflexionDiaria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaMillis: Long = System.currentTimeMillis(),
    val movimientoMaestro: String,
    val puntoCiego: String,
    val aperturaMañana: String
)