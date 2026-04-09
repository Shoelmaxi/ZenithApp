package com.example.zenithapp20.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

@Composable
fun CardRadarDisciplina(listaHabitos: List<Habito>) {
    // 1. Categorías oficiales (Deben coincidir con las del HabitoForm)
    val categorias = listOf("Salud", "Mente", "Productividad", "Social", "Hogar")

    // 2. Lógica de Puntaje REAL
    val puntajes = remember(listaHabitos) {
        categorias.map { cat ->
            // Filtramos hábitos que pertenecen a ESTA categoría
            val habitosDeCategoria = listaHabitos.filter { it.categoria == cat }

            if (habitosDeCategoria.isEmpty()) {
                0.1f // Valor mínimo para que no desaparezca el punto
            } else {
                // Sumamos todos los checks de los hábitos de esta categoría
                val totalChecks = habitosDeCategoria.sumOf { it.checks.size }

                // Dividimos por 5 para que con 5 checks ya se estire al máximo (Ajusta este número si quieres)
                (totalChecks.toFloat() / 5f).coerceIn(0.1f, 1f)
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "EQUILIBRIO DE DISCIPLINA",
                color = SecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // DIBUJO DEL RADAR
                Canvas(modifier = Modifier.size(160.dp)) {
                    val center = center
                    val radius = size.minDimension / 2
                    val angleStep = (2 * Math.PI / categorias.size).toFloat()

                    // A. Telaraña de fondo
                    for (i in 1..3) {
                        val currentRadius = radius * (i / 3f)
                        val path = androidx.compose.ui.graphics.Path()
                        for (j in categorias.indices) {
                            val angle = j * angleStep - Math.PI.toFloat() / 2
                            val x = center.x + currentRadius * Math.cos(angle.toDouble()).toFloat()
                            val y = center.y + currentRadius * Math.sin(angle.toDouble()).toFloat()
                            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = Color.White.copy(0.05f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                        )
                    }

                    // B. Polígono de Disciplina Real
                    val radarPath = androidx.compose.ui.graphics.Path()
                    puntajes.forEachIndexed { j, score ->
                        val angle = j * angleStep - Math.PI.toFloat() / 2
                        val x = center.x + (radius * score) * Math.cos(angle.toDouble()).toFloat()
                        val y = center.y + (radius * score) * Math.sin(angle.toDouble()).toFloat()
                        if (j == 0) radarPath.moveTo(x, y) else radarPath.lineTo(x, y)
                    }
                    radarPath.close()

                    drawPath(radarPath, Color(0xFF00C853).copy(0.3f)) // Verde Zenith Relleno
                    drawPath(
                        path = radarPath,
                        color = Color(0xFF00C853),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                    )
                }

                // C. Etiquetas dinámicas
                categorias.forEachIndexed { i, cat ->
                    val angle = i * (2 * Math.PI / categorias.size) - Math.PI / 2
                    // Un poco más de 100 para que no choquen con el dibujo
                    val xOff = (110 * Math.cos(angle)).toFloat()
                    val yOff = (110 * Math.sin(angle)).toFloat()

                    Text(
                        text = cat.uppercase(),
                        color = SecondaryText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(xOff.dp, yOff.dp)
                    )
                }
            }
        }
    }
}