package com.example.zenithapp20.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithapp20.data.model.AnalisisHabito
import com.example.zenithapp20.data.model.FactorPositivo
import com.example.zenithapp20.data.model.FrictionFactor
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.RazonNoCompletado
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.SecondaryText

// Zona de focus
private enum class ZonaFocus { ALTO, MEDIO, BAJO }

private fun zonaFocus(level: Int) = when {
    level >= 8 -> ZonaFocus.ALTO
    level >= 5 -> ZonaFocus.MEDIO
    else       -> ZonaFocus.BAJO
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalisisAAASheet(
    habito: Habito,
    estaCompletadoHoy: Boolean = false,
    onSave: (AnalisisHabito) -> Unit,
    onSkip: () -> Unit
) {
    var completado        by remember { mutableStateOf<Boolean?>(if (estaCompletadoHoy) true else null) }
    var focusLevel        by remember { mutableFloatStateOf(7f) }
    var frictionSelected  by remember { mutableStateOf<FrictionFactor?>(null) }
    var positivoSelected  by remember { mutableStateOf<FactorPositivo?>(null) }
    var razonSelected     by remember { mutableStateOf<RazonNoCompletado?>(null) }
    var adjustmentNote    by remember { mutableStateOf("") }
    var completadoError   by remember { mutableStateOf(false) }
    var noteError         by remember { mutableStateOf(false) }

    // La nota es obligatoria solo cuando no completó o focus bajo
    val requiresNote = completado == false || (completado == true && focusLevel.toInt() < 5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding()
    ) {

        // ── CABECERA ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ANÁLISIS AAA",
                    color = Color(0xFF4CAF50),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    habito.nombre,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
            TextButton(onClick = onSkip) {
                Text("OMITIR", color = SecondaryText, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── ¿COMPLETASTE ESTE HÁBITO? ───────────────────────────────────────
        Text(
            "¿COMPLETASTE ESTE HÁBITO?",
            color = SecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        if (completadoError) {
            Text("Selecciona una opción para continuar", color = Color.Red, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SÍ
            Surface(
                modifier = Modifier.weight(1f).clickable {
                    completado = true
                    completadoError = false
                },
                color = if (completado == true) Color(0xFF00C853).copy(0.12f) else Color.White.copy(0.04f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, if (completado == true) Color(0xFF00C853) else CardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✅", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "SÍ LO HICE",
                        color = if (completado == true) Color(0xFF00C853) else SecondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // NO
            Surface(
                modifier = Modifier.weight(1f).clickable {
                    completado = false
                    completadoError = false
                },
                color = if (completado == false) Color(0xFFFF4444).copy(0.08f) else Color.White.copy(0.04f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, if (completado == false) Color(0xFFFF4444).copy(0.7f) else CardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("❌", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "NO LO HICE",
                        color = if (completado == false) Color(0xFFFF4444) else SecondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // ── CONTENIDO CONDICIONAL ───────────────────────────────────────────
        completado?.let { hecho ->
            Spacer(modifier = Modifier.height(24.dp))

            if (hecho) {
                // ─────────────────────────────
                //  RAMA: SÍ LO HICE
                // ─────────────────────────────

                // Nivel de enfoque
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NIVEL DE ENFOQUE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    val zona = zonaFocus(focusLevel.toInt())
                    Text(
                        text = "${focusLevel.toInt()}/10  ${when (zona) {
                            ZonaFocus.ALTO  -> "🔥 En zona"
                            ZonaFocus.MEDIO -> "⚡ Sólido"
                            ZonaFocus.BAJO  -> "⚠️ Con fricción"
                        }}",
                        color = when (zona) {
                            ZonaFocus.ALTO  -> Color(0xFF00C853)
                            ZonaFocus.MEDIO -> Color(0xFFFFD700)
                            ZonaFocus.BAJO  -> Color(0xFFFF4444)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Slider(
                    value = focusLevel,
                    onValueChange = {
                        focusLevel = it
                        // Limpiar selecciones al cambiar de zona
                        frictionSelected = null
                        positivoSelected = null
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = when (zonaFocus(focusLevel.toInt())) {
                            ZonaFocus.ALTO  -> Color(0xFF00C853)
                            ZonaFocus.MEDIO -> Color(0xFFFFD700)
                            ZonaFocus.BAJO  -> Color(0xFFFF4444)
                        },
                        activeTrackColor = when (zonaFocus(focusLevel.toInt())) {
                            ZonaFocus.ALTO  -> Color(0xFF00C853)
                            ZonaFocus.MEDIO -> Color(0xFFFFD700)
                            ZonaFocus.BAJO  -> Color(0xFFFF4444)
                        },
                        inactiveTrackColor = Color.White.copy(0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Contenido según zona de focus ──────────────────────────
                when (zonaFocus(focusLevel.toInt())) {

                    ZonaFocus.ALTO -> {
                        // Focus 8-10: Banner positivo + factores opcionales
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF00C853).copy(0.07f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00C853).copy(0.3f))
                        ) {
                            Text(
                                "🔥 Excelente ejecución — ¿qué lo hizo posible?",
                                color = Color(0xFF00C853),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "FACTOR CLAVE (opcional)",
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
                            FactorPositivo.entries.forEach { factor ->
                                val isSelected = positivoSelected == factor
                                Surface(
                                    modifier = Modifier.clickable {
                                        positivoSelected = if (positivoSelected == factor) null else factor
                                    },
                                    color = if (isSelected) Color(0xFF00C853).copy(0.12f) else Color.White.copy(0.04f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00C853).copy(0.6f) else CardBorderColor
                                    )
                                ) {
                                    Text(
                                        "${factor.emoji} ${factor.label}",
                                        color = if (isSelected) Color(0xFF00C853) else SecondaryText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    ZonaFocus.MEDIO -> {
                        // Focus 5-7: Factor de fricción opcional
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFD700).copy(0.06f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(0.25f))
                        ) {
                            Text(
                                "⚡ Completado — ¿hubo algo que lo dificultó?",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "FACTOR DE FRICCIÓN (opcional)",
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
                            FrictionFactor.entries.forEach { factor ->
                                val isSelected = frictionSelected == factor
                                Surface(
                                    modifier = Modifier.clickable {
                                        frictionSelected = if (frictionSelected == factor) null else factor
                                    },
                                    color = if (isSelected) Color(0xFFFFD700).copy(0.12f) else Color.White.copy(0.04f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFFFD700).copy(0.6f) else CardBorderColor
                                    )
                                ) {
                                    Text(
                                        "${factor.emoji} ${factor.label}",
                                        color = if (isSelected) Color(0xFFFFD700) else SecondaryText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    ZonaFocus.BAJO -> {
                        // Focus 1-4: Fricción + nota obligatoria
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFF4444).copy(0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF4444).copy(0.3f))
                        ) {
                            Text(
                                "⚠️ Focus bajo 5 — nota de ajuste obligatoria",
                                color = Color(0xFFFF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "FACTOR DE FRICCIÓN (opcional)",
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
                            FrictionFactor.entries.forEach { factor ->
                                val isSelected = frictionSelected == factor
                                Surface(
                                    modifier = Modifier.clickable {
                                        frictionSelected = if (frictionSelected == factor) null else factor
                                    },
                                    color = if (isSelected) Color(0xFFFF4444).copy(0.08f) else Color.White.copy(0.04f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFFF4444).copy(0.5f) else CardBorderColor
                                    )
                                ) {
                                    Text(
                                        "${factor.emoji} ${factor.label}",
                                        color = if (isSelected) Color(0xFFFF4444) else SecondaryText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

            } else {
                // ─────────────────────────────
                //  RAMA: NO LO HICE
                // ─────────────────────────────
                Text(
                    "¿POR QUÉ NO LO HICISTE?",
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
                    RazonNoCompletado.entries.forEach { razon ->
                        val isSelected = razonSelected == razon
                        Surface(
                            modifier = Modifier.clickable { razonSelected = razon },
                            color = if (isSelected) Color(0xFFFF4444).copy(0.08f) else Color.White.copy(0.04f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFFF4444).copy(0.5f) else CardBorderColor
                            )
                        ) {
                            Text(
                                "${razon.emoji} ${razon.label}",
                                color = if (isSelected) Color(0xFFFF4444) else SecondaryText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── NOTA DE AJUSTE ─────────────────────────────────────────────
            OutlinedTextField(
                value = adjustmentNote,
                onValueChange = { adjustmentNote = it; noteError = false },
                label = {
                    Text(
                        if (requiresNote) "Nota de ajuste (obligatoria)" else "Nota de ajuste (opcional)",
                        color = SecondaryText
                    )
                },
                placeholder = {
                    val placeholder = when {
                        !hecho                              -> "¿Qué cambiarías para asegurarlo mañana?"
                        zonaFocus(focusLevel.toInt()) == ZonaFocus.ALTO  -> "¿Cómo replicar este nivel mañana?"
                        zonaFocus(focusLevel.toInt()) == ZonaFocus.MEDIO -> "¿Qué podrías optimizar?"
                        else                                -> "¿Qué cambiarías para la próxima vez?"
                    }
                    Text(placeholder, color = SecondaryText.copy(0.4f), fontSize = 12.sp)
                },
                isError = noteError,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = when {
                        !hecho                              -> Color(0xFFFF4444)
                        zonaFocus(focusLevel.toInt()) == ZonaFocus.ALTO  -> Color(0xFF00C853)
                        zonaFocus(focusLevel.toInt()) == ZonaFocus.MEDIO -> Color(0xFFFFD700)
                        else                                -> Color(0xFFFF4444)
                    },
                    unfocusedBorderColor = CardBorderColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (noteError) {
                Text(
                    if (!hecho) "La nota es obligatoria cuando no completaste el hábito"
                    else "La nota es obligatoria con focus menor a 5",
                    color = Color.Red,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── BOTÓN GUARDAR ───────────────────────────────────────────────────
        Button(
            onClick = {
                if (completado == null) {
                    completadoError = true
                    return@Button
                }
                if (requiresNote && adjustmentNote.isBlank()) {
                    noteError = true
                    return@Button
                }

                onSave(
                    AnalisisHabito(
                        habitoId          = habito.id,
                        habitoNombre      = habito.nombre,
                        completado        = completado!!,
                        focusLevel        = if (completado == true) focusLevel.toInt() else 0,
                        frictionFactor    = frictionSelected?.name ?: "",
                        factorPositivo    = positivoSelected?.name ?: "",
                        razonNoCompletado = if (completado == false) razonSelected?.name ?: "" else "",
                        adjustmentNote    = adjustmentNote.trim()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("GUARDAR ANÁLISIS", color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}