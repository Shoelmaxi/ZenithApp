package com.example.zenithapp20.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

// El Enum debe estar definido aquí o ser importado correctamente
enum class Prioridad(val nombre: String, val color: Color) {
    BAJA("Baja", Color(0xFF2196F3)),
    MEDIA("Media", Color(0xFFFF9800)),
    URGENTE("Urgente", Color(0xFFFF4444))
}

@Entity(tableName = "tareas")
data class TareaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val fechaLimiteMillis: Long,
    val prioridad: Prioridad, // Room usará el TypeConverter para esto
    val completada: Boolean = false
)