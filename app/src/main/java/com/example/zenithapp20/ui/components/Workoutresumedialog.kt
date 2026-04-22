package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import com.example.zenithapp20.ui.viewmodel.WorkoutState

@Composable
fun WorkoutResumeDialog(
    savedState: WorkoutState,
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    val ejercicioActual = savedState.ejerciciosFinales.getOrNull(savedState.ejIdx)
    val totalEjercicios = savedState.ejerciciosFinales.size
    val seriesCompletadas = savedState.ejerciciosFinales
        .take(savedState.ejIdx)
        .sumOf { it.registrosRealizados.size }

    AlertDialog(
        onDismissRequest = { /* no cerrar con tap fuera */ },
        containerColor = MainCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "⚡ Entrenamiento en progreso",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Tienes una sesión sin terminar. ¿Quieres continuar donde lo dejaste?",
                    color = Color.White.copy(0.85f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Info de la sesión
                Surface(
                    color = Color(0xFFFFD700).copy(0.06f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ejercicioActual?.let {
                            Text(
                                "Ejercicio ${savedState.ejIdx + 1} / $totalEjercicios",
                                color = SecondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                it.nombre.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Serie ${savedState.serieActual} de ${it.seriesObjetivo}",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (seriesCompletadas > 0) {
                            Text(
                                "$seriesCompletadas series completadas en total",
                                color = SecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("REANUDAR", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("DESCARTAR", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold)
            }
        }
    )
}