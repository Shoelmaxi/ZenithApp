package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuSuenoScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    var horaDespertar by remember { mutableIntStateOf(7) }
    var minDespertar by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    var bedtimeSeleccionado by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }
    var notifProgramada by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = horaDespertar, initialMinute = minDespertar, is24Hour = true
    )

    val opciones = viewModel.calcularHorasDormir(horaDespertar, minDespertar)

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    horaDespertar = timePickerState.hour
                    minDespertar = timePickerState.minute
                    bedtimeSeleccionado = null
                    notifProgramada = false
                    showTimePicker = false
                }) { Text("OK", color = Color(0xFF4CAF50)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("CANCELAR", color = SecondaryText) }
            },
            containerColor = Color(0xFF1A1A1A),
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFF2A2A2A),
                            selectorColor = Color(0xFF4CAF50),
                            containerColor = Color(0xFF1A1A1A)
                        )
                    )
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
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Column {
                Text("OPTIMIZADOR CIRCADIANO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("Ciclos de sueño de 90 min", color = SecondaryText, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // HORA DE DESPERTAR
        Text("¿A QUÉ HORA NECESITAS DESPERTAR?", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            color = MainCardBackground,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.5f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "%02d:%02d".format(horaDespertar, minDespertar),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
                Text("CAMBIAR", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("MEJORES HORARIOS PARA DORMIR", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // 3 OPCIONES
        opciones.forEachIndexed { idx, (h, m, ciclos) ->
            val isSelected = bedtimeSeleccionado == Triple(h, m, ciclos)
            val etiqueta = when (idx) {
                0 -> "⭐ IDEAL" to Color(0xFF4CAF50)
                1 -> "✅ BUENO" to Color(0xFFFFD700)
                else -> "⚡ MÍNIMO" to SecondaryText
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        bedtimeSeleccionado = Triple(h, m, ciclos)
                        notifProgramada = false
                    },
                color = if (isSelected) Color(0xFF4CAF50).copy(0.08f) else MainCardBackground,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF4CAF50).copy(0.5f) else CardBorderColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "%02d:%02d".format(h, m),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "$ciclos ciclos · ${ciclos * 90 / 60}h ${(ciclos * 90) % 60}min de sueño",
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        color = etiqueta.second.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, etiqueta.second.copy(0.4f))
                    ) {
                        Text(
                            etiqueta.first,
                            color = etiqueta.second,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // BOTÓN NOTIFICACIÓN
        bedtimeSeleccionado?.let { (h, m, _) ->
            Spacer(modifier = Modifier.height(24.dp))

            if (!notifProgramada) {
                Button(
                    onClick = {
                        viewModel.programarNotificacionSueno(h, m)
                        notifProgramada = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "🔔 AVÍSAME 60 MIN ANTES (%02d:%02d)".format(
                            if (m < 60) (h - 1 + 24) % 24 else h,
                            (m - 60 + 60) % 60
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50).copy(0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.3f))
                ) {
                    Text(
                        "✅ Checklist de desconexión programado para las %02d:%02d".format(
                            if (m < 60) (h - 1 + 24) % 24 else h,
                            (m - 60 + 60) % 60
                        ),
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}