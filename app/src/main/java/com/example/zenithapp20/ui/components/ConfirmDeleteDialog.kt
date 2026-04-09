package com.example.zenithapp20.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.zenithapp20.ui.theme.MainCardBackground

@Composable
fun ConfirmDeleteDialog(
    mensaje: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MainCardBackground,
        title = {
            Text(
                "¿Eliminar?",
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(mensaje, color = Color.Gray)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}