package com.example.zenithapp20.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Available preset durations in minutes
private val DURACIONES = listOf(25, 45, 60, 90, 120)

@Composable
fun RimuDeepWorkScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val sesiones by viewModel.sesionesDeepWork.collectAsState()

    // ── Session config ────────────────────────────────────────────────────
    var duracionObjetivo by remember { mutableIntStateOf(60) }
    var intencion by remember { mutableStateOf("") }   // what are you working on?
    var dificultadPercibida by remember { mutableFloatStateOf(5f) }  // 1-10 pre-session

    // ── Timer state ───────────────────────────────────────────────────────
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableLongStateOf(60 * 60L) }
    var distracciones by remember { mutableIntStateOf(0) }
    var duracionRealSeg by remember { mutableLongStateOf(0L) }

    // ── Post-session ──────────────────────────────────────────────────────
    var showResult by remember { mutableStateOf(false) }
    var calidadFinal by remember { mutableFloatStateOf(0f) }
    var notaPost by remember { mutableStateOf("") }   // post-session note

    val totalSeg = duracionObjetivo * 60L
    val progress = if (totalSeg > 0) timeLeft.toFloat() / totalSeg else 0f

    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            while (timeLeft > 0 && isRunning && !isPaused) {
                delay(1000L)
                timeLeft--
                duracionRealSeg++
            }
            if (timeLeft <= 0 && isRunning) {
                isRunning = false
                calidadFinal = duracionRealSeg.toFloat() / (distracciones + 1)
                viewModel.guardarSesionDeepWork(duracionObjetivo, duracionRealSeg, distracciones)
                showResult = true
            }
        }
    }

    val minutos = timeLeft / 60
    val segundos = timeLeft % 60
    val timerText = "%02d:%02d".format(minutos, segundos)

    val colorTimer by animateColorAsState(
        targetValue = when {
            !isRunning -> Color(0xFF888888)
            distracciones > 3 -> Color(0xFFFF4444)
            else -> Color(0xFF00C853)
        },
        animationSpec = tween(600),
        label = "timer_color"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
                }
                Column {
                    Text("DEEP WORK", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    if (isRunning && intencion.isNotBlank()) {
                        Text(intencion, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }

        // ── Circular timer ─────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val strokeWidth = 18.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2, radius * 2)
                    drawArc(
                        color = Color.White.copy(0.05f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = topLeft, size = arcSize
                    )
                    drawArc(
                        color = colorTimer,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = topLeft, size = arcSize
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(timerText, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Light)
                    Text(
                        if (!isRunning) "LISTO" else if (isPaused) "PAUSADO" else "ENFOCADO",
                        color = colorTimer, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp
                    )
                    if (isRunning && duracionRealSeg > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${duracionRealSeg / 60} min reales",
                            color = SecondaryText, fontSize = 11.sp
                        )
                    }
                    if (distracciones > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "📱 $distracciones distracción${if (distracciones != 1) "es" else ""}",
                            color = Color(0xFFFF4444), fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ── PRE-SESSION CONFIG (only when not running) ────────────────────
        if (!isRunning) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Intention field
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("¿EN QUÉ VAS A TRABAJAR?", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = intencion,
                        onValueChange = { if (it.length <= 100) intencion = it },
                        placeholder = {
                            Text(
                                "Ej: Terminar el módulo de autenticación",
                                color = SecondaryText.copy(0.4f), fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = CardBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Duration selector — presets + custom slider
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DURACIÓN", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$duracionObjetivo min",
                            color = Color(0xFF2196F3),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DURACIONES.forEach { min ->
                            val isSelected = duracionObjetivo == min
                            Surface(
                                modifier = Modifier
                                    .weight(1f),
                                color = if (isSelected) Color(0xFF2196F3).copy(0.15f) else Color.White.copy(0.04f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF2196F3) else CardBorderColor
                                ),
                                onClick = {
                                    duracionObjetivo = min
                                    timeLeft = min * 60L
                                }
                            ) {
                                Text(
                                    "${min}m",
                                    color = if (isSelected) Color(0xFF2196F3) else SecondaryText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fine-tune slider (5-min steps, 15–180 min)
                    Text("Ajuste fino", color = SecondaryText.copy(0.6f), fontSize = 10.sp)
                    Slider(
                        value = duracionObjetivo.toFloat(),
                        onValueChange = {
                            duracionObjetivo = (it / 5).toInt() * 5  // snap to 5-min steps
                            timeLeft = duracionObjetivo * 60L
                        },
                        valueRange = 15f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF2196F3),
                            activeTrackColor = Color(0xFF2196F3),
                            inactiveTrackColor = Color.White.copy(0.1f)
                        )
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Pre-session difficulty
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("DIFICULTAD PERCIBIDA", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${dificultadPercibida.toInt()}/10",
                            color = when {
                                dificultadPercibida >= 8 -> Color(0xFFFF4444)
                                dificultadPercibida >= 5 -> Color(0xFFFFD700)
                                else -> Color(0xFF00C853)
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Slider(
                        value = dificultadPercibida,
                        onValueChange = { dificultadPercibida = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFFFFD700),
                            inactiveTrackColor = Color.White.copy(0.1f)
                        )
                    )
                    Text(
                        when {
                            dificultadPercibida >= 8 -> "Tarea muy difícil — espera más distracciones"
                            dificultadPercibida >= 5 -> "Dificultad media"
                            else -> "Tarea fluida — buen momento para el deep work"
                        },
                        color = SecondaryText,
                        fontSize = 10.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // ── Distraction button (only while running) ───────────────────────
        if (isRunning && !isPaused) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Button(
                        onClick = { distracciones++ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444).copy(0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("📱 REGISTRAR DISTRACCIÓN", color = Color(0xFFFF4444), fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Controls ───────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isRunning) {
                    Button(
                        onClick = {
                            timeLeft = duracionObjetivo * 60L
                            duracionRealSeg = 0L
                            distracciones = 0
                            isPaused = false
                            isRunning = true
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("INICIAR", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Button(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) Color(0xFF4CAF50) else Color.White.copy(0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (isPaused) "REANUDAR" else "PAUSAR", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (duracionRealSeg > 0) {
                                calidadFinal = duracionRealSeg.toFloat() / (distracciones + 1)
                                viewModel.guardarSesionDeepWork(duracionObjetivo, duracionRealSeg, distracciones)
                                showResult = true
                            }
                            isRunning = false
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("TERMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── History ────────────────────────────────────────────────────────
        if (sesiones.isNotEmpty() && !isRunning) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "HISTORIAL",
                    color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(sesiones.take(7)) { sesion ->
                val sdf = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "ES"))
                val eficiencia = sesion.eficienciaPct
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp)
                        .background(Color.White.copy(0.03f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(0.07f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${sesion.duracionRealMin}/${sesion.duracionObjetivoMin} min",
                            color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                        Text(sdf.format(Date(sesion.fechaMillis)), color = SecondaryText, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$eficiencia% eficiencia",
                            color = when {
                                eficiencia >= 90 -> Color(0xFF00C853)
                                eficiencia >= 70 -> Color(0xFFFFD700)
                                else -> Color(0xFFFF4444)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text("${sesion.distracciones} distr.", color = SecondaryText, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // ── Result dialog ──────────────────────────────────────────────────────
    if (showResult) {
        AlertDialog(
            onDismissRequest = { },  // force user to close explicitly
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    when {
                        (duracionRealSeg / 60) >= duracionObjetivo -> "🏆 SESIÓN COMPLETA"
                        duracionRealSeg >= 60 * 15 -> "⚡ SESIÓN REGISTRADA"
                        else -> "📋 SESIÓN CORTA"
                    },
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (intencion.isNotBlank()) {
                        Surface(
                            color = Color(0xFF2196F3).copy(0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(0.3f))
                        ) {
                            Text(
                                intencion,
                                color = Color(0xFF2196F3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    ResultRow("Duración real", "${duracionRealSeg / 60} min de $duracionObjetivo")
                    ResultRow("Distracciones", "$distracciones")
                    ResultRow("Eficiencia", "${((duracionRealSeg / 60f) / duracionObjetivo * 100).toInt().coerceAtMost(100)}%")

                    HorizontalDivider(color = Color.White.copy(0.08f))

                    // Post-session note
                    Text("¿Qué lograste?", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = notaPost,
                        onValueChange = { notaPost = it },
                        placeholder = { Text("Describe brevemente lo que completaste…", color = SecondaryText.copy(0.4f), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = CardBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showResult = false
                    // Reset for next session
                    timeLeft = duracionObjetivo * 60L
                    duracionRealSeg = 0L
                    distracciones = 0
                    notaPost = ""
                    intencion = ""
                }) {
                    Text("CERRAR", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF888888), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}