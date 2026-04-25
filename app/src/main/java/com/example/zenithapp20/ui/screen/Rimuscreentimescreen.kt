package com.example.zenithapp20.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.utils.AppUsage
import com.example.zenithapp20.utils.Equivalencia
import com.example.zenithapp20.utils.ScreenTimeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuScreenTimeScreen(navController: NavController) {
    val context = LocalContext.current
    val tienePermiso = remember { ScreenTimeManager.tienePermiso(context) }

    // Datos cargados una sola vez al abrir la pantalla
    val appsDistraccion = remember {
        if (tienePermiso) ScreenTimeManager.getTiempoDistracciones(context)
        else emptyList()
    }
    val totalDistrMs = remember { appsDistraccion.sumOf { it.tiempoMs } }
    val totalPantallaMs = remember {
        if (tienePermiso) ScreenTimeManager.getTiempoTotalHoy(context) else 0L
    }
    val equivalencias = remember { ScreenTimeManager.calcularEquivalencias(totalDistrMs) }
    val appsPersonalizadas = remember {
        mutableStateListOf<String>().also {
            it.addAll(ScreenTimeManager.getAppsPersonalizadas(context))
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var paqueteInput by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = MainCardBackground,
            title = { Text("Añadir App", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "Ingresa el paquete exacto (ej: com.google.android.youtube)",
                        color = SecondaryText, fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = paqueteInput,
                        onValueChange = { paqueteInput = it },
                        label = { Text("Paquete", color = SecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF4444),
                            unfocusedBorderColor = CardBorderColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val pkg = paqueteInput.trim()
                    if (pkg.isNotBlank() && pkg.contains(".")) {
                        ScreenTimeManager.agregarAppPersonalizada(context, pkg)
                        appsPersonalizadas.clear()
                        appsPersonalizadas.addAll(ScreenTimeManager.getAppsPersonalizadas(context))
                    }
                    paqueteInput = ""
                    showAddDialog = false
                }) { Text("AÑADIR", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; paqueteInput = "" }) {
                    Text("CANCELAR", color = SecondaryText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Column {
                Text("AUDITORÍA DE TIEMPO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("¿Dónde va tu atención?", color = SecondaryText, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!tienePermiso) {
            PermisoCard { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {

                // ── RESUMEN GLOBAL ─────────────────────────────────────────
                item {
                    ResumenGlobalCard(
                        totalPantallaMs = totalPantallaMs,
                        totalDistrMs    = totalDistrMs
                    )
                }

                // ── EQUIVALENCIAS (el costo real) ─────────────────────────
                if (equivalencias.isNotEmpty()) {
                    item {
                        EquivalenciasCard(equivalencias = equivalencias, totalMs = totalDistrMs)
                    }
                }

                // ── APPS DE DISTRACCIÓN ────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "APPS DE DISTRACCIÓN",
                            color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFFFF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (appsDistraccion.isEmpty()) {
                    item {
                        Text(
                            "No has usado apps de distracción hoy. Bien hecho.",
                            color = Color(0xFF00C853), fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(appsDistraccion) { app ->
                        AppUsageRow(
                            app = app,
                            totalDistrMs = totalDistrMs
                        )
                    }
                }

                // ── APPS PERSONALIZADAS CONFIGURADAS ──────────────────────
                if (appsPersonalizadas.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "APPS PERSONALIZADAS MONITOREADAS",
                            color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    items(appsPersonalizadas) { pkg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(0.03f), RoundedCornerShape(10.dp))
                                .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pkg, color = SecondaryText, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    ScreenTimeManager.eliminarAppPersonalizada(context, pkg)
                                    appsPersonalizadas.clear()
                                    appsPersonalizadas.addAll(
                                        ScreenTimeManager.getAppsPersonalizadas(context)
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete, null,
                                    tint = Color(0xFFFF4444).copy(0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // ── APPS RASTREADAS POR DEFECTO ───────────────────────────
                item {
                    Spacer(Modifier.height(4.dp))
                    var expandida by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                            .clickable { expandida = !expandida }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Apps rastreadas por defecto (${ScreenTimeManager.APPS_DEFAULT.size})",
                                color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (expandida) "▲" else "▼",
                                color = SecondaryText, fontSize = 11.sp
                            )
                        }
                        if (expandida) {
                            Spacer(Modifier.height(10.dp))
                            ScreenTimeManager.APPS_DEFAULT.values.forEach { nombre ->
                                Text(
                                    "• $nombre",
                                    color = SecondaryText.copy(0.6f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes internos ──────────────────────────────────────────────────────

@Composable
private fun PermisoCard(onConceder: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFF4444).copy(0.07f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4444).copy(0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 36.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Permiso de Uso de Apps Necesario",
                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Para rastrear cuánto tiempo pasas en apps de distracción, necesitás conceder permiso de acceso de uso.",
                color = SecondaryText, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onConceder,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("CONCEDER PERMISO", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ResumenGlobalCard(totalPantallaMs: Long, totalDistrMs: Long) {
    val porcentajeDistraccion = if (totalPantallaMs > 0)
        ((totalDistrMs.toFloat() / totalPantallaMs) * 100).toInt().coerceIn(0, 100)
    else 0

    val colorSemaforo = when {
        porcentajeDistraccion >= 40 -> Color(0xFFFF4444)
        porcentajeDistraccion >= 20 -> Color(0xFFFFD700)
        else                        -> Color(0xFF00C853)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = colorSemaforo.copy(0.06f),
        shape    = RoundedCornerShape(20.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, colorSemaforo.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                "HOY REGALASTE",
                color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    ScreenTimeManager.tiempoATexto(totalDistrMs),
                    color = colorSemaforo, fontSize = 38.sp, fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "a apps de distracción",
                    color = SecondaryText, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Barra total de pantalla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pantalla total hoy", color = SecondaryText, fontSize = 11.sp)
                Text(
                    ScreenTimeManager.tiempoATexto(totalPantallaMs),
                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(8.dp)
                    .background(Color.White.copy(0.06f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(porcentajeDistraccion / 100f)
                        .height(8.dp)
                        .background(colorSemaforo, RoundedCornerShape(4.dp))
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "$porcentajeDistraccion% de tu pantalla fue distracción",
                color = colorSemaforo.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Mensaje según nivel
            val mensaje = when {
                totalDistrMs < 30 * 60_000L -> "✅ Día limpio. Menos de 30 minutos de distracción."
                totalDistrMs < 60 * 60_000L -> "⚡ Controlable. Menos de 1 hora. Podés bajar más."
                totalDistrMs < 120 * 60_000L -> "⚠️ ${ScreenTimeManager.tiempoATexto(totalDistrMs)} perdidas. ¿Valió la pena el intercambio?"
                else -> "🚨 Más de 2 horas regaladas hoy. Eso no vuelve."
            }
            Text(mensaje, color = Color.White.copy(0.75f), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun EquivalenciasCard(equivalencias: List<Equivalencia>, totalMs: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = Color(0xFFFFD700).copy(0.05f),
        shape    = RoundedCornerShape(20.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(0.25f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "EN VEZ DE ESO, PODRÍAS HABER...",
                color = Color(0xFFFFD700), fontSize = 10.sp,
                fontWeight = FontWeight.Black, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            equivalencias.forEach { eq ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(eq.emoji, fontSize = 16.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(eq.texto, color = Color.White.copy(0.85f), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun AppUsageRow(app: AppUsage, totalDistrMs: Long) {
    val proporcion = if (totalDistrMs > 0)
        (app.tiempoMs.toFloat() / totalDistrMs).coerceIn(0f, 1f)
    else 0f

    val color = when {
        proporcion >= 0.4f -> Color(0xFFFF4444)
        proporcion >= 0.2f -> Color(0xFFFFD700)
        else               -> SecondaryText
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = color.copy(0.04f),
        shape    = RoundedCornerShape(14.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    app.nombre,
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    ScreenTimeManager.tiempoATexto(app.tiempoMs),
                    color = color, fontSize = 15.sp, fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(4.dp)
                    .background(Color.White.copy(0.05f), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(proporcion)
                        .height(4.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${(proporcion * 100).toInt()}% de tu distracción total",
                color = color.copy(0.7f), fontSize = 10.sp
            )
        }
    }
}