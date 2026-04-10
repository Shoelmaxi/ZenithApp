package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.TipoTransaccion
import com.example.zenithapp20.data.model.Transaccion
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText


@Composable
fun FinanzasItem(transaccion: Transaccion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MainCardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(transaccion.nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(transaccion.categoria.uppercase(), color = SecondaryText, fontSize = 11.sp)
        }

        Text(
            text = if (transaccion.tipo == TipoTransaccion.INGRESO)
                "+${formatCLP(transaccion.monto)}"
            else
                "-${formatCLP(transaccion.monto)}",
            color = if (transaccion.tipo == TipoTransaccion.INGRESO) Color.Green else Color.Red,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}