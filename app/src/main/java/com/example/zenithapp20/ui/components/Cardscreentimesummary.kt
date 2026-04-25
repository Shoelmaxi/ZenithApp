package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import com.example.zenithapp20.utils.ScreenTimeManager

/**
 * Tarjeta compacta para RimuSummaryScreen.
 * Muestra: total de tiempo en apps de distracción + mensaje de impacto.
 * Si no hay permiso muestra un banner para activarlo.
 */
@Composable
fun CardScreenTimeSummary(onVerDetalle: () -> Unit) {
    val context      = LocalContext.current
    val tienePermiso = remember { ScreenTimeManager.tienePermiso(context) }

    if (!tienePermiso) {
        // Banner pequeño invitando al permiso
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVerDetalle() },
            color  = Color.White.copy(0.03f),
            shape  = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
        ) {
            Row(
                modifier          = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AUDITORÍA DE TIEMPO",
                        color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Activa el permiso para ver cuánto regalas",
                        color = Color.White.copy(0.6f), fontSize = 12.sp
                    )
                }
                Text("→", color = SecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val apps         = remember { ScreenTimeManager.getTiempoDistracciones(context) }
    val totalDistrMs = remember { apps.sumOf { it.tiempoMs } }

    val colorSemaforo = when {
        totalDistrMs >= 120 * 60_000L -> Color(0xFFFF4444)
        totalDistrMs >= 60 * 60_000L  -> Color(0xFFFFD700)
        totalDistrMs >= 30 * 60_000L  -> Color(0xFFFF9800)
        else                          -> Color(0xFF00C853)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() },
        color  = colorSemaforo.copy(0.05f),
        shape  = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorSemaforo.copy(0.25f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "TIEMPO REGALADO HOY",
                        color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (totalDistrMs < 60_000L) "< 1m"
                            else ScreenTimeManager.tiempoATexto(totalDistrMs),
                            color      = colorSemaforo,
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (apps.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "en ${apps.size} app${if (apps.size != 1) "s" else ""}",
                                color    = SecondaryText,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        when {
                            totalDistrMs < 30 * 60_000L  -> "✅"
                            totalDistrMs < 60 * 60_000L  -> "⚡"
                            totalDistrMs < 120 * 60_000L -> "⚠️"
                            else                         -> "🚨"
                        },
                        fontSize = 24.sp
                    )
                    Text("VER →", color = colorSemaforo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // App más usada
            apps.firstOrNull()?.let { top ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(1.dp)
                        .background(Color.White.copy(0.05f))
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mayor consumo", color = SecondaryText, fontSize = 11.sp)
                    Text(
                        "${top.nombre} · ${ScreenTimeManager.tiempoATexto(top.tiempoMs)}",
                        color      = colorSemaforo,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mensaje impacto
            if (totalDistrMs >= 30 * 60_000L) {
                Spacer(Modifier.height(10.dp))
                Text(
                    when {
                        totalDistrMs >= 120 * 60_000L ->
                            "Hoy regalaste ${ScreenTimeManager.tiempoATexto(totalDistrMs)}. Eso no vuelve."
                        totalDistrMs >= 60 * 60_000L ->
                            "1 hora+. ¿Valió la pena el intercambio?"
                        else ->
                            "${ScreenTimeManager.tiempoATexto(totalDistrMs)} de distracción. Podés bajar más."
                    },
                    color      = colorSemaforo.copy(0.7f),
                    fontSize   = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}