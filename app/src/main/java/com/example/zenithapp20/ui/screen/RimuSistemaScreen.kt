package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.AnalisisHabito
import com.example.zenithapp20.data.model.FrictionFactor
import com.example.zenithapp20.data.model.RazonNoCompletado
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuSistemaScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val rachaPoder  by viewModel.rachaPoder.collectAsState()
    val sesiones    by viewModel.sesionesDeepWork.collectAsState()
    val analisis    by viewModel.analisis.collectAsState()

    val promedioEficiencia = remember(sesiones) {
        if (sesiones.isEmpty()) 0
        else sesiones.map { it.eficienciaPct }.average().toInt()
    }
    val mejorSesion = remember(sesiones) {
        sesiones.maxOfOrNull { it.duracionRealMin } ?: 0
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
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Sistema de Alto Rendimiento",
                    color = SecondaryText,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── CARD ANÁLISIS TRIPLE A — con historial ────────────────────
            item {
                AAAHistorialCard(analisis = analisis)
            }

            // ── DEEP WORK ────────────────────────────────────────────────
            item {
                SistemaCard(
                    emoji = "🧠",
                    titulo = "Deep Work",
                    subtitulo = "Cronómetro de Enfoque Profundo",
                    descripcion = buildString {
                        append("${sesiones.size} sesiones")
                        if (sesiones.isNotEmpty()) {
                            append(" · Eficiencia media: $promedioEficiencia%")
                            append(" · Mejor: ${mejorSesion}min")
                        }
                    },
                    accentColor = Color(0xFF2196F3),
                    badge = if (sesiones.isNotEmpty()) "${sesiones.size}" else null,
                    onClick = { navController.navigate("rimu_deep_work") }
                )
            }

            // ── OPTIMIZADOR CIRCADIANO ────────────────────────────────────
            item {
                SistemaCard(
                    emoji = "🌙",
                    titulo = "Optimizador Circadiano",
                    subtitulo = "Cálculo de ciclos de sueño",
                    descripcion = "Basado en ciclos de 90 min + buffer de 15 min",
                    accentColor = Color(0xFF9C27B0),
                    badge = null,
                    onClick = { navController.navigate("rimu_sueno") }
                )
            }

            // ── INCOMODIDAD VOLUNTARIA ────────────────────────────────────
            item {
                SistemaCard(
                    emoji = "🔱",
                    titulo = "Incomodidad Voluntaria",
                    subtitulo = "Tracker de Resiliencia",
                    descripcion = "Racha de Poder: ${"%.1f".format(rachaPoder)} días",
                    accentColor = Color(0xFFFFD700),
                    badge = if (rachaPoder >= 7) "⚡ FUERTE" else null,
                    onClick = { navController.navigate("rimu_resiliencia") }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CARD ANÁLISIS TRIPLE A — historial con filtro por día
// ─────────────────────────────────────────────────────────────────────────────

private data class DiaFiltro(
    val label: String,       // "Hoy", "Ayer", "Lun 14", etc.
    val inicioMillis: Long,
    val esHoy: Boolean = false
)

@Composable
private fun AAAHistorialCard(analisis: List<AnalisisHabito>) {
    val accentColor = Color(0xFF4CAF50)

    // Generar últimos 7 días como opciones de filtro
    val dias = remember {
        val sdf = SimpleDateFormat("EEE d", Locale("es", "ES"))
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        (0..6).map { offset ->
            val cal = hoy.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            DiaFiltro(
                label       = if (offset == 0) "Hoy" else if (offset == 1) "Ayer"
                else sdf.format(cal.time).replaceFirstChar { it.uppercase() },
                inicioMillis = cal.timeInMillis,
                esHoy        = offset == 0
            )
        }
    }

    var diaSeleccionado by remember { mutableStateOf(dias.first()) }

    // Registros del día seleccionado, ordenados del más reciente al más antiguo
    val registrosDia = remember(analisis, diaSeleccionado) {
        val fin = diaSeleccionado.inicioMillis + 86_400_000L
        analisis
            .filter { it.fechaMillis >= diaSeleccionado.inicioMillis && it.fechaMillis < fin }
            .sortedByDescending { it.fechaMillis }
    }

    // Stats rápidas del día
    val completadosHoy  = registrosDia.count { it.completado }
    val noCompletadoHoy = registrosDia.count { !it.completado }
    val focusPromedioHoy = registrosDia
        .filter { it.completado && it.focusLevel > 0 }
        .map { it.focusLevel }.average()
        .let { if (it.isNaN()) null else it }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MainCardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.25f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Cabecera ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "ANÁLISIS TRIPLE A",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Acción · Análisis · Ajuste",
                            color = SecondaryText,
                            fontSize = 10.sp
                        )
                    }
                }
                // Total de registros
                Surface(
                    color = accentColor.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.3f))
                ) {
                    Text(
                        "${analisis.size} registros",
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Selector de día ─────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(dias) { dia ->
                    val isSelected = dia == diaSeleccionado
                    Surface(
                        modifier = Modifier.clickable { diaSeleccionado = dia },
                        color = if (isSelected) accentColor.copy(0.15f) else Color.White.copy(0.04f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else CardBorderColor
                        )
                    ) {
                        Text(
                            text = dia.label,
                            color = if (isSelected) accentColor else SecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Mini-stats del día seleccionado ─────────────────────────
            if (registrosDia.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStat(
                        label = "COMPLETADOS",
                        value = "$completadosHoy",
                        color = Color(0xFF00C853),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStat(
                        label = "NO HECHOS",
                        value = "$noCompletadoHoy",
                        color = Color(0xFFFF4444),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStat(
                        label = "FOCUS MEDIO",
                        value = focusPromedioHoy?.let { "%.1f".format(it) } ?: "—",
                        color = when {
                            focusPromedioHoy == null     -> SecondaryText
                            focusPromedioHoy >= 8        -> Color(0xFF00C853)
                            focusPromedioHoy >= 5        -> Color(0xFFFFD700)
                            else                         -> Color(0xFFFF4444)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(0.06f))
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ── Lista de registros del día ───────────────────────────────
            if (registrosDia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin registros para ${diaSeleccionado.label.lowercase()}",
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    registrosDia.forEach { registro ->
                        AAARegistroItem(registro = registro)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ITEM individual de un registro AAA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AAARegistroItem(registro: AnalisisHabito) {
    val colorEstado = if (registro.completado) Color(0xFF00C853) else Color(0xFFFF4444)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorEstado.copy(0.05f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorEstado.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Fila superior: nombre + hora + estado ───────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (registro.completado) "✅" else "❌",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        registro.habitoNombre,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Text(
                    sdf.format(Date(registro.fechaMillis)),
                    color = SecondaryText,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Fila de métricas ────────────────────────────────────────
            if (registro.completado) {
                // Focus + fricción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de focus
                    val focusColor = when {
                        registro.focusLevel >= 8 -> Color(0xFF00C853)
                        registro.focusLevel >= 5 -> Color(0xFFFFD700)
                        else                     -> Color(0xFFFF4444)
                    }
                    Surface(
                        color = focusColor.copy(0.12f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, focusColor.copy(0.4f))
                    ) {
                        Text(
                            "FOCUS ${registro.focusLevel}/10",
                            color = focusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Badge de fricción si existe
                    val friction = runCatching {
                        FrictionFactor.valueOf(registro.frictionFactor)
                    }.getOrNull()
                    friction?.let {
                        Surface(
                            color = Color(0xFFFFD700).copy(0.08f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFFFFD700).copy(0.3f)
                            )
                        ) {
                            Text(
                                "${it.emoji} ${it.label}",
                                color = Color(0xFFFFD700).copy(0.85f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            } else {
                // Razón de no completado
                val razon = runCatching {
                    RazonNoCompletado.valueOf(registro.razonNoCompletado)
                }.getOrNull()

                razon?.let {
                    Surface(
                        color = Color(0xFFFF4444).copy(0.08f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Color(0xFFFF4444).copy(0.3f)
                        )
                    ) {
                        Text(
                            "${it.emoji} ${it.label}",
                            color = Color(0xFFFF4444).copy(0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // ── Nota de ajuste ───────────────────────────────────────────
            if (registro.adjustmentNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(0.03f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "📝",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        registro.adjustmentNote,
                        color = Color.White.copy(0.75f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(0.07f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(label, color = SecondaryText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SISTEMA CARD (genérica, sin cambios)
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
        color = MainCardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.25f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
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
                Surface(
                    color = accentColor.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.4f))
                ) {
                    Text(
                        it,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}