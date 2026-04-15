package com.example.zenithapp20.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.CategoriaLibro
import com.example.zenithapp20.data.model.Libro
import com.example.zenithapp20.data.model.SesionLectura
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText

private val SACRIFICIOS = listOf(
    "RRSS", "YouTube", "Series", "Videojuegos", "Trabajo vacío", "Nada específico"
)

private val PLACEHOLDERS_LECCION = listOf(
    "¿Qué debilidad detectaste en este capítulo?",
    "¿Qué regla implícita acaba de romperse?",
    "¿Qué haría este autor con tu problema actual?",
    "¿Qué patrón se repite que no habías notado antes?",
    "¿Qué creencia tuya desafía este pasaje?",
    "¿Qué información te daría ventaja si la recordaras siempre?",
    "¿Qué error concreto evitarías aplicando esto?"
)

private val PLACEHOLDERS_ESTRATEGIA = listOf(
    "¿Cómo aplicarías esto bajo presión?",
    "¿En qué situación de tu vida esto cambia el resultado?",
    "¿Qué harías diferente mañana con este conocimiento?",
    "¿Cómo te da ventaja sobre alguien que no leyó esto?",
    "¿Qué decisión pendiente resuelve este principio?",
    "¿Cuál es el primer paso concreto para implementar esto?",
    "Si lo olvidaras en una semana, ¿cuál sería el costo?"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SesionLecturaForm(
    libro: Libro,
    onSave: (SesionLectura) -> Unit
) {
    // Placeholders aleatorios fijados al primer render
    val placeholderLeccion = remember { PLACEHOLDERS_LECCION.random() }
    val placeholderEstrategia = remember { PLACEHOLDERS_ESTRATEGIA.random() }

    var paginaFinInput by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(libro.categoria) }
    var sacrificioSeleccionado by remember { mutableStateOf("") }
    var esMinimo by remember { mutableStateOf(false) }
    var leccion by remember { mutableStateOf("") }
    var estrategia by remember { mutableStateOf("") }
    var paginaError by remember { mutableStateOf<String?>(null) }

    // Delta calculado en tiempo real
    val delta = remember(paginaFinInput) {
        val pf = paginaFinInput.toIntOrNull() ?: 0
        (pf - libro.paginaActual).coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding()
    ) {

        // ── HEADER ──────────────────────────────────────────────────────────
        Text("CHECKPOINT", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(libro.titulo, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 2)
        Text("Continúas desde la página ${libro.paginaActual}", color = SecondaryText, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // ── PÁGINA ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value = paginaFinInput,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    paginaFinInput = it
                    paginaError = null
                }
            },
            label = { Text("Página en la que quedaste", color = SecondaryText) },
            isError = paginaError != null,
            supportingText = {
                paginaError?.let { Text(it, color = Color.Red) }
            },
            trailingIcon = {
                if (delta > 0) {
                    Text(
                        "+$delta págs",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            },
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

        // ── CATEGORÍA ───────────────────────────────────────────────────────
        Text("CONTEXTO DE LA SESIÓN", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    color = if (isSelected) Color(0xFF4CAF50).copy(0.15f) else Color.White.copy(0.03f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF4CAF50) else CardBorderColor)
                ) {
                    Text(
                        text = "${cat.emoji} ${cat.label}",
                        color = if (isSelected) Color(0xFF4CAF50) else SecondaryText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── SACRIFICIO ──────────────────────────────────────────────────────
        Text("REEMPLAZASTE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SACRIFICIOS.forEach { s ->
                val isSelected = sacrificioSeleccionado == s
                Surface(
                    modifier = Modifier.clickable {
                        sacrificioSeleccionado = if (sacrificioSeleccionado == s) "" else s
                    },
                    color = if (isSelected) Color(0xFFFFD700).copy(0.1f) else Color.White.copy(0.03f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700).copy(0.6f) else CardBorderColor)
                ) {
                    Text(
                        text = s,
                        color = if (isSelected) Color(0xFFFFD700) else SecondaryText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── META MÍNIMA ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.03f), RoundedCornerShape(12.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("¿Fue meta mínima?", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Solo leíste 5–10 minutos", color = SecondaryText, fontSize = 11.sp)
            }
            Switch(
                checked = esMinimo,
                onCheckedChange = { esMinimo = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── LECCIÓN ──────────────────────────────────────────────────────────
        Text("LECCIÓN CLAVE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("Opcional — máx. 280 caracteres", color = SecondaryText.copy(0.5f), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = leccion,
            onValueChange = { if (it.length <= 280) leccion = it },
            placeholder = { Text(placeholderLeccion, color = SecondaryText.copy(0.4f), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        if (leccion.isNotBlank()) {
            Text(
                "${leccion.length}/280",
                color = SecondaryText,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── APLICACIÓN ───────────────────────────────────────────────────────
        Text("APLICACIÓN ESTRATÉGICA", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("Opcional — ¿cómo lo usas para ganar?", color = SecondaryText.copy(0.5f), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = estrategia,
            onValueChange = { if (it.length <= 280) estrategia = it },
            placeholder = { Text(placeholderEstrategia, color = SecondaryText.copy(0.4f), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        if (estrategia.isNotBlank()) {
            Text(
                "${estrategia.length}/280",
                color = SecondaryText,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── GUARDAR ───────────────────────────────────────────────────────────
        Button(
            onClick = {
                val pf = paginaFinInput.toIntOrNull()
                when {
                    pf == null || pf <= 0 -> {
                        paginaError = "Ingresa un número de página válido"
                    }
                    pf <= libro.paginaActual -> {
                        paginaError = "Debes avanzar al menos 1 página (estás en la ${libro.paginaActual})"
                    }
                    pf > libro.paginasTotales -> {
                        paginaError = "El libro tiene ${libro.paginasTotales} páginas"
                    }
                    else -> {
                        onSave(
                            SesionLectura(
                                libroId = libro.id,
                                paginaInicio = libro.paginaActual,
                                paginaFin = pf,
                                leccionClave = leccion.trim(),
                                aplicacionEstrategica = estrategia.trim(),
                                categoria = categoriaSeleccionada,
                                sacrificio = sacrificioSeleccionado,
                                esMinimoCumplido = esMinimo
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "REGISTRAR CHECKPOINT",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}