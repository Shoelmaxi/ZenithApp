package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import com.example.zenithapp20.ui.viewmodel.AguaViewModel

@Composable
fun CardAgua(
    totalMl: Int,
    metaMl: Int = AguaViewModel.META_ML,
    mlPorVaso: Int = AguaViewModel.ML_POR_VASO,
    onAgregarVaso: () -> Unit,
    onQuitarVaso: () -> Unit
) {
    val vasos = totalMl / mlPorVaso
    val vasosTotal = metaMl / mlPorVaso
    val progreso = (totalMl.toFloat() / metaMl.toFloat()).coerceIn(0f, 1f)
    val metaAlcanzada = totalMl >= metaMl
    val colorAgua = if (metaAlcanzada) Color(0xFF00C853) else Color(0xFF2196F3)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MainCardBackground,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (metaAlcanzada) Color(0xFF00C853).copy(0.4f) else CardBorderColor
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💧", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "AGUA",
                            color = SecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (metaAlcanzada) "¡Meta alcanzada! 🎉"
                            else "${totalMl}ml / ${metaMl}ml",
                            color = if (metaAlcanzada) Color(0xFF00C853) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onQuitarVaso,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(0.05f), CircleShape)
                            .border(1.dp, CardBorderColor, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            null,
                            tint = SecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        "$vasos/$vasosTotal",
                        color = colorAgua,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = onAgregarVaso,
                        modifier = Modifier
                            .size(32.dp)
                            .background(colorAgua.copy(0.1f), CircleShape)
                            .border(1.dp, colorAgua.copy(0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = colorAgua,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BARRA DE PROGRESO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.White.copy(0.05f), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso)
                        .height(6.dp)
                        .background(colorAgua, RoundedCornerShape(3.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // VASITOS VISUALES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(vasosTotal) { i ->
                    val lleno = i < vasos
                    Text(
                        text = if (lleno) "🥤" else "○",
                        fontSize = if (lleno) 14.sp else 12.sp,
                        color = if (lleno) colorAgua else Color.White.copy(0.2f)
                    )
                }
            }
        }
    }
}