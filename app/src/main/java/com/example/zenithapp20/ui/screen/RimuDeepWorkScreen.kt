package com.example.zenithapp20.ui.screen

import android.app.Activity
import android.view.WindowManager
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
import androidx.compose.ui.platform.LocalContext
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

private val DURACIONES = listOf(25, 45, 60, 90, 120)

@Composable
fun RimuDeepWorkScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val sesiones by viewModel.sesionesDeepWork.collectAsState()
    val context  = LocalContext.current

    var duracionObjetivo       by remember { mutableIntStateOf(60) }
    var intencion              by remember { mutableStateOf("") }
    var dificultadPercibida    by remember { mutableFloatStateOf(5f) }
    var isRunning              by remember { mutableStateOf(false) }
    var isPaused               by remember { mutableStateOf(false) }
    var timeLeft               by remember { mutableLongStateOf(60 * 60L) }
    var distracciones          by remember { mutableIntStateOf(0) }
    var duracionRealSeg        by remember { mutableLongStateOf(0L) }
    var showResult             by remember { mutableStateOf(false) }
    var notaPost               by remember { mutableStateOf("") }

    // ── Pantalla encendida solo cuando hay sesión activa ──────────────────
    DisposableEffect(isRunning) {
        val window = (context as Activity).window
        if (isRunning) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else           window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose    { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

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
                viewModel.guardarSesionDeepWork(duracionObjetivo, duracionRealSeg, distracciones)
                showResult = true
            }
        }
    }

    val colorTimer by animateColorAsState(
        targetValue = when {
            !isRunning    -> Color(0xFF888888)
            distracciones > 3 -> Color(0xFFFF4444)
            else          -> Color(0xFF00C853)
        },
        animationSpec = tween(600),
        label = "timer_color"
    )

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(DeepBackground),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ── Cabecera ──────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(48.dp))
            DeepWorkHeader(
                navController = navController,
                isRunning     = isRunning,
                intencion     = intencion
            )
        }

        // ── Timer circular ────────────────────────────────────────────────
        item {
            DeepWorkTimerCircle(
                timeLeft        = timeLeft,
                progress        = progress,
                colorTimer      = colorTimer,
                isRunning       = isRunning,
                isPaused        = isPaused,
                distracciones   = distracciones,
                duracionRealSeg = duracionRealSeg
            )
        }

        // ── Config pre-sesión (solo si no está corriendo) ─────────────────
        if (!isRunning) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                DeepWorkIntentionField(
                    intencion = intencion,
                    onchange  = { intencion = it }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                DeepWorkDurationSelector(
                    duracionObjetivo = duracionObjetivo,
                    onPresetSelect   = { min ->
                        duracionObjetivo = min
                        timeLeft         = min * 60L
                    },
                    onSliderChange = { min ->
                        duracionObjetivo = min
                        timeLeft         = min * 60L
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                DeepWorkDifficultySlider(
                    dificultad = dificultadPercibida,
                    onChange   = { dificultadPercibida = it }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // ── Botón de distracción (solo corriendo) ─────────────────────────
        if (isRunning && !isPaused) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Button(
                        onClick  = { distracciones++ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444).copy(0.15f)),
                        shape    = RoundedCornerShape(16.dp),
                        border   = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(
                            "📱 REGISTRAR DISTRACCIÓN",
                            color      = Color(0xFFFF4444),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Controles ─────────────────────────────────────────────────────
        item {
            DeepWorkControls(
                isRunning       = isRunning,
                isPaused        = isPaused,
                duracionRealSeg = duracionRealSeg,
                onStart = {
                    timeLeft        = duracionObjetivo * 60L
                    duracionRealSeg = 0L
                    distracciones   = 0
                    isPaused        = false
                    isRunning       = true
                },
                onTogglePause = { isPaused = !isPaused },
                onStop = {
                    if (duracionRealSeg > 0) {
                        viewModel.guardarSesionDeepWork(duracionObjetivo, duracionRealSeg, distracciones)
                        showResult = true
                    }
                    isRunning = false
                }
            )
        }

        // ── Historial (solo si no está corriendo) ─────────────────────────
        if (sesiones.isNotEmpty() && !isRunning) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "HISTORIAL",
                    color      = SecondaryText,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(sesiones.take(7)) { sesion ->
                DeepWorkHistoryRow(sesion = sesion)
            }
        }
    }

    // ── Diálogo de resultado ──────────────────────────────────────────────
    if (showResult) {
        DeepWorkResultDialog(
            duracionRealSeg  = duracionRealSeg,
            duracionObjetivo = duracionObjetivo,
            distracciones    = distracciones,
            intencion        = intencion,
            notaPost         = notaPost,
            onNotaChange     = { notaPost = it },
            onClose          = {
                showResult      = false
                timeLeft        = duracionObjetivo * 60L
                duracionRealSeg = 0L
                distracciones   = 0
                notaPost        = ""
                intencion       = ""
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Subcomponentes de Deep Work
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeepWorkHeader(
    navController: NavController,
    isRunning: Boolean,
    intencion: String
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
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

@Composable
private fun DeepWorkTimerCircle(
    timeLeft: Long,
    progress: Float,
    colorTimer: Color,
    isRunning: Boolean,
    isPaused: Boolean,
    distracciones: Int,
    duracionRealSeg: Long
) {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(top = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 18.dp.toPx()
            val radius      = (size.minDimension - strokeWidth) / 2
            val topLeft     = Offset(center.x - radius, center.y - radius)
            val arcSize     = Size(radius * 2, radius * 2)
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
                useCenter  = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft, size = arcSize
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "%02d:%02d".format(timeLeft / 60, timeLeft % 60),
                color      = Color.White,
                fontSize   = 48.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                if (!isRunning) "LISTO" else if (isPaused) "PAUSADO" else "ENFOCADO",
                color         = colorTimer,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 3.sp
            )
            if (isRunning && duracionRealSeg > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("${duracionRealSeg / 60} min reales", color = SecondaryText, fontSize = 11.sp)
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

@Composable
private fun DeepWorkIntentionField(intencion: String, onchange: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("¿EN QUÉ VAS A TRABAJAR?", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value         = intencion,
            onValueChange = { if (it.length <= 100) onchange(it) },
            placeholder   = {
                Text(
                    "Ej: Terminar el módulo de autenticación",
                    color = SecondaryText.copy(0.4f), fontSize = 13.sp
                )
            },
            modifier       = Modifier.fillMaxWidth(),
            maxLines       = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors         = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Color(0xFF2196F3),
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White,
                cursorColor          = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun DeepWorkDurationSelector(
    duracionObjetivo: Int,
    onPresetSelect: (Int) -> Unit,
    onSliderChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("DURACIÓN", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "$duracionObjetivo min",
                color = Color(0xFF2196F3), fontSize = 18.sp, fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DURACIONES.forEach { min ->
                val isSelected = duracionObjetivo == min
                Surface(
                    modifier = Modifier.weight(1f),
                    color    = if (isSelected) Color(0xFF2196F3).copy(0.15f) else Color.White.copy(0.04f),
                    shape    = RoundedCornerShape(10.dp),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, if (isSelected) Color(0xFF2196F3) else CardBorderColor
                    ),
                    onClick = { onPresetSelect(min) }
                ) {
                    Text(
                        "${min}m",
                        color      = if (isSelected) Color(0xFF2196F3) else SecondaryText,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Ajuste fino", color = SecondaryText.copy(0.6f), fontSize = 10.sp)
        Slider(
            value         = duracionObjetivo.toFloat(),
            onValueChange = { onSliderChange((it / 5).toInt() * 5) },
            valueRange    = 15f..180f,
            colors        = SliderDefaults.colors(
                thumbColor          = Color(0xFF2196F3),
                activeTrackColor    = Color(0xFF2196F3),
                inactiveTrackColor  = Color.White.copy(0.1f)
            )
        )
    }
}

@Composable
private fun DeepWorkDifficultySlider(dificultad: Float, onChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("DIFICULTAD PERCIBIDA", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "${dificultad.toInt()}/10",
                color = when {
                    dificultad >= 8 -> Color(0xFFFF4444)
                    dificultad >= 5 -> Color(0xFFFFD700)
                    else            -> Color(0xFF00C853)
                },
                fontSize = 14.sp, fontWeight = FontWeight.Black
            )
        }
        Slider(
            value         = dificultad,
            onValueChange = onChange,
            valueRange    = 1f..10f,
            steps         = 8,
            colors        = SliderDefaults.colors(
                thumbColor         = Color(0xFFFFD700),
                activeTrackColor   = Color(0xFFFFD700),
                inactiveTrackColor = Color.White.copy(0.1f)
            )
        )
        Text(
            when {
                dificultad >= 8 -> "Tarea muy difícil — espera más distracciones"
                dificultad >= 5 -> "Dificultad media"
                else            -> "Tarea fluida — buen momento para el deep work"
            },
            color = SecondaryText, fontSize = 10.sp
        )
    }
}

@Composable
private fun DeepWorkControls(
    isRunning: Boolean,
    isPaused: Boolean,
    duracionRealSeg: Long,
    onStart: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isRunning) {
            Button(
                onClick  = onStart,
                modifier = Modifier.weight(1f).height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Text("INICIAR", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Button(
                onClick  = onTogglePause,
                modifier = Modifier.weight(1f).height(56.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) Color(0xFF4CAF50) else Color.White.copy(0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isPaused) "REANUDAR" else "PAUSAR",
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick  = onStop,
                modifier = Modifier.weight(1f).height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.15f)),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Text("TERMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DeepWorkHistoryRow(sesion: com.example.zenithapp20.data.model.SesionDeepWork) {
    val sdf        = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "ES"))
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
        verticalAlignment     = Alignment.CenterVertically
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
                    else             -> Color(0xFFFF4444)
                },
                fontSize = 13.sp, fontWeight = FontWeight.Black
            )
            Text("${sesion.distracciones} distr.", color = SecondaryText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DeepWorkResultDialog(
    duracionRealSeg: Long,
    duracionObjetivo: Int,
    distracciones: Int,
    intencion: String,
    notaPost: String,
    onNotaChange: (String) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor   = Color(0xFF1A1A1A),
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(
                when {
                    (duracionRealSeg / 60) >= duracionObjetivo -> "🏆 SESIÓN COMPLETA"
                    duracionRealSeg >= 60 * 15                 -> "⚡ SESIÓN REGISTRADA"
                    else                                       -> "📋 SESIÓN CORTA"
                },
                color = Color(0xFF00C853), fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (intencion.isNotBlank()) {
                    Surface(
                        color  = Color(0xFF2196F3).copy(0.08f),
                        shape  = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(0.3f))
                    ) {
                        Text(
                            intencion, color = Color(0xFF2196F3),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                DeepResultRow("Duración real", "${duracionRealSeg / 60} min de $duracionObjetivo")
                DeepResultRow("Distracciones", "$distracciones")
                DeepResultRow(
                    "Eficiencia",
                    "${((duracionRealSeg / 60f) / duracionObjetivo * 100).toInt().coerceAtMost(100)}%"
                )
                HorizontalDivider(color = Color.White.copy(0.08f))
                Text("¿Qué lograste?", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value         = notaPost,
                    onValueChange = onNotaChange,
                    placeholder   = {
                        Text("Describe brevemente lo que completaste…",
                            color = SecondaryText.copy(0.4f), fontSize = 12.sp)
                    },
                    modifier  = Modifier.fillMaxWidth(),
                    minLines  = 2,
                    maxLines  = 3,
                    colors    = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color(0xFF4CAF50),
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("CERRAR", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DeepResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF888888), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}