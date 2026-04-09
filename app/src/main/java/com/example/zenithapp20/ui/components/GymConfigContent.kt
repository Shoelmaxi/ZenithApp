package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
    var nombreRutina by remember(diaActual) {
        val existente = rutinasExistentes.find { it.dia == diaActual }
        mutableStateOf(existente?.nombreRutina ?: "")
    }
    val ejerciciosTemp = remember { mutableStateListOf<EjercicioGym>() }

    // Estados para el nuevo ejercicio
    var nombreEjerc by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var descanso by remember { mutableStateOf("90") } // Nuevo: Descanso
    var esCardio by remember { mutableStateOf(false) } // Nuevo: Switch Cardio
    var nombreRutinaError by remember { mutableStateOf(false) }
    var nombreEjercError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()) {
        Text("CONFIGURAR RUTINA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de días
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dias.forEach { d ->
                val seleccionado = diaActual == d
                Box(
                    modifier = Modifier.size(40.dp)
                        .background(if (seleccionado) Color.White else Color.Transparent, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { onDiaSelect(d) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(d, color = if (seleccionado) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        CustomTextField(value = nombreRutina, onValueChange = { nombreRutina = it }, label = "Nombre de la Rutina (ej: Empuje)")

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardBorderColor)

        // Switch para Cardio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("¿Es ejercicio de Cardio?", color = Color.White, fontSize = 14.sp)
            Switch(
                checked = esCardio,
                onCheckedChange = { esCardio = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Green)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Formulario dinámico
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomTextField(value = nombreEjerc, onValueChange = { nombreEjerc = it }, label = if(esCardio) "Ej: Caminadora" else "Ej: Press Banca")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (esCardio) {
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { if (it.all { c -> c.isDigit() }) reps = it },
                            label = { Text("Minutos", color = SecondaryText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF333333)
                            )
                        )
                    }
                } else {
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = series,
                            onValueChange = { if (it.all { c -> c.isDigit() }) series = it },
                            label = { Text("Series", color = SecondaryText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF333333)
                            )
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { if (it.all { c -> c.isDigit() }) reps = it },
                            label = { Text("Reps", color = SecondaryText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF333333)
                            )
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = descanso,
                            onValueChange = { if (it.all { c -> c.isDigit() }) descanso = it },
                            label = { Text("Desc(s)", color = SecondaryText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF333333)
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                nombreEjercError = nombreEjerc.isEmpty()
                if (!nombreEjercError) {
                    ejerciciosTemp.add(
                        EjercicioGym(
                            nombre = nombreEjerc,
                            seriesObjetivo = if(esCardio) 1 else (series.toIntOrNull() ?: 3),
                            repsObjetivo = reps,
                            esCardio = esCardio,
                            descansoSegundos = descanso.toIntOrNull() ?: 90
                        )
                    )
                    // Reset campos
                    nombreEjerc = ""; series = ""; reps = ""; descanso = "90"; esCardio = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("AÑADIR A LA LISTA", color = Color.White)
        }

        // Lista previa de lo que se va añadiendo (Opcional, ayuda a ver qué llevas)
        if (nombreEjercError) {
            Text("El nombre del ejercicio es obligatorio", color = Color.Red, fontSize = 11.sp)
        }


        // Botón Final Guardar
        Button(
            onClick = { onSaveRutina(nombreRutina, ejerciciosTemp.toList()) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GUARDAR TODO", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}