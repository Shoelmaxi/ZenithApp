package com.example.zenithapp20.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.ui.theme.SecondaryText

@Composable
fun GymExercisePreviewItem(ejercicio: EjercicioGym) {
    val completado = ejercicio.estaCompletado
    val registros  = ejercicio.registrosRealizados

    val primera = registros.firstOrNull()
    val ultima  = registros.lastOrNull()

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        color    = if (completado) Color(0xFF00C853).copy(0.05f) else Color.White.copy(0.02f),
        shape    = RoundedCornerShape(16.dp),
        border   = BorderStroke(1.dp, if (completado) Color(0xFF00C853).copy(0.5f) else Color.White.copy(0.1f))
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nombre y objetivo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = ejercicio.nombre.uppercase(),
                    color      = if (completado) Color(0xFF00C853) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
                Text(
                    text  = if (ejercicio.esCardio) "${ejercicio.minutosCardio} min"
                    else "${ejercicio.seriesObjetivo} × ${ejercicio.repsObjetivo}",
                    color = SecondaryText,
                    fontSize = 12.sp
                )

                // Historial de series de la última sesión
                if (registros.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SessionSeriesSummary(primera = primera, ultima = ultima, total = registros.size)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // PR actual
            Column(horizontalAlignment = Alignment.End) {
                Text("PR", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text  = if (ejercicio.recordPersonal == "0 KG") "—" else ejercicio.recordPersonal,
                    color = if (completado) Color(0xFF00C853) else Color.White,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ── Subcomponente: resumen de series de la sesión anterior ───────────────────

@Composable
private fun SessionSeriesSummary(
    primera: com.example.zenithapp20.data.model.SerieRegistro?,
    ultima: com.example.zenithapp20.data.model.SerieRegistro?,
    total: Int
) {
    if (primera == null) return

    val soloUna = total == 1 || primera == ultima

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Badge "Última sesión"
        Text(
            "ÚLTIMA SESIÓN",
            color      = SecondaryText,
            fontSize   = 8.sp,
            fontWeight = FontWeight.Bold
        )

        if (soloUna) {
            // Solo una serie registrada
            SerieChip(
                label  = "×$total",
                peso   = primera.peso,
                reps   = primera.reps,
                color  = Color(0xFFFFD700)
            )
        } else {
            // Primera y última distintas → muestra progresión
            SerieChip(label = "1ª", peso = primera.peso, reps = primera.reps, color = SecondaryText)
            Text("→", color = SecondaryText, fontSize = 10.sp)
            SerieChip(
                label  = "${total}ª",
                peso   = ultima?.peso ?: "",
                reps   = ultima?.reps ?: 0,
                color  = Color(0xFF00C853)
            )
        }
    }
}

@Composable
private fun SerieChip(label: String, peso: String, reps: Int, color: Color) {
    val content = buildString {
        if (peso.isNotBlank() && peso != "0") append("${peso}kg")
        if (reps > 0) {
            if (isNotEmpty()) append("×")
            append("${reps}")
        }
    }.ifBlank { "—" }

    Text(
        text       = "$label $content",
        color      = color,
        fontSize   = 10.sp,
        fontWeight = FontWeight.Bold
    )
}