package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.zenithapp20.data.model.FactorPositivo
import com.example.zenithapp20.data.model.FrictionFactor
import com.example.zenithapp20.data.model.RazonNoCompletado
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuTripleAScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val analisis by viewModel.analisis.collectAsState()

    // Stats generales
    val completados  = analisis.count { it.completado }
    val noCompletados = analisis.count { !it.completado }
    val promedioFocus = analisis
        .filter { it.completado && it.focusLevel > 0 }
        .map { it.focusLevel }.average()
        .let { if (it.isNaN()) null else it }

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
                    "ANÁLISIS TRIPLE A",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Acción · Análisis · Ajuste",
                    color = SecondaryText,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (analisis.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin registros todavía",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Completa un hábito y llena el análisis\npara ver tus datos aquí.",
                        color = SecondaryText,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {

                // ── Stats globales ────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlobalStat(
                            label = "COMPLETADOS",
                            value = "$completados",
                            color = Color(0xFF00C853),
                            modifier = Modifier.weight(1f)
                        )
                        GlobalStat(
                            label = "NO HECHOS",
                            value = "$noCompletados",
                            color = Color(0xFFFF4444),
                            modifier = Modifier.weight(1f)
                        )
                        GlobalStat(
                            label = "FOCUS MEDIO",
                            value = promedioFocus?.let { "%.1f".format(it) } ?: "—",
                            color = when {
                                promedioFocus == null  -> SecondaryText
                                promedioFocus >= 8     -> Color(0xFF00C853)
                                promedioFocus >= 5     -> Color(0xFFFFD700)
                                else                   -> Color(0xFFFF4444)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Historial con filtro por día ──────────────────────────
                item {
                    AAAHistorialPorDia(analisis = analisis)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Historial con selector de día
// ─────────────────────────────────────────────────────────────────────────────

private data class DiaFiltro(val label: String, val inicioMillis: Long)

@Composable
private fun AAAHistorialPorDia(analisis: List<AnalisisHabito>) {
    val accentColor = Color(0xFF4CAF50)

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
                label = if (offset == 0) "Hoy" else if (offset == 1) "Ayer"
                else sdf.format(cal.time).replaceFirstChar { it.uppercase() },
                inicioMillis = cal.timeInMillis
            )
        }
    }

    var diaSeleccionado by remember { mutableStateOf(dias.first()) }

    val registrosDia = remember(analisis, diaSeleccionado) {
        val fin = diaSeleccionado.inicioMillis + 86_400_000L
        analisis
            .filter { it.fechaMillis >= diaSeleccionado.inicioMillis && it.fechaMillis < fin }
            .sortedByDescending { it.fechaMillis }
    }

    val completadosHoy  = registrosDia.count { it.completado }
    val noCompletadoHoy = registrosDia.count { !it.completado }
    val focusPromedioHoy = registrosDia
        .filter { it.completado && it.focusLevel > 0 }
        .map { it.focusLevel }.average()
        .let { if (it.isNaN()) null else it }

    Column {
        // Selector de días
        Text(
            "HISTORIAL POR DÍA",
            color = SecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

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

        // Mini-stats del día
        if (registrosDia.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlobalStat("COMPLETADOS", "$completadosHoy", Color(0xFF00C853), Modifier.weight(1f))
                GlobalStat("NO HECHOS", "$noCompletadoHoy", Color(0xFFFF4444), Modifier.weight(1f))
                GlobalStat(
                    "FOCUS",
                    focusPromedioHoy?.let { "%.1f".format(it) } ?: "—",
                    when {
                        focusPromedioHoy == null  -> SecondaryText
                        focusPromedioHoy >= 8     -> Color(0xFF00C853)
                        focusPromedioHoy >= 5     -> Color(0xFFFFD700)
                        else                      -> Color(0xFFFF4444)
                    },
                    Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Lista de registros
        if (registrosDia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
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

// ─────────────────────────────────────────────────────────────────────────────
//  Item de registro AAA
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

            // Nombre + hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (registro.completado) "✅" else "❌", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        registro.habitoNombre,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Text(sdf.format(Date(registro.fechaMillis)), color = SecondaryText, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (registro.completado) {
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
                    BadgePill("FOCUS ${registro.focusLevel}/10", focusColor)

                    // Factor positivo (si existe)
                    if (registro.factorPositivo.isNotBlank()) {
                        runCatching { FactorPositivo.valueOf(registro.factorPositivo) }
                            .getOrNull()?.let {
                                BadgePill("${it.emoji} ${it.label}", Color(0xFF00C853).copy(0.85f))
                            }
                    }

                    // Factor de fricción (si existe)
                    if (registro.frictionFactor.isNotBlank()) {
                        runCatching { FrictionFactor.valueOf(registro.frictionFactor) }
                            .getOrNull()?.let {
                                BadgePill("${it.emoji} ${it.label}", Color(0xFFFFD700).copy(0.85f))
                            }
                    }
                }
            } else {
                // Razón de no completado
                if (registro.razonNoCompletado.isNotBlank()) {
                    runCatching { RazonNoCompletado.valueOf(registro.razonNoCompletado) }
                        .getOrNull()?.let {
                            BadgePill("${it.emoji} ${it.label}", Color(0xFFFF4444).copy(0.85f))
                        }
                }
            }

            // Nota
            if (registro.adjustmentNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(0.03f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("📝", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
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

@Composable
private fun BadgePill(texto: String, color: Color) {
    Surface(
        color = color.copy(0.12f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.4f))
    ) {
        Text(
            texto,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun GlobalStat(label: String, value: String, color: Color, modifier: Modifier) {
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