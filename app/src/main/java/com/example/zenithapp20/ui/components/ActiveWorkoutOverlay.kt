package com.example.zenithapp20.ui.components

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.SerieRegistro
import com.example.zenithapp20.ui.theme.DeepBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import com.example.zenithapp20.ui.viewmodel.WorkoutState
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutOverlay(
    workoutState: WorkoutState,
    onStateUpdate: (WorkoutState) -> Unit,
    onFinish: (List<EjercicioGym>) -> Unit
) {
    val context = LocalContext.current

    // --- FIX: Mantener pantalla encendida durante el entrenamiento ---
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // --- Sonido para cuenta regresiva ---
    val toneGen = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }
    DisposableEffect(Unit) {
        onDispose { toneGen.release() }
    }

    // --- Estado local del overlay, inicializado desde el ViewModel ---
    var ejIdx by remember { mutableIntStateOf(workoutState.ejIdx) }
    var serieActual by remember { mutableIntStateOf(workoutState.serieActual) }
    val ejerciciosFinales = remember {
        workoutState.ejerciciosFinales.toMutableList().also { /* snapshot */ }
    }.let { remember { it.toMutableList() } }

    // Registros de la serie actual del ejercicio en curso (se acumulan y se guardan al avanzar)
    val registrosDeEsteEjercicio = remember { mutableStateListOf<SerieRegistro>() }

    // Inputs del usuario
    var pesoInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    // Timer
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeLeft by remember {
        mutableIntStateOf(workoutState.ejerciciosFinales.getOrNull(workoutState.ejIdx)?.descansoSegundos ?: 60)
    }

    val ejercicioActual = ejerciciosFinales.getOrNull(ejIdx)

    // --- Determina si al terminar la serie actual toca cambiar de ejercicio ---
    val esUltimaSerieDeEjercicio = ejercicioActual?.esCardio == true ||
            serieActual >= (ejercicioActual?.seriesObjetivo ?: 1)

    // Nombre del siguiente ejercicio para mostrar durante el descanso
    val nombreSiguienteEjercicio = if (esUltimaSerieDeEjercicio) {
        ejerciciosFinales.getOrNull(ejIdx + 1)?.nombre
    } else null

    // --- Función para avanzar al siguiente estado (siguiente serie o siguiente ejercicio) ---
    fun avanzar() {
        if (ejercicioActual == null) return

        if (!esUltimaSerieDeEjercicio) {
            // Siguiente serie del mismo ejercicio
            serieActual++
            timeLeft = ejercicioActual.descansoSegundos
            onStateUpdate(WorkoutState(ejIdx, serieActual, ejerciciosFinales))
        } else {
            // Guardar registros en el ejercicio actual antes de pasar al siguiente
            val updatedEj = ejerciciosFinales[ejIdx].copy(
                registrosRealizados = registrosDeEsteEjercicio.toList()
            )
            ejerciciosFinales[ejIdx] = updatedEj

            if (ejIdx < ejerciciosFinales.size - 1) {
                // Siguiente ejercicio
                ejIdx++
                serieActual = 1
                pesoInput = ""
                repsInput = ""
                registrosDeEsteEjercicio.clear()
                timeLeft = ejerciciosFinales[ejIdx].descansoSegundos
                onStateUpdate(WorkoutState(ejIdx, serieActual, ejerciciosFinales))
            } else {
                // Entrenamiento terminado
                onFinish(ejerciciosFinales)
            }
        }
    }

    // --- FIX: Timer con while loop — no crea un efecto por cada tick ---
    LaunchedEffect(isTimerRunning) {
        if (!isTimerRunning) return@LaunchedEffect

        while (timeLeft > 0) {
            delay(1000L)
            if (!isTimerRunning) return@LaunchedEffect // Cancelado por saltar descanso
            timeLeft--

            // FIX: Sonido cuenta regresiva en los últimos 3 segundos
            if (timeLeft in 1..3) {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
            }
        }

        // Timer llegó a 0 naturalmente
        if (isTimerRunning) {
            isTimerRunning = false
            avanzar()
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(32.dp)
    ) {
        ejercicioActual?.let { ej ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Estado: TRABAJO / DESCANSO
                Text(
                    text = if (isTimerRunning) "DESCANSO" else "TRABAJO",
                    color = if (isTimerRunning) Color(0xFFFFD700) else Color(0xFF00C853),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = ej.nombre.uppercase(),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (ej.esCardio) "OBJETIVO: ${ej.minutosCardio} MIN"
                    else "OBJETIVO: ${ej.repsObjetivo} REPS",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (!isTimerRunning) {
                    // --- MODO TRABAJO ---
                    Text(
                        text = "SERIE $serieActual / ${ej.seriesObjetivo}",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (!ej.esCardio) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CustomTextField(
                                    value = pesoInput,
                                    onValueChange = { pesoInput = it },
                                    label = "KG"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CustomTextField(
                                    value = repsInput,
                                    onValueChange = { repsInput = it },
                                    label = "REPS"
                                )
                            }
                        }
                    }

                } else {
                    // --- MODO DESCANSO ---

                    // Reloj gigante — se pone rojo en los últimos 3 segundos
                    Text(
                        text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                        color = if (timeLeft in 1..3) Color.Red else Color.White,
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Light
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // FIX: Muestra el nombre del siguiente ejercicio si existe
                    if (nombreSiguienteEjercicio != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "A CONTINUACIÓN",
                                color = SecondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nombreSiguienteEjercicio.uppercase(),
                                color = Color(0xFF00C853),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "PREPÁRATE PARA LA SERIE ${serieActual + 1}",
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Barra de progreso del entrenamiento
                val progreso = (ejIdx.toFloat() + (serieActual.toFloat() / (ejercicioActual.seriesObjetivo.coerceAtLeast(1)))) / ejerciciosFinales.size.coerceAtLeast(1)
                LinearProgressIndicator(
                    progress = { progreso.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF00C853),
                    trackColor = Color.White.copy(0.1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal
                Button(
                    onClick = {
                        if (!isTimerRunning) {
                            // Registrar serie
                            registrosDeEsteEjercicio.add(
                                SerieRegistro(
                                    peso = pesoInput,
                                    reps = repsInput.toIntOrNull() ?: 0,
                                    completada = true
                                )
                            )
                            // FIX: Para cardio el timer se inicia igual (cuenta el descanso entre ejercicios)
                            isTimerRunning = true
                        } else {
                            // Saltar descanso: cancela el LaunchedEffect y avanza manualmente
                            isTimerRunning = false
                            avanzar()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) Color.White.copy(0.08f) else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isTimerRunning) "SALTAR DESCANSO" else "TERMINAR Y REGISTRAR",
                        color = if (isTimerRunning) Color.White else Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}