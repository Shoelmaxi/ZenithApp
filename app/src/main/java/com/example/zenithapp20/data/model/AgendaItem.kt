package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoAgenda { RECURRENTE, FECHA_ESPECIFICA }

@Entity(tableName = "agenda")
data class AgendaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val hora: String,
    val tipo: TipoAgenda = TipoAgenda.RECURRENTE,
    val dias: List<String> = emptyList(),
    val fechaEspecificaMillis: Long? = null,
    val diasCompletados: List<String> = emptyList()
)