package com.example.zenithapp20.ui.components

import androidx.benchmark.traceprocessor.Row
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.Prioridad
import com.example.zenithapp20.data.model.TareaItem
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.PrimaryText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaForm(tareaAEditar: TareaItem? = null,
              onSave: (TareaItem) -> Unit) {
    var nombre by remember { mutableStateOf(tareaAEditar?.nombre ?: "") }
    var desc by remember { mutableStateOf(tareaAEditar?.descripcion ?: "") }
    var prioridadSelected by remember { mutableStateOf(tareaAEditar?.prioridad ?: Prioridad.BAJA) }
    var nombreError by remember { mutableStateOf(false) }
    var fechaError by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = tareaAEditar?.fechaLimiteMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }

    // --- FIX VISUAL EN EL FORMULARIO ---
    // Forzamos UTC aquí también para que el texto del botón no mienta
    val fechaTexto = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(it))
    } ?: "Seleccionar fecha límite"

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 40.dp)) {
        Text(
            if (tareaAEditar == null) "Nueva Tarea" else "Editar Tarea",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        CustomTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre de la tarea")
        if (nombreError) {
            Text("El nombre es obligatorio", color = Color.Red, fontSize = 11.sp)
        }
        CustomTextField(value = desc, onValueChange = { desc = it }, label = "Notas/Descripción")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Text(fechaTexto, color = PrimaryText)
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("OK", color = Color.Green) }
                }
            ) { DatePicker(state = datePickerState) }
        }
        if (fechaError) {
            Text("Selecciona una fecha límite", color = Color.Red, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "PRIORIDAD",
            color = Color.White.copy(0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Prioridad.entries.forEach { prioridad ->
                val isSelected = prioridadSelected == prioridad
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { prioridadSelected = prioridad },
                    color = if (isSelected) prioridad.color.copy(0.15f) else Color.White.copy(0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) prioridad.color else Color.White.copy(0.1f)
                    )
                ) {
                    Text(
                        text = prioridad.nombre.uppercase(),
                        color = if (isSelected) prioridad.color else Color.White.copy(0.5f),
                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                nombreError = nombre.isEmpty()
                fechaError = datePickerState.selectedDateMillis == null
                val seleccionMillis = datePickerState.selectedDateMillis

                if (!nombreError && !fechaError) {

                    // 1. Extraemos el día exacto que el usuario tocó en el calendario (está en UTC)
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        if (seleccionMillis != null) {
                            timeInMillis = seleccionMillis
                        }
                    }

                    // 2. Lo guardamos en un Calendar local pero forzando los mismos números de día/mes/año
                    // Usamos las 12:00 PM para que esté lejos de cualquier borde horario que cause saltos
                    val fechaFinal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    onSave(TareaItem(
                        nombre = nombre,
                        descripcion = desc,
                        fechaLimiteMillis = fechaFinal,
                        prioridad = prioridadSelected
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("CREAR TAREA", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}