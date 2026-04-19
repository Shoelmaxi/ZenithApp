package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.zenithapp20.data.model.FrictionFactor
import com.example.zenithapp20.ui.components.CardActividadDiaria
import com.example.zenithapp20.ui.components.CardRadarDisciplina
import com.example.zenithapp20.ui.components.SeguimientoSemanalHabitos
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel

@Composable
fun RimuHabitsStatsScreen(
    navController: NavController,
    habitosViewModel: HabitosViewModel,
    icViewModel: IngenieriaConductualViewModel
) {
    val listaHabitos     by habitosViewModel.habitos.collectAsState()
    val analisis         by icViewModel.analisis.collectAsState()
    val sesionesDeepWork by icViewModel.sesionesDeepWork.collectAsState()

    // ── AAA stats ─────────────────────────────────────────────────────────
    val promedioFocusFloat = analisis
        .map { it.focusLevel }.average()
        .let { if (it.isNaN()) 0f else it.toFloat() }

    val promedioFocusStr = if (analisis.isEmpty()) null
    else "%.1f".format(promedioFocusFloat)

    val diasAltoFocus = analisis.count { it.focusLevel >= 8 }

    val habitoMasFriccion = analisis
        .filter { it.focusLevel < 7 }
        .groupBy { it.habitoNombre }
        .maxByOrNull { it.value.size }?.key

    val factorMasFrecuente = analisis
        .groupBy { it.frictionFactor }
        .maxByOrNull { it.value.size }
        ?.let { (name, _) -> runCatching { FrictionFactor.valueOf(name) }.getOrNull() }

    val ultimoPorHabito = analisis
        .groupBy { it.habitoNombre }
        .mapValues { (_, list) -> list.maxByOrNull { it.fechaMillis }!! }

    // ── Deep Work stats ───────────────────────────────────────────────────
    val promedioEficiencia = if (sesionesDeepWork.isEmpty()) 0
    else sesionesDeepWork.map { it.eficienciaPct }.average().toInt()

    val mejorSesionMin = sesionesDeepWork.maxOfOrNull { it.duracionRealMin } ?: 0

    // ── UI ────────────────────────────────────────────────────────────────
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
            Text("RENDIMIENTO", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Existing habit charts ─────────────────────────────────────
            item { CardRadarDisciplina(listaHabitos = listaHabitos) }
            item { SeguimientoSemanalHabitos(listaHabitos = listaHabitos) }
            item { CardActividadDiaria(listaHabitos = listaHabitos) }

            // ── Section separator ─────────────────────────────────────────
            item {
                Text(
                    "INGENIERÍA CONDUCTUAL",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // ── AAA card — ALWAYS visible ─────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(0.02f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF4CAF50).copy(0.25f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🎯 ANÁLISIS AAA",
                                color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Black
                            )
                            Text(
                                if (analisis.isEmpty()) "Sin datos aún"
                                else "${analisis.size} registro${if (analisis.size != 1) "s" else ""}",
                                color = SecondaryText, fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (analisis.isEmpty()) {
                            Text(
                                "Completa un hábito y llena el análisis AAA para ver tu focus aquí.",
                                color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp
                            )
                        } else {
                            // Focus bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Focus promedio", color = SecondaryText, fontSize = 12.sp)
                                Text(
                                    promedioFocusStr ?: "-",
                                    color = focusColor(promedioFocusFloat),
                                    fontSize = 16.sp, fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressBar(
                                progress = (promedioFocusFloat / 10f).coerceIn(0f, 1f),
                                color = focusColor(promedioFocusFloat)
                            )

                            if (diasAltoFocus > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Días con focus ≥ 8", color = SecondaryText, fontSize = 12.sp)
                                    Text(
                                        "$diasAltoFocus",
                                        color = Color(0xFF00C853), fontSize = 14.sp, fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            habitoMasFriccion?.let { nombre ->
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(0.06f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Hábito con más fricción", color = SecondaryText, fontSize = 11.sp)
                                        Text(nombre, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("⚠️", fontSize = 18.sp)
                                }
                            }

                            factorMasFrecuente?.let { factor ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Factor más frecuente", color = SecondaryText, fontSize = 11.sp)
                                        Text(
                                            "${factor.emoji} ${factor.label}",
                                            color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Per-habit last focus — only if data exists ────────────────
            if (ultimoPorHabito.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(0.02f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "ÚLTIMO FOCUS POR HÁBITO",
                                color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ultimoPorHabito.entries.forEach { (nombre, a) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        nombre, color = Color.White, fontSize = 12.sp,
                                        modifier = Modifier.weight(1f), maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(6.dp)
                                            .background(Color.White.copy(0.05f), RoundedCornerShape(3.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(a.focusLevel / 10f)
                                                .height(6.dp)
                                                .background(focusColor(a.focusLevel.toFloat()), RoundedCornerShape(3.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${a.focusLevel}",
                                        color = focusColor(a.focusLevel.toFloat()),
                                        fontSize = 12.sp, fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Deep Work card — ALWAYS visible ──────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(0.02f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF2196F3).copy(0.25f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🧠 DEEP WORK",
                                color = Color(0xFF2196F3), fontSize = 12.sp, fontWeight = FontWeight.Black
                            )
                            Text(
                                if (sesionesDeepWork.isEmpty()) "Sin sesiones aún"
                                else "${sesionesDeepWork.size} sesión${if (sesionesDeepWork.size != 1) "es" else ""}",
                                color = SecondaryText, fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (sesionesDeepWork.isEmpty()) {
                            Text(
                                "Completa tu primera sesión de Deep Work para ver estadísticas aquí.",
                                color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DwStat("Sesiones", "${sesionesDeepWork.size}", Color.White, Modifier.weight(1f))
                                DwStat(
                                    "Eficiencia", "$promedioEficiencia%",
                                    when {
                                        promedioEficiencia >= 80 -> Color(0xFF00C853)
                                        promedioEficiencia >= 60 -> Color(0xFFFFD700)
                                        else -> Color(0xFFFF4444)
                                    },
                                    Modifier.weight(1f)
                                )
                                DwStat("Mejor", "${mejorSesionMin}min", Color(0xFF2196F3), Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun focusColor(value: Float) = when {
    value >= 8 -> Color(0xFF00C853)
    value >= 5 -> Color(0xFFFFD700)
    else       -> Color(0xFFFF4444)
}

@Composable
private fun LinearProgressBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(Color.White.copy(0.05f), RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun DwStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}