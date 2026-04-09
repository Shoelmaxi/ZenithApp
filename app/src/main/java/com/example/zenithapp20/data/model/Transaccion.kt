package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoTransaccion { INGRESO, EGRESO }

@Entity(tableName = "finanzas")
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val monto: Int, // Ideal para CLP (Pesos Chilenos)
    val categoria: String,
    val tipo: TipoTransaccion, // Requerirá TypeConverter (String)
    val fechaMillis: Long = System.currentTimeMillis()
)