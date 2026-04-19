package com.example.zenithapp20.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuDeepWorkScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val sesiones by viewModel.sesionesDeepWork.collectAsState()

    var duracionObjetivo by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableLongStateOf(60 * 60L) }
    var distracciones by remember { mutableIntStateOf(0) }
    var duracionRealSeg by remember { mutableLongStateOf(0L) }
    var showResult by remember { mutableStateOf(false) }
    var calidadFinal by remember { mutableFloatStateOf(0f) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text("DEEP WORK", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        // TIMER CIRCULAR
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

                // Fondo
                drawArc(
                    color = Color.White.copy(0.05f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft, size = arcSize
                )
                // Progreso
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
                    color = colorTimer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                if (distracciones > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📱 $distracciones distracción${if (distracciones != 1) "es" else ""}",
                        color = Color(0xFFFF4444), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SELECTOR DURACIÓN (solo si no corriendo)
        if (!isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(60, 90).forEach { min ->
                    Button(
                        onClick = {
                            duracionObjetivo = min
                            timeLeft = min * 60L
                            duracionRealSeg = 0L
                            distracciones = 0
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (duracionObjetivo == min) Color(0xFF4CAF50) else Color.White.copy(0.08f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("${min}min", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // BOTÓN DISTRACCIÓN — grande, rojo
        if (isRunning && !isPaused) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                contentAlignment = Alignment.Center
            ) {
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

        // CONTROLES
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
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

        // HISTORIAL
        if (sesiones.isNotEmpty() && !isRunning) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "HISTORIAL",
                color = SecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(sesiones.take(5)) { sesion ->
                    val sdf = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "ES"))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                "Calidad: %.1f".format(sesion.calidadSesion),
                                color = Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.Black
                            )
                            Text("${sesion.distracciones} dist.", color = SecondaryText, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // RESULTADO MODAL
    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(20.dp),
            title = { Text("SESIÓN COMPLETADA", color = Color(0xFF00C853), fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultRow("Duración real", "${duracionRealSeg / 60} min")
                    ResultRow("Distracciones", "$distracciones")
                    ResultRow("Calidad de sesión", "%.1f".format(calidadFinal))
                    Spacer(modifier = Modifier.height(4.dp))
                    val rating = when {
                        calidadFinal > 1800 -> "🏆 Excepcional"
                        calidadFinal > 900 -> "⚡ Buena sesión"
                        calidadFinal > 300 -> "📈 En progreso"
                        else -> "📱 Muchas distracciones"
                    }
                    Text(rating, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showResult = false
                    timeLeft = duracionObjetivo * 60L
                    duracionRealSeg = 0L
                    distracciones = 0
                }) { Text("CERRAR", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) }
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