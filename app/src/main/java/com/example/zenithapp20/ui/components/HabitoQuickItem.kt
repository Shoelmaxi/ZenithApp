package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText

@Composable
fun HabitoQuickItem(
    habito: Habito,
    isCompletadoHoy: Boolean,
    onCheckClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MainCardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onCheckClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono sutil o inicial
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isCompletadoHoy) Color.Green.copy(0.1f) else Color.White.copy(0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(habito.icono, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = habito.nombre,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = habito.meta,
                    color = SecondaryText,
                    fontSize = 11.sp
                )
            }
        }

        // Botón de Check tipo "Toggle"
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    width = 1.dp,
                    color = if (isCompletadoHoy) Color.Green else Color.Gray.copy(0.5f),
                    shape = CircleShape
                )
                .background(
                    if (isCompletadoHoy) Color.Green else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompletadoHoy) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}