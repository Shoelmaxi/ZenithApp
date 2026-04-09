package com.example.zenithapp20.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Cambiado de String/UUID a Long para mejor rendimiento en Room
    val nombre: String,
    val meta: String = "1 vez al día",
    val categoria: String = "Salud",
    val checks: List<Long> = emptyList(), // Requerirá TypeConverter
    val icono: String = "🔥"
) {
    // Room ignora automáticamente los 'val' con 'get()' personalizado,
    // así que tu lógica de racha queda intacta y segura.
    val semanasPerfectas: Int
        get() {
            if (checks.isEmpty()) return 0

            val fechas = checks.map {
                Calendar.getInstance().apply {
                    timeInMillis = it
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }.distinct().sortedDescending()

            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val ayer = hoy - 86400000

            if (!fechas.contains(hoy) && !fechas.contains(ayer)) return 0

            var diasSeguidos = 0
            var fechaEsperada = fechas.first()
            for (f in fechas) {
                if (f == fechaEsperada) {
                    diasSeguidos++
                    fechaEsperada -= 86400000
                } else break
            }
            return diasSeguidos / 7
        }
}