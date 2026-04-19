package com.example.zenithapp20.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText

private fun esHabitoGestionado(habito: Habito): Boolean {
    val nombre = habito.nombre.lowercase()
    return nombre.contains("entrenar") ||
            nombre.contains("entrenamiento") ||
            nombre.contains("gym") ||
            nombre.contains("ejercicio") ||
            nombre.contains("agua") ||
            nombre.contains("hidrat")
}

@Composable
fun HabitoQuickItem(
    habito: Habito,
    isCompletadoHoy: Boolean,
    onCheckClick: () -> Unit,
    onLongClick: () -> Unit,                // abre el formulario de edición
    onInfoClick: () -> Unit = {}            // muestra el diálogo de info
) {
    val bloqueado = esHabitoGestionado(habito)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MainCardBackground, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                when {
                    isCompletadoHoy -> Color.Green.copy(0.4f)
                    bloqueado       -> Color(0xFFFFD700).copy(0.2f)
                    else            -> CardBorderColor
                },
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ── ÁREA DE TEXTO ──────────────────────────────────────────────────
        // Tap  → muestra diálogo de info
        // Long → abre edición (solo si no está bloqueado)
        Row(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = { onInfoClick() },
                    onLongClick = if (!bloqueado) onLongClick else null
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
                Text(
                    habito.nombre,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Sub-texto: meta normal o hint de bloqueado
                Text(
                    text = when {
                        bloqueado && isCompletadoHoy  -> "✓ ${habito.meta}"
                        bloqueado && !isCompletadoHoy -> habito.meta
                        else                          -> habito.meta
                    },
                    color = if (bloqueado && !isCompletadoHoy)
                        Color(0xFFFFD700).copy(0.7f) else SecondaryText,
                    fontSize = 11.sp
                )

                // Hint sutil: indica al usuario que puede tocar para ver info
                if (habito.descripcion.isNotBlank()) {
                    Text(
                        text = "Toca para ver instrucciones",
                        color = Color(0xFF4CAF50).copy(0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── ÁREA DEL CHECK ─────────────────────────────────────────────────
        if (bloqueado) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isCompletadoHoy) Color.Green else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isCompletadoHoy) Color.Green else Color(0xFFFFD700).copy(0.4f),
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
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Completar automáticamente",
                        tint = Color(0xFFFFD700).copy(0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
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
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .border(1.dp, borderColor, CircleShape)
                    .background(checkColor, CircleShape)
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
}