package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda")
data class AgendaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val hora: String,
    val dias: List<String>,
    val diasCompletados: List<String> = emptyList() // NUEVO
)