package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.ReflexionDiaria
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.IngenieriaConductualViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuReflexionScreen(
    navController: NavController,
    viewModel: IngenieriaConductualViewModel
) {
    val reflexionHoy by viewModel.reflexionHoy.collectAsState()
    val historial by viewModel.reflexiones.collectAsState()
    var modoHistorial by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
                }
                Column {
                    Text("DIARIO DEL AJEDRECISTA", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("Reflexión Nocturna", color = SecondaryText, fontSize = 11.sp)
                }
            }
            TextButton(onClick = { modoHistorial = !modoHistorial }) {
                Text(if (modoHistorial) "HOY" else "HISTORIAL",
                    color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (modoHistorial) {
            // HISTORIAL
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (historial.isEmpty()) {
                    item {
                        Text("Sin reflexiones guardadas todavía.", color = SecondaryText,
                            fontSize = 14.sp, modifier = Modifier.padding(top = 24.dp))
                    }
                } else {
                    items(historial) { r -> ReflexionCard(r) }
                }
            }
        } else {
            // FORMULARIO DE HOY
            val yaExiste = reflexionHoy != null
            if (yaExiste) {
                // Mostrar la reflexión de hoy
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50).copy(0.06f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.3f))
                    ) {
                        Text(
                            "✅ Reflexión de hoy guardada",
                            color = Color(0xFF4CAF50),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    reflexionHoy?.let { ReflexionCard(it) }
                }
            } else {
                ReflexionForm(
                    onSave = { viewModel.guardarReflexion(it) }
                )
            }
        }
    }
}

@Composable
private fun ReflexionForm(onSave: (ReflexionDiaria) -> Unit) {
    var maestro by remember { mutableStateOf("") }
    var puntoCiego by remember { mutableStateOf("") }
    var apertura by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ReflexionField(
            label = "♟️ MOVIMIENTO MAESTRO",
            sublabel = "Tu logro clave de hoy",
            placeholder = "¿Qué decisión o acción marcó la diferencia hoy?",
            value = maestro,
            accentColor = Color(0xFF4CAF50),
            isError = "maestro" in errors,
            onValueChange = { maestro = it; errors = errors - "maestro" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReflexionField(
            label = "🔍 PUNTO CIEGO",
            sublabel = "Error impulsivo o reacción automática",
            placeholder = "¿Qué hiciste por impulso que no deberías haber hecho?",
            value = puntoCiego,
            accentColor = Color(0xFFFF4444),
            isError = "ciego" in errors,
            onValueChange = { puntoCiego = it; errors = errors - "ciego" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReflexionField(
            label = "🌅 APERTURA DE MAÑANA",
            sublabel = "Tarea principal del día siguiente",
            placeholder = "Si mañana solo pudieras hacer UNA cosa, ¿cuál sería?",
            value = apertura,
            accentColor = Color(0xFFFFD700),
            isError = "apertura" in errors,
            onValueChange = { apertura = it; errors = errors - "apertura" }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val newErrors = mutableSetOf<String>()
                if (maestro.isBlank()) newErrors.add("maestro")
                if (puntoCiego.isBlank()) newErrors.add("ciego")
                if (apertura.isBlank()) newErrors.add("apertura")
                errors = newErrors
                if (newErrors.isEmpty()) {
                    onSave(ReflexionDiaria(
                        movimientoMaestro = maestro.trim(),
                        puntoCiego = puntoCiego.trim(),
                        aperturaMañana = apertura.trim()
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("CERRAR EL DÍA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ReflexionField(
    label: String, sublabel: String, placeholder: String,
    value: String, accentColor: Color, isError: Boolean,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(sublabel, color = SecondaryText, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 400) onValueChange(it) },
            placeholder = { Text(placeholder, color = SecondaryText.copy(0.4f), fontSize = 12.sp) },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2, maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = CardBorderColor,
                errorBorderColor = Color.Red,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = accentColor
            ),
            shape = RoundedCornerShape(12.dp)
        )
        if (isError) Text("Campo obligatorio", color = Color.Red, fontSize = 10.sp)
    }
}

@Composable
private fun ReflexionCard(r: ReflexionDiaria) {
    val sdf = SimpleDateFormat("EEEE, dd MMM", Locale("es", "ES"))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MainCardBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(sdf.format(Date(r.fechaMillis)).uppercase(), color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            ReflexionBlock("♟️ MOVIMIENTO MAESTRO", r.movimientoMaestro, Color(0xFF4CAF50))
            ReflexionBlock("🔍 PUNTO CIEGO", r.puntoCiego, Color(0xFFFF4444))
            ReflexionBlock("🌅 APERTURA DE MAÑANA", r.aperturaMañana, Color(0xFFFFD700))
        }
    }
}

@Composable
private fun ReflexionBlock(label: String, texto: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = color.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(texto, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
    }
}