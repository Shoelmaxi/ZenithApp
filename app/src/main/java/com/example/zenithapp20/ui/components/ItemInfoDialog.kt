package com.example.zenithapp20.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText

/**
 * Diálogo reutilizable para mostrar la info/descripción de un ítem
 * (hábito, evento de agenda o tarea).
 *
 * @param titulo    Nombre del ítem — se muestra como título del diálogo
 * @param descripcion  Texto libre que el usuario escribió al crear el ítem
 * @param detalles  Lista de pares (etiqueta, valor) con metadata del ítem
 * @param onDismiss Callback al cerrar
 */
@Composable
fun ItemInfoDialog(
    titulo: String,
    descripcion: String = "",
    detalles: List<Pair<String, String>> = emptyList(),
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MainCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = titulo,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Descripción principal
                if (descripcion.isNotBlank()) {
                    Text(
                        text = descripcion,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                } else {
                    Text(
                        text = "Sin descripción añadida.",
                        color = SecondaryText,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                // Separador solo si hay descripción Y detalles
                if (detalles.isNotEmpty()) {
                    HorizontalDivider(color = Color.White.copy(0.06f))
                }

                // Metadata (hora, fecha límite, meta, racha, etc.)
                detalles.forEach { (label, valor) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = label,
                            color = SecondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = valor,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CERRAR",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    )
}