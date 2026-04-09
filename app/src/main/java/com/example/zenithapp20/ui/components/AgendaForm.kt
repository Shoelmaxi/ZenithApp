package com.example.zenithapp20.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.AgendaItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaForm(
    itemAEditar: AgendaItem? = null,
    onSave: (AgendaItem) -> Unit
) {
    var nombre by remember { mutableStateOf(itemAEditar?.nombre ?: "") }
    var desc by remember { mutableStateOf(itemAEditar?.descripcion ?: "") }
    var horaSeleccionada by remember { mutableStateOf(itemAEditar?.hora ?: "06:00")}
    var showTimePicker by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var diasError by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(initialHour = 6, initialMinute = 0, is24Hour = true)
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    // Usamos una lista normal de Strings para los días seleccionados
    val diasSeleccionados = remember {
        mutableStateListOf<String>().also { lista ->
            itemAEditar?.dias?.let { lista.addAll(it) }
        }
    }

    if (showTimePicker) {
        // CORRECCIÓN: Usamos TimePickerDialog o un BasicAlertDialog,
        // DatePickerDialog es para fechas, no para horas.
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour.toString().padStart(2, '0')
                    val m = timePickerState.minute.toString().padStart(2, '0')
                    horaSeleccionada = "$h:$m"
                    showTimePicker = false
                }) { Text("OK", color = Color(0xFF4CAF50)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("CANCELAR", color = Color.Gray) }
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

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 40.dp)) {
        Text("Nuevo Evento", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        CustomTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                nombreError = false
            },
            label = "Nombre"
        )
        if (nombreError) {
            Text("El nombre es obligatorio", color = Color.Red, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        CustomTextField(value = desc, onValueChange = { desc = it }, label = "Descripción")

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
            color = Color.Transparent,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF333333))
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hora: $horaSeleccionada", color = Color.White)
                Text("EDITAR", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            diasSemana.forEach { dia ->
                DayChip(dia, diasSeleccionados.contains(dia)) {
                    if (diasSeleccionados.contains(dia)) diasSeleccionados.remove(dia)
                    else diasSeleccionados.add(dia)
                }
            }
            if (diasError) {
                Text("Selecciona al menos un día", color = Color.Red, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                nombreError = nombre.isEmpty()
                diasError = diasSeleccionados.isEmpty()

                if (!nombreError && !diasError) {
                    onSave(
                        AgendaItem(
                            id = 0,
                            nombre = nombre,
                            descripcion = desc,
                            hora = horaSeleccionada,
                            dias = diasSeleccionados.toList()
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GUARDAR", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}