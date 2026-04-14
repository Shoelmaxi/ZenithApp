package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agua")
data class AguaRegistro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fechaMillis: Long = System.currentTimeMillis(),
    val cantidadMl: Int = 250
)