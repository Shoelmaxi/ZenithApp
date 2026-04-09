package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.theme.SecondaryText
import java.util.Calendar

@Composable
fun SeguimientoSemanalHabitos(listaHabitos: List<Habito>) {
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    val timestampsSemana = remember {
        val cal = Calendar.getInstance().apply {
            // Ajuste para que la semana empiece en Lunes
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        List(7) {
            val time = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            time
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("SEGUIMIENTO SEMANAL", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // ENCABEZADO
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.width(180.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    diasSemana.forEach { dia ->
                        Text(
                            text = dia,
                            color = SecondaryText,
                            fontSize = 10.sp,
                            modifier = Modifier.width(20.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LISTA DE HÁBITOS
            listaHabitos.forEach { habito ->
                FilaHabitoStats(habito = habito, timestampsSemana = timestampsSemana)
                Spacer(modifier = Modifier.height(16.dp)) // Un poco más de espacio para los tags
            }
        }
    }
}

@Composable
fun FilaHabitoStats(habito: Habito, timestampsSemana: List<Long>) {
    val checksEstaSemana = habito.checks.count { it >= timestampsSemana.first() }
    val porcentaje = (checksEstaSemana.toFloat() / 7f * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna de Nombre + Racha
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habito.nombre.uppercase(),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // SI TIENE SEMANAS PERFECTAS, MOSTRAMOS EL TAG
            if (habito.rachaDias > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                RachaTag(dias = habito.rachaDias)
            }
        }

        // Porcentaje (estilo discreto)
        Text(
            text = "$porcentaje%",
            color = if(porcentaje == 100) Color(0xFF00C853) else SecondaryText.copy(0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Los Puntos de la semana
        Row(
            modifier = Modifier.width(180.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timestampsSemana.forEach { inicioDia ->
                val finDia = inicioDia + 86400000
                val cumplidoEseDia = habito.checks.any { it in inicioDia until finDia }

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = if (cumplidoEseDia) Color(0xFF00C853).copy(0.15f) else Color.White.copy(0.03f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (cumplidoEseDia) Color(0xFF00C853) else Color.White.copy(0.1f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}