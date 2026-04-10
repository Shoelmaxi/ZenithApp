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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.TipoAgenda
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaForm(
    itemAEditar: AgendaItem? = null,
    onSave: (AgendaItem) -> Unit
) {
    var nombre by remember { mutableStateOf(itemAEditar?.nombre ?: "") }
    var desc by remember { mutableStateOf(itemAEditar?.descripcion ?: "") }
    var horaSeleccionada by remember { mutableStateOf(itemAEditar?.hora ?: "06:00") }
    var showTimePicker by remember { mutableStateOf(false) }
    var tipoSeleccionado by remember { mutableStateOf(itemAEditar?.tipo ?: TipoAgenda.RECURRENTE) }
    var nombreError by remember { mutableStateOf(false) }
    var diasError by remember { mutableStateOf(false) }
    var fechaError by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(initialHour = 6, initialMinute = 0, is24Hour = true)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = itemAEditar?.fechaEspecificaMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }

    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    val diasSeleccionados = remember {
        mutableStateListOf<String>().also { lista ->
            itemAEditar?.dias?.let { lista.addAll(it) }
        }
    }

    val fechaTexto = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(it))
    } ?: "Seleccionar fecha"

    if (showTimePicker) {
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
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CANCELAR", color = Color.Gray)
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            if (itemAEditar == null) "Nuevo Evento" else "Editar Evento",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        // SELECTOR DE TIPO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TipoAgenda.entries.forEach { tipo ->
                val isSelected = tipoSeleccionado == tipo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (isSelected) Color(0xFF4CAF50).copy(0.2f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { tipoSeleccionado = tipo },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tipo == TipoAgenda.RECURRENTE) "RECURRENTE" else "FECHA EXACTA",
                        color = if (isSelected) Color(0xFF4CAF50) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = nombre,
            onValueChange = { nombre = it; nombreError = false },
            label = "Nombre"
        )
        if (nombreError) {
            Text("El nombre es obligatorio", color = Color.Red, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
        CustomTextField(value = desc, onValueChange = { desc = it }, label = "Descripción")
        Spacer(modifier = Modifier.height(16.dp))

        // SELECTOR DE HORA
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
            color = Color.Transparent,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF333333))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hora: $horaSeleccionada", color = Color.White)
                Text("EDITAR", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SEGÚN EL TIPO muestra días o datepicker
        if (tipoSeleccionado == TipoAgenda.RECURRENTE) {
            Text("DÍAS DE LA SEMANA", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                diasSemana.forEach { dia ->
                    DayChip(dia, diasSeleccionados.contains(dia)) {
                        if (diasSeleccionados.contains(dia)) diasSeleccionados.remove(dia)
                        else diasSeleccionados.add(dia)
                        diasError = false
                    }
                }
            }
            if (diasError) {
                Text("Selecciona al menos un día", color = Color.Red, fontSize = 11.sp)
            }
        } else {
            Text("FECHA DEL EVENTO", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Text(fechaTexto, color = Color.White)
            }
            if (fechaError) {
                Text("Selecciona una fecha", color = Color.Red, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                nombreError = nombre.isEmpty()

                if (tipoSeleccionado == TipoAgenda.RECURRENTE) {
                    diasError = diasSeleccionados.isEmpty()
                    if (!nombreError && !diasError) {
                        onSave(AgendaItem(
                            id = itemAEditar?.id ?: 0,
                            nombre = nombre,
                            descripcion = desc,
                            hora = horaSeleccionada,
                            tipo = TipoAgenda.RECURRENTE,
                            dias = diasSeleccionados.toList(),
                            diasCompletados = itemAEditar?.diasCompletados ?: emptyList()
                        ))
                    }
                } else {
                    fechaError = datePickerState.selectedDateMillis == null
                    if (!nombreError && !fechaError) {
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = datePickerState.selectedDateMillis!!
                        }
                        val fechaFinal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 12)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        onSave(AgendaItem(
                            id = itemAEditar?.id ?: 0,
                            nombre = nombre,
                            descripcion = desc,
                            hora = horaSeleccionada,
                            tipo = TipoAgenda.FECHA_ESPECIFICA,
                            fechaEspecificaMillis = fechaFinal,
                            diasCompletados = itemAEditar?.diasCompletados ?: emptyList()
                        ))
                    }
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