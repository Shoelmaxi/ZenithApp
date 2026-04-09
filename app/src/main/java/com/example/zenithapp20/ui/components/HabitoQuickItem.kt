package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
    onCheckClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MainCardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // área de texto — solo long press para editar
        Row(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isCompletadoHoy) Color.Green.copy(0.1f) else Color.White.copy(0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(habito.icono, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(habito.nombre, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(habito.meta, color = SecondaryText, fontSize = 11.sp)
            }
        }

        // área del check — solo toggle
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    1.dp,
                    if (isCompletadoHoy) Color.Green else Color.Gray.copy(0.5f),
                    CircleShape
                )
                .background(
                    if (isCompletadoHoy) Color.Green else Color.Transparent,
                    CircleShape
                )
                .clickable { onCheckClick() },
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