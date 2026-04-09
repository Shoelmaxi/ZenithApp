package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.EjercicioGym
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymConfigContent(
    diaActual: String,
    onDiaSelect: (String) -> Unit,
    onSaveRutina: (String, List<EjercicioGym>) -> Unit
) {
    val dias = listOf("L", "M", "X", "J", "V", "S", "D")
    var nombreRutina by remember { mutableStateOf("") }
    val ejerciciosTemp = remember { mutableStateListOf<EjercicioGym>() }

    // Estados para el nuevo ejercicio
    var nombreEjerc by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var descanso by remember { mutableStateOf("60") } // Nuevo: Descanso
    var esCardio by remember { mutableStateOf(false) } // Nuevo: Switch Cardio

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
                        CustomTextField(value = reps, onValueChange = { reps = it }, label = "Minutos totales")
                    }
                } else {
                    Box(Modifier.weight(1f)) { CustomTextField(value = series, onValueChange = { series = it }, label = "Series") }
                    Box(Modifier.weight(1f)) { CustomTextField(value = reps, onValueChange = { reps = it }, label = "Reps") }
                    Box(Modifier.weight(1f)) { CustomTextField(value = descanso, onValueChange = { descanso = it }, label = "Descanso (s)") }
                }
            }
        }

        Button(
            onClick = {
                if (nombreEjerc.isNotEmpty()) {
                    ejerciciosTemp.add(
                        EjercicioGym(
                            nombre = nombreEjerc,
                            seriesObjetivo = if(esCardio) 1 else (series.toIntOrNull() ?: 3),
                            repsObjetivo = reps,
                            esCardio = esCardio,
                            descansoSegundos = descanso.toIntOrNull() ?: 60
                        )
                    )
                    // Reset campos
                    nombreEjerc = ""; series = ""; reps = ""; descanso = "60"; esCardio = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("AÑADIR A LA LISTA", color = Color.White)
        }

        // Lista previa de lo que se va añadiendo (Opcional, ayuda a ver qué llevas)
        if (ejerciciosTemp.isNotEmpty()) {
            Text("Agregados: ${ejerciciosTemp.size}", color = SecondaryText, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
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