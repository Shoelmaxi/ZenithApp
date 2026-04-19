package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel

private const val PREFS_SUENO = "zenith_sueno_prefs"
private const val KEY_NOTIF_HORA = "notif_hora"
private const val KEY_NOTIF_MIN  = "notif_min"
private const val KEY_NOTIF_SET  = "notif_programada"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuSuenoScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val context = LocalContext.current

    // FIX: restore persisted state so user sees what they scheduled after leaving the screen
    val prefs = remember { context.getSharedPreferences(PREFS_SUENO, android.content.Context.MODE_PRIVATE) }

    var horaDespertar by remember { mutableIntStateOf(7) }
    var minDespertar  by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    var bedtimeSeleccionado by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }

    // FIX: persist whether a notification was scheduled and for what time
    var notifProgramada by remember {
        mutableStateOf(prefs.getBoolean(KEY_NOTIF_SET, false))
    }
    var notifHoraGuardada by remember {
        mutableIntStateOf(prefs.getInt(KEY_NOTIF_HORA, -1))
    }
    var notifMinGuardado by remember {
        mutableIntStateOf(prefs.getInt(KEY_NOTIF_MIN, -1))
    }

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
                    minDespertar  = timePickerState.minute
                    bedtimeSeleccionado = null
                    showTimePicker = false
                }) { Text("OK", color = Color(0xFF4CAF50)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CANCELAR", color = SecondaryText)
                }
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

        // FIX: show active notification banner if one is already scheduled
        if (notifProgramada && notifHoraGuardada >= 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50).copy(0.08f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "🔔 Notificación programada",
                            color = Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Aviso a las %02d:%02d".format(notifHoraGuardada, notifMinGuardado),
                            color = SecondaryText, fontSize = 11.sp
                        )
                    }
                    TextButton(onClick = {
                        // cancel
                        prefs.edit()
                            .putBoolean(KEY_NOTIF_SET, false)
                            .apply()
                        notifProgramada = false
                        notifHoraGuardada = -1
                        notifMinGuardado = -1
                    }) {
                        Text("CANCELAR", color = Color(0xFFFF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Wake time selector
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
                    color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black
                )
                Text("CAMBIAR", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("MEJORES HORARIOS PARA DORMIR", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        opciones.forEachIndexed { idx, (h, m, ciclos) ->
            val isSelected = bedtimeSeleccionado == Triple(h, m, ciclos)
            val (etiqueta, accentColor) = when (idx) {
                0 -> "⭐ IDEAL"  to Color(0xFF4CAF50)
                1 -> "✅ BUENO"  to Color(0xFFFFD700)
                else -> "⚡ MÍNIMO" to SecondaryText
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { bedtimeSeleccionado = Triple(h, m, ciclos) },
                color = if (isSelected) accentColor.copy(0.08f) else MainCardBackground,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) accentColor.copy(0.5f) else CardBorderColor
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
                            color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black
                        )
                        Text(
                            "$ciclos ciclos · ${ciclos * 90 / 60}h ${(ciclos * 90) % 60}min de sueño",
                            color = SecondaryText, fontSize = 12.sp
                        )
                    }
                    Surface(
                        color = accentColor.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.4f))
                    ) {
                        Text(
                            etiqueta, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Schedule notification button
        bedtimeSeleccionado?.let { (h, m, _) ->
            Spacer(modifier = Modifier.height(24.dp))

            // FIX: correct "60 min before h:m" calculation
            val totalMinBed = h * 60 + m
            val totalMinNotif = (totalMinBed - 60 + 24 * 60) % (24 * 60)
            val notifH = totalMinNotif / 60
            val notifM = totalMinNotif % 60

            if (!notifProgramada) {
                Button(
                    onClick = {
                        viewModel.programarNotificacionSueno(h, m)
                        // FIX: persist so user sees it after coming back
                        prefs.edit()
                            .putBoolean(KEY_NOTIF_SET, true)
                            .putInt(KEY_NOTIF_HORA, notifH)
                            .putInt(KEY_NOTIF_MIN, notifM)
                            .apply()
                        notifProgramada = true
                        notifHoraGuardada = notifH
                        notifMinGuardado  = notifM
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "🔔 AVÍSAME A LAS %02d:%02d".format(notifH, notifM),
                        color = Color.White, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, fontSize = 14.sp
                    )
                }
            }
        }
    }
}