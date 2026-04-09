package com.example.zenithapp20.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.PrimaryText
import com.example.zenithapp20.ui.theme.SecondaryText

@Composable
fun GymExercisePreviewItem(ejercicio: EjercicioGym) {
    val completado = ejercicio.estaCompletado

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = if (completado) Color(0xFF00C853).copy(0.05f) else Color.White.copy(0.02f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (completado) Color(0xFF00C853).copy(0.5f) else Color.White.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombre.uppercase(),
                    color = if (completado) Color(0xFF00C853) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (ejercicio.esCardio) "${ejercicio.minutosCardio} min" else "${ejercicio.seriesObjetivo} x ${ejercicio.repsObjetivo}",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            // Muestra el Récord Personal
            Column(horizontalAlignment = Alignment.End) {
                Text("PR", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = ejercicio.recordPersonal,
                    color = if (completado) Color(0xFF00C853) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}