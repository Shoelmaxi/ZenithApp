package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.theme.SecondaryText
import java.util.Calendar

@Composable
fun CardActividadDiaria(listaHabitos: List<Habito>) {
    // 1. Calculamos la actividad de los últimos 7 días
    val actividadUltimosDias = remember(listaHabitos) {
        val cal = Calendar.getInstance().apply {
            // Ir 6 días atrás para tener una semana completa contando hoy
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }

        List(7) {
            val inicioDia = cal.timeInMillis
            val finDia = inicioDia + 86400000

            // Contamos cuántos checks totales hubo en este día entre todos los hábitos
            val conteoChecks = listaHabitos.sumOf { habito ->
                habito.checks.count { it in inicioDia until finDia }
            }

            cal.add(Calendar.DAY_OF_YEAR, 1)
            conteoChecks
        }
    }

    val maxChecks = actividadUltimosDias.maxOrNull()?.coerceAtLeast(1) ?: 1

    Surface(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("📈 ACTIVIDAD DIARIA", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.weight(1f))

            // Dibujamos la gráfica
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                actividadUltimosDias.forEach { conteo ->
                    // Proporción de la barra (altura basada en el máximo de checks)
                    val proporcion = conteo.toFloat() / maxChecks.toFloat()

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // El valor numérico pequeño arriba de la barra
                        if (conteo > 0) {
                            Text(conteo.toString(), color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // La barra de la gráfica
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(proporcion.coerceAtLeast(0.05f)) // Altura mínima para que se vea
                                .background(
                                    color = if (proporcion > 0.5f) Color.Green else Color.Green.copy(0.3f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Línea base decorativa
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hace 7 días", color = SecondaryText, fontSize = 10.sp)
                Text("Hoy", color = SecondaryText, fontSize = 10.sp)
            }
        }
    }
}