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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitoForm(
    habitoAEditar: Habito? = null,
    onSave: (Habito) -> Unit
) {
    var nombreHabito   by remember { mutableStateOf(habitoAEditar?.nombre      ?: "") }
    var descripcion    by remember { mutableStateOf(habitoAEditar?.descripcion  ?: "") }
    var meta           by remember { mutableStateOf(habitoAEditar?.meta         ?: "") }
    var categoriaSeleccionada by remember { mutableStateOf(habitoAEditar?.categoria ?: "Salud") }
    var nombreError    by remember { mutableStateOf(false) }

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

        // ── NOMBRE ─────────────────────────────────────────────────────────
        CustomTextField(
            value = nombreHabito,
            onValueChange = { nombreHabito = it; nombreError = false },
            label = "Nombre del Hábito (ej: Meditar)"
        )
        if (nombreError) {
            Text("El nombre es obligatorio", color = Color.Red, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── DESCRIPCIÓN ────────────────────────────────────────────────────
        // Campo clave: aquí el usuario escribe QUÉ tiene que hacer exactamente.
        // Al tocar la card del hábito en la pantalla principal, verá este texto.
        OutlinedTextField(
            value = descripcion,
            onValueChange = { if (it.length <= 300) descripcion = it },
            label = { Text("Descripción / ¿Qué debo hacer?", color = SecondaryText) },
            placeholder = {
                Text(
                    "Ej: Meditar 10 min con la app Headspace, posición de loto, sin teléfono.",
                    color = SecondaryText.copy(0.4f),
                    fontSize = 12.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor    = Color.White,
                unfocusedTextColor  = Color.White,
                focusedBorderColor  = Color(0xFF4CAF50),
                unfocusedBorderColor = CardBorderColor,
                focusedLabelColor   = Color(0xFF4CAF50),
                unfocusedLabelColor = SecondaryText,
                cursorColor         = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (descripcion.isNotBlank()) {
                    Text(
                        "${descripcion.length}/300",
                        color = SecondaryText,
                        fontSize = 10.sp
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── META ───────────────────────────────────────────────────────────
        CustomTextField(
            value = meta,
            onValueChange = { meta = it },
            label = "Meta Diaria (ej: 10 min)"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── CATEGORÍA ──────────────────────────────────────────────────────
        Text(
            "CATEGORÍA DE DISCIPLINA",
            color = Color.White.copy(0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
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

        // ── GUARDAR ────────────────────────────────────────────────────────
        Button(
            onClick = {
                if (nombreHabito.isNotEmpty()) {
                    nombreError = false
                    val habitoFinal = if (habitoAEditar != null) {
                        habitoAEditar.copy(
                            nombre      = nombreHabito,
                            descripcion = descripcion,
                            meta        = meta,
                            categoria   = categoriaSeleccionada
                        )
                    } else {
                        Habito(
                            nombre      = nombreHabito,
                            descripcion = descripcion,
                            meta        = meta,
                            categoria   = categoriaSeleccionada,
                            checks      = emptyList(),
                            icono       = "🔥"
                        )
                    }
                    onSave(habitoFinal)
                    if (habitoAEditar == null) {
                        nombreHabito = ""; descripcion = ""; meta = ""
                    }
                } else {
                    nombreError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
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