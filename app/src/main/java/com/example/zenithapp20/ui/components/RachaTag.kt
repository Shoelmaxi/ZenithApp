package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Archivo: ui/components/RachaTag.kt
@Composable
fun RachaTag(semanas: Int) {
    if (semanas > 0) {
        Row(
            modifier = Modifier
                .background(Color(0xFFFFD700).copy(0.1f), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFFFD700).copy(0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⭐", fontSize = 10.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$semanas SEMANA${if (semanas > 1) "S" else ""} PERFECTA",
                color = Color(0xFFFFD700),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}