package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.data.model.RutinaDia
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymConfigContent(
    diaActual: String,
    onDiaSelect: (String) -> Unit,
    onSaveRutina: (String, List<EjercicioGym>) -> Unit,
    rutinasExistentes: List<RutinaDia>
) {
    val dias = listOf("L", "M", "X", "J", "V", "S", "D")

    // Nombre de la rutina — se reinicia al cambiar de día
    var nombreRutina by remember(diaActual) {
        val existente = rutinasExistentes.find { it.dia == diaActual }
        mutableStateOf(existente?.nombreRutina ?: "")
    }

    // Lista de ejercicios — se carga desde la BD al cambiar de día
    val ejerciciosTemp = remember(diaActual) { mutableStateListOf<EjercicioGym>() }
    var ejerciciosCargados by remember(diaActual) { mutableStateOf(false) }

    // Cargamos los ejercicios existentes una sola vez por día
    LaunchedEffect(diaActual, rutinasExistentes) {
        if (!ejerciciosCargados && rutinasExistentes.isNotEmpty()) {
            val existente = rutinasExistentes.find { it.dia == diaActual }
            ejerciciosTemp.clear()
            existente?.ejercicios?.let { ejerciciosTemp.addAll(it) }
            ejerciciosCargados = true
        }
    }

    // --- Campos del formulario de ejercicio ---
    var nombreEjerc by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var descanso by remember { mutableStateOf("90") }
    var esCardio by remember { mutableStateOf(false) }

    // Índice del ejercicio que se está editando (null = modo creación)
    var editandoIdx by remember { mutableStateOf<Int?>(null) }

    // Errores
    var nombreRutinaError by remember { mutableStateOf(false) }
    var nombreEjercError by remember { mutableStateOf(false) }

    // Helper para limpiar el formulario
    fun limpiarFormulario() {
        nombreEjerc = ""; series = ""; reps = ""; descanso = "90"; esCardio = false
        editandoIdx = null; nombreEjercError = false
    }

    // Helper para poblar el formulario con un ejercicio existente
    fun cargarEjercicioEnFormulario(idx: Int) {
        val ej = ejerciciosTemp[idx]
        nombreEjerc = ej.nombre
        esCardio = ej.esCardio
        series = if (ej.esCardio) "" else ej.seriesObjetivo.toString()
        reps = if (ej.esCardio) ej.minutosCardio.toString() else ej.repsObjetivo
        descanso = ej.descansoSegundos.toString()
        editandoIdx = idx
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text(
            "CONFIGURAR RUTINA",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- SELECTOR DE DÍAS ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dias.forEach { d ->
                val seleccionado = diaActual == d
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (seleccionado) Color.White else Color.Transparent, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { onDiaSelect(d) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        d,
                        color = if (seleccionado) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- NOMBRE DE LA RUTINA ---
        CustomTextField(
            value = nombreRutina,
            onValueChange = { nombreRutina = it; nombreRutinaError = false },
            label = "Nombre de la Rutina (ej: Empuje)"
        )
        if (nombreRutinaError) {
            Text("El nombre de la rutina es obligatorio", color = Color.Red, fontSize = 11.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardBorderColor)

        // --- FORMULARIO DE EJERCICIO ---
        // Título dinámico según si estamos editando o creando
        Text(
            text = if (editandoIdx != null) "✏️ EDITANDO EJERCICIO" else "AÑADIR EJERCICIO",
            color = if (editandoIdx != null) Color(0xFFFFD700) else SecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Switch Cardio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("¿Es cardio?", color = Color.White, fontSize = 14.sp)
            Switch(
                checked = esCardio,
                onCheckedChange = { esCardio = it; reps = "" },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Green)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = nombreEjerc,
            onValueChange = { nombreEjerc = it; nombreEjercError = false },
            label = if (esCardio) "Ej: Caminadora" else "Ej: Press Banca"
        )
        if (nombreEjercError) {
            Text("El nombre del ejercicio es obligatorio", color = Color.Red, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (esCardio) {
                // CARDIO: solo campo de minutos
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { if (it.all { c -> c.isDigit() }) reps = it },
                        label = { Text("Minutos", color = SecondaryText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = campoNumericoColors()
                    )
                }
            } else {
                // FUERZA: series, reps, descanso
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = series,
                        onValueChange = { if (it.all { c -> c.isDigit() }) series = it },
                        label = { Text("Series", color = SecondaryText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = campoNumericoColors()
                    )
                }
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { if (it.all { c -> c.isDigit() }) reps = it },
                        label = { Text("Reps", color = SecondaryText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = campoNumericoColors()
                    )
                }
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = descanso,
                        onValueChange = { if (it.all { c -> c.isDigit() }) descanso = it },
                        label = { Text("Desc(s)", color = SecondaryText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = campoNumericoColors()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón añadir / actualizar
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    nombreEjercError = nombreEjerc.isEmpty()
                    if (!nombreEjercError) {
                        val nuevoEjercicio = EjercicioGym(
                            nombre = nombreEjerc,
                            esCardio = esCardio,
                            // FIX: minutosCardio se guarda correctamente para cardio
                            minutosCardio = if (esCardio) reps.toIntOrNull() ?: 0 else 0,
                            seriesObjetivo = if (esCardio) 1 else series.toIntOrNull() ?: 3,
                            repsObjetivo = if (esCardio) "" else reps,
                            descansoSegundos = descanso.toIntOrNull() ?: 90
                        )

                        if (editandoIdx != null) {
                            // MODO EDICIÓN: reemplaza en la posición correcta
                            ejerciciosTemp[editandoIdx!!] = nuevoEjercicio
                        } else {
                            // MODO CREACIÓN: añade al final
                            ejerciciosTemp.add(nuevoEjercicio)
                        }
                        limpiarFormulario()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (editandoIdx != null) Color(0xFFFFD700).copy(0.15f) else Color.White.copy(0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (editandoIdx != null) "ACTUALIZAR" else "AÑADIR",
                    color = if (editandoIdx != null) Color(0xFFFFD700) else Color.White
                )
            }

            // Botón cancelar edición (solo visible en modo edición)
            if (editandoIdx != null) {
                OutlinedButton(
                    onClick = { limpiarFormulario() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("CANCELAR")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardBorderColor)

        // --- LISTA DE EJERCICIOS AÑADIDOS ---
        if (ejerciciosTemp.isNotEmpty()) {
            Text(
                "EJERCICIOS (${ejerciciosTemp.size})",
                color = SecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Altura máxima para no empujar el botón guardar fuera de pantalla
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(ejerciciosTemp) { idx, ej ->
                    val esElQueEdita = editandoIdx == idx
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (esElQueEdita) Color(0xFFFFD700).copy(0.05f) else Color.White.copy(0.03f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (esElQueEdita) Color(0xFFFFD700).copy(0.4f) else CardBorderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ej.nombre.uppercase(),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (ej.esCardio) "${ej.minutosCardio} min cardio"
                                else "${ej.seriesObjetivo}x${ej.repsObjetivo} · ${ej.descansoSegundos}s descanso",
                                color = SecondaryText,
                                fontSize = 11.sp
                            )
                        }

                        // Botón editar
                        IconButton(
                            onClick = { cargarEjercicioEnFormulario(idx) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Botón eliminar
                        IconButton(
                            onClick = { ejerciciosTemp.removeAt(idx) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red.copy(0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- BOTÓN GUARDAR TODA LA RUTINA ---
        Button(
            onClick = {
                nombreRutinaError = nombreRutina.isBlank()
                if (!nombreRutinaError) {
                    onSaveRutina(nombreRutina, ejerciciosTemp.toList())
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GUARDAR RUTINA", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun campoNumericoColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF4CAF50),
    unfocusedBorderColor = Color(0xFF333333)
)