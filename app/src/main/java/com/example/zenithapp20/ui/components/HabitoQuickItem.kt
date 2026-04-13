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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer

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
        val scale by animateFloatAsState(
            targetValue = if (isCompletadoHoy) 1f else 0.9f,
            animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
            label = "check_scale"
        )
        val checkColor by animateColorAsState(
            targetValue = if (isCompletadoHoy) Color.Green else Color.Transparent,
            label = "check_color"
        )
        val borderColor by animateColorAsState(
            targetValue = if (isCompletadoHoy) Color.Green else Color.Gray.copy(0.5f),
            label = "check_border"
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)  // ← animación de escala
                .border(1.dp, borderColor, CircleShape)
                .background(checkColor, CircleShape)
                .clickable { onCheckClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompletadoHoy) {
                Icon(Icons.Default.Check, contentDescription = null,
                    tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }
    }
}