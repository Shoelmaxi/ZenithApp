package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuResilienciaScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val registroHoy by viewModel.registroHoy.collectAsState()
    val historial by viewModel.registrosResiliencia.collectAsState()
    val rachaPoder by viewModel.rachaPoder.collectAsState()

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
            Column {
                Text("INCOMODIDAD VOLUNTARIA", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("Resiliencia", color = SecondaryText, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // BARRA DE RACHA DE PODER
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MainCardBackground,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (rachaPoder >= 7) Color(0xFFFFD700).copy(0.4f) else CardBorderColor
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RACHA DE PODER", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "%.1f".format(rachaPoder),
                                        color = Color(0xFFFFD700),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(" días", color = SecondaryText, fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp))
                                }
                            }
                            Text(
                                when {
                                    rachaPoder >= 14 -> "🔱 ÉLITE"
                                    rachaPoder >= 7 -> "⚡ FUERTE"
                                    rachaPoder >= 3 -> "🔥 CONSTRUYENDO"
                                    else -> "🌱 INICIO"
                                },
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra de progreso — max reference = 14
                        val barProgress = (rachaPoder / 14f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color.White.copy(0.05f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barProgress)
                                    .height(8.dp)
                                    .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Si fallas un día → racha × 0.5 (nunca a 0)",
                            color = SecondaryText,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // LOS 3 INTERRUPTORES
            item {
                Text("DESAFÍOS DE HOY", color = SecondaryText, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold)
            }

            item {
                ResilienciaSwitch(
                    emoji = "🚿",
                    titulo = "Ducha Fría",
                    subtitulo = "2-3 min de agua fría",
                    checked = registroHoy?.duchaFria == true,
                    onToggle = { viewModel.toggleDuchaFria() }
                )
            }
            item {
                ResilienciaSwitch(
                    emoji = "📵",
                    titulo = "Ayuno de Dopamina",
                    subtitulo = "Sin RRSS, streaming o juegos",
                    checked = registroHoy?.ayunoDopamina == true,
                    onToggle = { viewModel.toggleAyunoDopamina() }
                )
            }
            item {
                ResilienciaSwitch(
                    emoji = "🏋️",
                    titulo = "Entrenamiento bajo Resistencia",
                    subtitulo = "Cuando menos ganas tengas",
                    checked = registroHoy?.entrenamientoResistencia == true,
                    onToggle = { viewModel.toggleEntrenamiento() }
                )
            }

            // ESTADO DEL DÍA
            registroHoy?.let { r ->
                item {
                    val completados = r.completados
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = when (completados) {
                            3 -> Color(0xFF00C853).copy(0.08f)
                            0 -> Color.White.copy(0.02f)
                            else -> Color(0xFFFFD700).copy(0.05f)
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (completados) {
                                3 -> Color(0xFF00C853).copy(0.3f)
                                0 -> CardBorderColor
                                else -> Color(0xFFFFD700).copy(0.2f)
                            }
                        )
                    ) {
                        Text(
                            text = when (completados) {
                                3 -> "✅ DÍA PERFECTO — Racha protegida"
                                0 -> "⏳ Sin completar aún — La racha espera"
                                else -> "⚡ $completados/3 completados — Sigue"
                            },
                            color = when (completados) {
                                3 -> Color(0xFF00C853)
                                0 -> SecondaryText
                                else -> Color(0xFFFFD700)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // HISTORIAL
            if (historial.size > 1) {
                item {
                    Text("HISTORIAL", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                items(historial.drop(1).take(7)) { registro ->
                    val sdf = SimpleDateFormat("EEE dd/MM", Locale("es", "ES"))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(0.02f), RoundedCornerShape(10.dp))
                            .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sdf.format(Date(registro.fechaDia)).uppercase(),
                            color = SecondaryText, fontSize = 12.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (registro.duchaFria) "🚿" else "○", fontSize = 14.sp)
                            Text(if (registro.ayunoDopamina) "📵" else "○", fontSize = 14.sp)
                            Text(if (registro.entrenamientoResistencia) "🏋️" else "○", fontSize = 14.sp)
                        }
                        Text(
                            "${registro.completados}/3",
                            color = if (registro.diaPerfecto) Color(0xFF00C853) else SecondaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResilienciaSwitch(
    emoji: String,
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (checked) Color(0xFF00C853).copy(0.06f) else MainCardBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) Color(0xFF00C853).copy(0.4f) else CardBorderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(titulo, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(subtitulo, color = SecondaryText, fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00C853)
                )
            )
        }
    }
}