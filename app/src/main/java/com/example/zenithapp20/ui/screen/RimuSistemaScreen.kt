package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import com.example.zenithapp20.utils.BlackoutOverlayService
import com.example.zenithapp20.utils.ScreenTimeManager

@Composable
fun RimuSistemaScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val context     = LocalContext.current
    val sesiones    by viewModel.sesionesDeepWork.collectAsState()
    val analisis    by viewModel.analisis.collectAsState()

    // Estado blackout activo
    val blackoutActivo by viewModel.blackoutActivo.collectAsState()

    val promedioEficiencia = remember(sesiones) {
        if (sesiones.isEmpty()) 0
        else sesiones.map { it.eficienciaPct }.average().toInt()
    }
    val mejorSesion = remember(sesiones) {
        sesiones.maxOfOrNull { it.duracionRealMin } ?: 0
    }

    val totalAnalisis = analisis.size
    val promedioFocus = analisis
        .filter { it.completado && it.focusLevel > 0 }
        .map { it.focusLevel }.average()
        .let { if (it.isNaN()) null else it }

    // Stats Sombra Predictiva
    val fallosPatronHoy = remember(analisis) {
        viewModel.calcularFallosPatronDiaActual(analisis)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Column {
                Text(
                    "INGENIERÍA CONDUCTUAL",
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black
                )
                Text(
                    "Sistema de Alto Rendimiento",
                    color = SecondaryText, fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding      = PaddingValues(bottom = 40.dp)
        ) {

            // ── ANÁLISIS TRIPLE A ──────────────────────────────────────────
            item {
                SistemaCard(
                    emoji       = "🎯",
                    titulo      = "Análisis Triple A",
                    subtitulo   = "Acción · Análisis · Ajuste",
                    descripcion = if (totalAnalisis == 0) "Sin registros todavía"
                    else buildString {
                        append("$totalAnalisis registro${if (totalAnalisis != 1) "s" else ""}")
                        promedioFocus?.let { append(" · Focus medio: ${"%.1f".format(it)}") }
                    },
                    accentColor = Color(0xFF4CAF50),
                    badge       = if (totalAnalisis > 0) "$totalAnalisis" else null,
                    onClick     = { navController.navigate("rimu_triple_a") }
                )
            }

            // ── DEEP WORK ──────────────────────────────────────────────────
            item {
                SistemaCard(
                    emoji       = "🧠",
                    titulo      = "Deep Work",
                    subtitulo   = "Cronómetro de Enfoque Profundo",
                    descripcion = buildString {
                        append("${sesiones.size} sesiones")
                        if (sesiones.isNotEmpty()) {
                            append(" · Eficiencia media: $promedioEficiencia%")
                            append(" · Mejor: ${mejorSesion}min")
                        }
                    },
                    accentColor = Color(0xFF2196F3),
                    badge       = if (sesiones.isNotEmpty()) "${sesiones.size}" else null,
                    onClick     = { navController.navigate("rimu_deep_work") }
                )
            }

            // ── OPTIMIZADOR CIRCADIANO ─────────────────────────────────────
            item {
                SistemaCard(
                    emoji       = "🌙",
                    titulo      = "Optimizador Circadiano",
                    subtitulo   = "Cálculo de ciclos de sueño",
                    descripcion = "Basado en ciclos de 90 min + buffer de 15 min",
                    accentColor = Color(0xFF9C27B0),
                    badge       = null,
                    onClick     = { navController.navigate("rimu_sueno") }
                )
            }

            // ── MODO BLACKOUT ──────────────────────────────────────────────
            item {
                BlackoutCard(
                    activo    = blackoutActivo,
                    onToggle  = {
                        if (blackoutActivo) {
                            viewModel.desactivarBlackout(context)
                        } else {
                            viewModel.activarBlackout(context)
                        }
                    },
                    onConfig  = { navController.navigate("rimu_sueno") }
                )
            }

            // ── SOMBRA PREDICTIVA ──────────────────────────────────────────
            item {
                SombraPredictivaCard(
                    fallosHoy  = fallosPatronHoy,
                    onClick    = { navController.navigate("rimu_triple_a") }
                )
            }

            // ── AUDITORÍA DE TIEMPO ────────────────────────────────────────
            item {
                SistemaCard(
                    emoji       = "📊",
                    titulo      = "Auditoría de Tiempo",
                    subtitulo   = "Screen time interno",
                    descripcion = if (ScreenTimeManager.tienePermiso(context)) {
                        val total = remember { ScreenTimeManager.getTiempoDistracciones(context) }
                        val ms    = remember { total.sumOf { it.tiempoMs } }
                        if (ms < 60_000L) "Hoy: menos de 1 min de distracción ✅"
                        else "Hoy: ${ScreenTimeManager.tiempoATexto(ms)} en apps de distracción"
                    } else "Concede permiso para activar el rastreo",
                    accentColor = Color(0xFFFF4444),
                    badge       = null,
                    onClick     = { navController.navigate("rimu_screen_time") }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  BLACKOUT CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BlackoutCard(
    activo:   Boolean,
    onToggle: () -> Unit,
    onConfig: () -> Unit
) {
    val accentColor = if (activo) Color(0xFF9C27B0) else Color(0xFF555555)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = if (activo) Color(0xFF9C27B0).copy(0.08f) else MainCardBackground,
        shape    = RoundedCornerShape(20.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(accentColor.copy(0.1f), RoundedCornerShape(14.dp))
                        .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (activo) "🌑" else "🌙", fontSize = 24.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Modo Blackout",
                        color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black
                    )
                    Text(
                        if (activo) "ACTIVO — protocolo de sueño en curso"
                        else "Overlay de emergencia contra el scroll",
                        color      = accentColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Requiere permiso de overlay",
                        color = SecondaryText, fontSize = 10.sp
                    )
                }
                Switch(
                    checked         = activo,
                    onCheckedChange = { onToggle() },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor  = Color.White,
                        checkedTrackColor  = Color(0xFF9C27B0)
                    )
                )
            }
            if (!activo) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "La hora de activación se toma del Optimizador Circadiano. Configúrala en 🌙 Optimizador.",
                    color = SecondaryText.copy(0.6f), fontSize = 10.sp, lineHeight = 15.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SOMBRA PREDICTIVA CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SombraPredictivaCard(
    fallosHoy: Int,
    onClick:   () -> Unit
) {
    val accentColor = when {
        fallosHoy >= 3 -> Color(0xFFFF4444)
        fallosHoy >= 2 -> Color(0xFFFFD700)
        else           -> Color(0xFF4CAF50)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color    = accentColor.copy(0.05f),
        shape    = RoundedCornerShape(20.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.25f))
    ) {
        Row(
            modifier          = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accentColor.copy(0.1f), RoundedCornerShape(14.dp))
                    .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔮", fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Sombra Predictiva",
                    color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black
                )
                Text(
                    "Notificaciones basadas en tu historial",
                    color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    when {
                        fallosHoy >= 3 -> "⚠️ Patrón de fallo detectado para hoy ($fallosHoy veces en historial)"
                        fallosHoy >= 2 -> "⚡ $fallosHoy fallos históricos este día de la semana"
                        fallosHoy == 1 -> "1 fallo histórico registrado — dato insuficiente"
                        else           -> "Sin patrón de fallo para hoy. Buen historial."
                    },
                    color = SecondaryText, fontSize = 11.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SISTEMA CARD (igual que antes — se mantiene)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SistemaCard(
    emoji: String,
    titulo: String,
    subtitulo: String,
    descripcion: String,
    accentColor: Color,
    badge: String?,
    onClick: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color  = MainCardBackground,
        shape  = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.25f))
    ) {
        Row(
            modifier          = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accentColor.copy(0.1f), RoundedCornerShape(14.dp))
                    .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(subtitulo, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(3.dp))
                Text(descripcion, color = SecondaryText, fontSize = 11.sp)
            }
            badge?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color  = accentColor.copy(0.1f),
                    shape  = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.4f))
                ) {
                    Text(
                        it,
                        color      = accentColor,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}