package com.example.zenithapp20.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.zenithapp20.data.model.Habito

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitoForm(
    habitoAEditar: Habito? = null, // Parámetro nuevo para soportar edición
    onSave: (Habito) -> Unit
) {
    // Inicializamos los estados con los valores del hábito si estamos editando
    var nombreHabito by remember { mutableStateOf(habitoAEditar?.nombre ?: "") }
    var meta by remember { mutableStateOf(habitoAEditar?.meta ?: "") }
    var categoriaSeleccionada by remember { mutableStateOf(habitoAEditar?.categoria ?: "Salud") }
    var nombreError by remember { mutableStateOf(false) }

    val categoriasDisponibles = listOf("Salud", "Mente", "Productividad", "Social", "Hogar")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = if (habitoAEditar == null) "CREAR NUEVO HÁBITO" else "EDITAR HÁBITO",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo Nombre
        CustomTextField(
            value = nombreHabito,
            onValueChange = {
                nombreHabito = it
                nombreError = false
            },
            label = "Nombre del Hábito (ej: Meditar)"
        )
        if (nombreError) {
            Text("El nombre es obligatorio", color = Color.Red, fontSize = 11.sp)
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Campo Meta
        CustomTextField(
            value = meta,
            onValueChange = { meta = it },
            label = "Meta Diaria (ej: 10 min)"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("CATEGORÍA DE DISCIPLINA", color = Color.White.copy(0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriasDisponibles.forEach { cat ->
                val isSelected = categoriaSeleccionada == cat
                Surface(
                    modifier = Modifier.clickable { categoriaSeleccionada = cat },
                    color = if (isSelected) Color(0xFF00C853).copy(0.15f) else Color.White.copy(0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF00C853) else Color.White.copy(0.1f)
                    )
                ) {
                    Text(
                        text = cat.uppercase(),
                        color = if (isSelected) Color(0xFF00C853) else Color.White.copy(0.6f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Guardar / Actualizar
        Button(
            onClick = {
                if (nombreHabito.isNotEmpty()) {
                    nombreError = false
                    // LÓGICA DE PERSISTENCIA:
                    val habitoFinal = if (habitoAEditar != null) {
                        // Mantenemos ID y Checks, solo cambiamos datos del form
                        habitoAEditar.copy(
                            nombre = nombreHabito,
                            meta = meta,
                            categoria = categoriaSeleccionada
                        )
                    } else {
                        nombreError = false
                        // Es un hábito nuevo
                        Habito(
                            nombre = nombreHabito,
                            meta = meta,
                            categoria = categoriaSeleccionada,
                            checks = emptyList(),
                            icono = "🔥"
                        )
                    }

                    onSave(habitoFinal)

                    // Solo limpiamos si es creación nueva
                    if (habitoAEditar == null) {
                        nombreHabito = ""; meta = ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (habitoAEditar == null) "GUARDAR Y FIRMAR" else "ACTUALIZAR HÁBITO",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}