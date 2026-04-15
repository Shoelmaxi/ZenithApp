package com.example.zenithapp20.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.example.zenithapp20.data.model.CategoriaLibro
import com.example.zenithapp20.data.model.Libro
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibroForm(
    libroAEditar: Libro? = null,
    onSave: (Libro) -> Unit
) {
    var titulo by remember { mutableStateOf(libroAEditar?.titulo ?: "") }
    var autor by remember { mutableStateOf(libroAEditar?.autor ?: "") }
    var paginas by remember { mutableStateOf(if (libroAEditar != null) libroAEditar.paginasTotales.toString() else "") }
    var categoriaSeleccionada by remember { mutableStateOf(libroAEditar?.categoria ?: CategoriaLibro.OTRO) }

    var tituloError by remember { mutableStateOf(false) }
    var paginasError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = if (libroAEditar == null) "AÑADIR LIBRO" else "EDITAR LIBRO",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            value = titulo,
            onValueChange = { titulo = it; tituloError = false },
            label = "Título"
        )
        if (tituloError) Text("El título es obligatorio", color = Color.Red, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = autor,
            onValueChange = { autor = it },
            label = "Autor"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = paginas,
            onValueChange = { if (it.all { c -> c.isDigit() }) { paginas = it; paginasError = false } },
            label = { Text("Número de páginas", color = SecondaryText) },
            isError = paginasError,
            supportingText = { if (paginasError) Text("Ingresa el total de páginas", color = Color.Red) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "CATEGORÍA",
            color = SecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoriaLibro.entries.forEach { cat ->
                val isSelected = categoriaSeleccionada == cat
                Surface(
                    modifier = Modifier.clickable { categoriaSeleccionada = cat },
                    color = if (isSelected) Color(0xFF4CAF50).copy(0.15f) else Color.White.copy(0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF4CAF50) else Color.White.copy(0.1f)
                    )
                ) {
                    Text(
                        text = "${cat.emoji} ${cat.label.uppercase()}",
                        color = if (isSelected) Color(0xFF4CAF50) else SecondaryText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                tituloError = titulo.isBlank()
                paginasError = paginas.isBlank() || (paginas.toIntOrNull() ?: 0) <= 0

                if (!tituloError && !paginasError) {
                    onSave(
                        Libro(
                            id = libroAEditar?.id ?: 0,
                            titulo = titulo.trim(),
                            autor = autor.trim(),
                            paginasTotales = paginas.toInt(),
                            paginaActual = libroAEditar?.paginaActual ?: 0,
                            estado = libroAEditar?.estado ?: com.example.zenithapp20.data.model.EstadoLibro.PENDIENTE,
                            categoria = categoriaSeleccionada,
                            fechaInicio = libroAEditar?.fechaInicio,
                            fechaFin = libroAEditar?.fechaFin
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (libroAEditar == null) "GUARDAR LIBRO" else "ACTUALIZAR LIBRO",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}