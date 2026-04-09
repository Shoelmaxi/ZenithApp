package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. SerieRegistro y EjercicioGym no necesitan @Entity porque vivirán dentro de RutinaDia
data class SerieRegistro(
    val peso: String = "",
    val reps: Int = 0,
    val completada: Boolean = false
)

data class EjercicioGym(
    val nombre: String,
    val seriesObjetivo: Int = 0,
    val repsObjetivo: String = "",
    val pesoAnterior: String = "0",
    val esCardio: Boolean = false,
    val minutosCardio: Int = 0,
    val descansoSegundos: Int = 60,
    val registrosRealizados: List<SerieRegistro> = emptyList()
) {
    // Room ignorará automáticamente estas propiedades calculadas, lo cual es perfecto
    val recordPersonal: String
        get() {
            val maxHoy = registrosRealizados
                .mapNotNull { it.peso.toDoubleOrNull() }
                .maxOrNull()
            val anterior = pesoAnterior.toDoubleOrNull() ?: 0.0
            return if (maxHoy != null && maxHoy > anterior) "${maxHoy.toInt()} KG" else "${anterior.toInt()} KG"
        }

    val estaCompletado: Boolean
        get() = if (esCardio) registrosRealizados.isNotEmpty()
        else registrosRealizados.size >= seriesObjetivo
}

// 2. Esta es la única TABLA real para Room
@Entity(tableName = "rutinas")
data class RutinaDia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dia: String, // "L", "M", "X", "J", "V", "S", "D"
    val nombreRutina: String,
    val ejercicios: List<EjercicioGym> = emptyList() // Requerirá TypeConverter (JSON)
)