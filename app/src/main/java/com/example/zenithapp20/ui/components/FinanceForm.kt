package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.TipoTransaccion
import com.example.zenithapp20.data.model.Transaccion
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText


@Composable
fun FinanceForm(onSave: (Transaccion) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoTransaccion.EGRESO) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "NUEVO MOVIMIENTO",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Selector de Tipo (Ingreso / Egreso)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TipoButton(
                label = "EGRESO",
                isSelected = tipo == TipoTransaccion.EGRESO,
                selectedColor = Color.Red,
                modifier = Modifier.weight(1f)
            ) { tipo = TipoTransaccion.EGRESO }

            TipoButton(
                label = "INGRESO",
                isSelected = tipo == TipoTransaccion.INGRESO,
                selectedColor = Color.Green,
                modifier = Modifier.weight(1f)
            ) { tipo = TipoTransaccion.INGRESO }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPO MONTO (CORREGIDO CON OUTLINEDTEXTFIELD ESTÁNDAR) ---
        OutlinedTextField(
            value = monto,
            onValueChange = { if (it.all { char -> char.isDigit() }) monto = it },
            label = { Text("Monto ($)", color = SecondaryText) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White.copy(0.5f),
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Para los campos de texto normales, seguimos usando tu CustomTextField
        CustomTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = "Descripción (ej: Supermercado)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = "Categoría (ej: Comida, Ocio, Cuentas)"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (nombre.isNotBlank() && monto.isNotBlank()) {
                    onSave(
                        Transaccion(
                            nombre = nombre,
                            monto = monto.toIntOrNull() ?: 0,
                            categoria = categoria.ifBlank { "General" },
                            tipo = tipo
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("GUARDAR MOVIMIENTO", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun TipoButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) selectedColor else SecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}