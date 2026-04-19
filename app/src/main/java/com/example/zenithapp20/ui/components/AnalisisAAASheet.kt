package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.zenithapp20.data.model.AnalisisHabito
import com.example.zenithapp20.data.model.FrictionFactor
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalisisAAASheet(
    habito: Habito,
    onSave: (AnalisisHabito) -> Unit,
    onSkip: () -> Unit
) {
    var focusLevel by remember { mutableFloatStateOf(7f) }
    var frictionSelected by remember { mutableStateOf<FrictionFactor?>(null) }
    var adjustmentNote by remember { mutableStateOf("") }
    var noteError by remember { mutableStateOf(false) }

    val requiresNote = focusLevel.toInt() < 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ANÁLISIS AAA", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text(habito.nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            TextButton(onClick = onSkip) {
                Text("OMITIR", color = SecondaryText, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NIVEL DE ENFOQUE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NIVEL DE ENFOQUE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "${focusLevel.toInt()}/10",
                color = when {
                    focusLevel >= 8 -> Color(0xFF00C853)
                    focusLevel >= 5 -> Color(0xFFFFD700)
                    else -> Color(0xFFFF4444)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Slider(
            value = focusLevel,
            onValueChange = { focusLevel = it },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color.White.copy(0.1f)
            )
        )

        if (requiresNote) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF4444).copy(0.08f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4444).copy(0.3f))
            ) {
                Text(
                    "⚠️ Focus bajo 7 — análisis de ajuste obligatorio",
                    color = Color(0xFFFF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FACTOR DE FRICCIÓN
        Text("FACTOR DE FRICCIÓN", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrictionFactor.entries.forEach { factor ->
                val isSelected = frictionSelected == factor
                Surface(
                    modifier = Modifier.clickable { frictionSelected = factor },
                    color = if (isSelected) Color(0xFFFFD700).copy(0.12f) else Color.White.copy(0.04f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFFFFD700).copy(0.6f) else CardBorderColor
                    )
                ) {
                    Text(
                        "${factor.emoji} ${factor.label}",
                        color = if (isSelected) Color(0xFFFFD700) else SecondaryText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NOTA DE AJUSTE
        OutlinedTextField(
            value = adjustmentNote,
            onValueChange = { adjustmentNote = it; noteError = false },
            label = {
                Text(
                    if (requiresNote) "Nota de ajuste (obligatorio)" else "Nota de ajuste (opcional)",
                    color = SecondaryText
                )
            },
            placeholder = {
                Text(
                    "¿Qué cambiarías para la próxima vez?",
                    color = SecondaryText.copy(0.4f), fontSize = 12.sp
                )
            },
            isError = noteError,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (noteError) {
            Text("El análisis de ajuste es obligatorio con focus < 7", color = Color.Red, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (requiresNote && adjustmentNote.isBlank()) {
                    noteError = true
                    return@Button
                }
                onSave(
                    AnalisisHabito(
                        habitoId = habito.id,
                        habitoNombre = habito.nombre,
                        focusLevel = focusLevel.toInt(),
                        frictionFactor = frictionSelected?.name ?: FrictionFactor.ENTORNO.name,
                        adjustmentNote = adjustmentNote.trim()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("GUARDAR ANÁLISIS", color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}