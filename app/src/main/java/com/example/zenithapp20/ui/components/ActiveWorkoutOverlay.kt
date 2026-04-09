package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.SerieRegistro
import com.example.zenithapp20.ui.theme.DeepBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import kotlinx.coroutines.delay
import com.example.zenithapp20.ui.components.CustomTextField
import com.example.zenithapp20.ui.components.DayChip

@Composable
fun ActiveWorkoutOverlay(
    ejercicios: List<EjercicioGym>,
    onFinish: (List<EjercicioGym>) -> Unit
) {
    // Estado del progreso
    var ejIdx by remember { mutableIntStateOf(0) }
    var serieActual by remember { mutableIntStateOf(1) }

    // Inputs del usuario
    var pesoInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    // Lista para acumular los ejercicios con sus registros reales
    val ejerciciosFinales = remember { ejercicios.toMutableStateList() }
    val registrosDeEsteEjercicio = remember { mutableStateListOf<SerieRegistro>() }

    val ejercicioActual = ejerciciosFinales.getOrNull(ejIdx)

    // Timer states
    var timeLeft by remember { mutableIntStateOf(ejercicioActual?.descansoSegundos ?: 60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Lógica de Cronómetro y Avance
    LaunchedEffect(isTimerRunning, timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (isTimerRunning && timeLeft == 0) {
            isTimerRunning = false
            avanzarEntrenamiento(
                ejercicioActual = ejercicioActual,
                serieActual = serieActual,
                ejIdx = ejIdx,
                totalEjercicios = ejercicios.size,
                onNextSerie = { serieActual++ },
                onNextEjercicio = {
                    // Guardar registros en el ejercicio anterior antes de pasar al siguiente
                    ejerciciosFinales[ejIdx] = ejerciciosFinales[ejIdx].copy(
                        registrosRealizados = registrosDeEsteEjercicio.toList()
                    )
                    registrosDeEsteEjercicio.clear()
                    ejIdx++
                    serieActual = 1
                    pesoInput = ""
                    repsInput = ""
                },
                onFinishAll = {
                    ejerciciosFinales[ejIdx] = ejerciciosFinales[ejIdx].copy(
                        registrosRealizados = registrosDeEsteEjercicio.toList()
                    )
                    onFinish(ejerciciosFinales)
                },
                resetTimer = { timeLeft = ejercicios.getOrNull(if(serieActual < (ejercicioActual?.seriesObjetivo ?: 0)) ejIdx else ejIdx + 1)?.descansoSegundos ?: 60 }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepBackground).padding(32.dp)) {
        ejercicioActual?.let { ej ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cabecera de estado
                Text(
                    text = if (isTimerRunning) "DESCANSO" else "TRABAJO",
                    color = if (isTimerRunning) Color(0xFFFFD700) else Color(0xFF00C853),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = ej.nombre.uppercase(),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info de guía
                Text(
                    text = if(ej.esCardio) "OBJETIVO: ${ej.minutosCardio} MIN" else "OBJETIVO: ${ej.repsObjetivo} REPS",
                    color = SecondaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (!isTimerRunning) {
                    // MODO TRABAJO: Inputs de registro
                    Text(
                        text = "SERIE $serieActual",
                        color = Color.White,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField(value = pesoInput, onValueChange = { pesoInput = it }, label = "KG")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField(value = repsInput, onValueChange = { repsInput = it }, label = "REPS")
                        }
                    }
                } else {
                    // MODO DESCANSO: Reloj gigante
                    Text(
                        text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                        color = Color.White,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(text = "PREPÁRATE PARA LA SERIE ${serieActual + 1}", color = SecondaryText, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón de acción principal
                Button(
                    onClick = {
                        if (!isTimerRunning) {
                            // Registrar la serie actual
                            registrosDeEsteEjercicio.add(
                                SerieRegistro(
                                    peso = pesoInput,
                                    reps = repsInput.toIntOrNull() ?: 0,
                                    completada = true
                                )
                            )

                            if (ej.esCardio) {
                                // El cardio no tiene timer, pasa directo
                                timeLeft = 0
                                isTimerRunning = true
                            } else {
                                isTimerRunning = true
                            }
                        } else {
                            timeLeft = 0 // Saltar descanso
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isTimerRunning) Color.White.copy(0.1f) else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isTimerRunning) "SALTAR DESCANSO" else "TERMINAR Y REGISTRAR",
                        color = if (isTimerRunning) Color.White else Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

// Función auxiliar para manejar la navegación del entreno
private fun avanzarEntrenamiento(
    ejercicioActual: EjercicioGym?,
    serieActual: Int,
    ejIdx: Int,
    totalEjercicios: Int,
    onNextSerie: () -> Unit,
    onNextEjercicio: () -> Unit,
    onFinishAll: () -> Unit,
    resetTimer: () -> Unit
) {
    if (ejercicioActual == null) return

    if (serieActual < ejercicioActual.seriesObjetivo && !ejercicioActual.esCardio) {
        onNextSerie()
        resetTimer()
    } else {
        if (ejIdx < totalEjercicios - 1) {
            onNextEjercicio()
            resetTimer()
        } else {
            onFinishAll()
        }
    }
}