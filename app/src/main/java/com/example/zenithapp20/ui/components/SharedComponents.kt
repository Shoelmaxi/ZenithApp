package com.example.zenithapp20.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color(0xFF333333),
            unfocusedLabelColor = Color.Gray,
            focusedLabelColor = Color(0xFF4CAF50)
        )
    )
}

@Composable
fun DayChip(dia: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(38.dp).clickable { onClick() },
        shape = CircleShape,
        color = if (seleccionado) Color(0xFF4CAF50) else Color(0xFF2A2A2A),
        border = if (!seleccionado) BorderStroke(1.dp, Color(0xFF333333)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(dia, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatCLP(monto: Int): String {
    return "$" + "%,d".format(monto).replace(",", ".")
}