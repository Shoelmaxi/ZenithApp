package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda")
data class AgendaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Indispensable para borrar o editar items específicos
    val nombre: String,
    val descripcion: String,
    val hora: String,
    val dias: List<String>, // Esto requerirá el TypeConverter que haremos al final
    val completado: Boolean = false
)