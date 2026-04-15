package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoLibro(val label: String, val emoji: String) {
    PENDIENTE("Pendiente", "📚"),
    LEYENDO("Leyendo", "📖"),
    TERMINADO("Terminado", "✅"),
    ABANDONADO("Abandonado", "❌")
}

enum class CategoriaLibro(val label: String, val emoji: String) {
    ESTRATEGIA("Estrategia", "♟️"),
    FILOSOFIA("Filosofía", "🧠"),
    HISTORIA("Historia", "🏛️"),
    CIENCIA("Ciencia", "🔬"),
    NEGOCIOS("Negocios", "💼"),
    PSICOLOGIA("Psicología", "🪞"),
    OTRO("Otro", "📌")
}

@Entity(tableName = "libros")
data class Libro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val autor: String,
    val paginasTotales: Int,
    val paginaActual: Int = 0,
    val estado: EstadoLibro = EstadoLibro.PENDIENTE,
    val categoria: CategoriaLibro = CategoriaLibro.OTRO,
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null
) {
    // Computed — no se persiste en BD, igual que rachaDias en Habito
    val progresoFloat: Float
        get() = if (paginasTotales > 0)
            (paginaActual.toFloat() / paginasTotales.toFloat()).coerceIn(0f, 1f)
        else 0f

    val porcentaje: Int
        get() = (progresoFloat * 100).toInt()

    val paginasRestantes: Int
        get() = (paginasTotales - paginaActual).coerceAtLeast(0)
}

@Entity(tableName = "sesiones_lectura")
data class SesionLectura(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libroId: Long,
    val paginaInicio: Int,
    val paginaFin: Int,
    val leccionClave: String = "",
    val aplicacionEstrategica: String = "",
    val categoria: CategoriaLibro = CategoriaLibro.OTRO,
    val sacrificio: String = "",
    val esMinimoCumplido: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
) {
    // Computed — evita inconsistencias en BD
    val paginasLeidas: Int get() = (paginaFin - paginaInicio).coerceAtLeast(0)

    val tieneInsight: Boolean
        get() = leccionClave.isNotBlank() || aplicacionEstrategica.isNotBlank()
}