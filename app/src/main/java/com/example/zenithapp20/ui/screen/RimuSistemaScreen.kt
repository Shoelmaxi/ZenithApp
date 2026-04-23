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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel

@Composable
fun RimuSistemaScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val sesiones    by viewModel.sesionesDeepWork.collectAsState()
    val analisis    by viewModel.analisis.collectAsState()

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
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SISTEMA CARD
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