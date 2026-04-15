package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.EstadoLibro
import com.example.zenithapp20.data.model.Libro
import com.example.zenithapp20.data.model.SesionLectura
import com.example.zenithapp20.ui.components.ConfirmDeleteDialog
import com.example.zenithapp20.ui.components.SesionLecturaForm
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.LecturaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuLibroDetailScreen(
    navController: NavController,
    libroId: Long,
    viewModel: LecturaViewModel
) {
    val libro by viewModel.getLibroById(libroId).collectAsState(initial = null)
    val todasLasSesiones by viewModel.getSesionesDeLibro(libroId).collectAsState(initial = emptyList())
    val sesionesConInsight by viewModel.getSesionesConInsight(libroId).collectAsState(initial = emptyList())

    var showSesionSheet by remember { mutableStateOf(false) }
    var modoGuia by remember { mutableStateOf(false) }
    var sesionAEliminar by remember { mutableStateOf<SesionLectura?>(null) }

    val sesionesAMostrar = if (modoGuia) sesionesConInsight else todasLasSesiones

    if (sesionAEliminar != null) {
        ConfirmDeleteDialog(
            mensaje = "Se eliminará esta sesión de lectura.",
            onConfirm = { viewModel.eliminarSesion(sesionAEliminar!!); sesionAEliminar = null },
            onDismiss = { sesionAEliminar = null }
        )
    }

    libro?.let { lib ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBackground)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── HEADER ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lib.titulo,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (lib.autor.isNotBlank()) {
                        Text(lib.autor, color = SecondaryText, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── PROGRESS CARD ─────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = MainCardBackground,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (lib.estado == EstadoLibro.TERMINADO) Color(0xFF00C853).copy(0.4f) else CardBorderColor
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${lib.paginaActual} / ${lib.paginasTotales} páginas",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "${lib.paginasRestantes} restantes · ${lib.categoria.emoji} ${lib.categoria.label}",
                                color = SecondaryText,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            "${lib.porcentaje}%",
                            color = when (lib.estado) {
                                EstadoLibro.TERMINADO -> Color(0xFF00C853)
                                EstadoLibro.LEYENDO   -> Color(0xFF4CAF50)
                                else -> SecondaryText
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.White.copy(0.05f), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(lib.progresoFloat)
                                .height(6.dp)
                                .background(
                                    if (lib.estado == EstadoLibro.TERMINADO) Color(0xFF00C853)
                                    else Color(0xFF4CAF50),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }

                    if (lib.estado != EstadoLibro.TERMINADO && lib.estado != EstadoLibro.ABANDONADO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showSesionSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "REGISTRAR SESIÓN",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    } else if (lib.estado == EstadoLibro.TERMINADO) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "✅ Libro completado — ${todasLasSesiones.size} sesiones",
                            color = Color(0xFF00C853),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── TOGGLE HISTORIAL / GUÍA ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(44.dp)
                    .background(Color.White.copy(0.04f), RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToggleTab(
                    label = "HISTORIAL",
                    count = todasLasSesiones.size,
                    isSelected = !modoGuia,
                    modifier = Modifier.weight(1f)
                ) { modoGuia = false }

                ToggleTab(
                    label = "GUÍA ESTRATÉGICA",
                    count = sesionesConInsight.size,
                    isSelected = modoGuia,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                ) { modoGuia = true }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── LISTA DE SESIONES ─────────────────────────────────────────────
            if (sesionesAMostrar.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (modoGuia) "Sin insights registrados todavía.\nAnota lecciones en tus próximas sesiones."
                        else "Sin sesiones registradas todavía.",
                        color = SecondaryText,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(sesionesAMostrar) { sesion ->
                        SesionCard(
                            sesion = sesion,
                            modoGuia = modoGuia,
                            onDelete = { sesionAEliminar = sesion }
                        )
                    }
                }
            }
        }
    } ?: run {
        // Libro no encontrado (raro — por si acaba de borrarse)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        }
    }

    if (showSesionSheet) {
        libro?.let { lib ->
            ModalBottomSheet(
                onDismissRequest = { showSesionSheet = false },
                containerColor = MainCardBackground
            ) {
                SesionLecturaForm(
                    libro = lib,
                    onSave = { sesion ->
                        viewModel.guardarSesion(sesion, lib)
                        showSesionSheet = false
                    }
                )
            }
        }
    }
}

// ── COMPONENTES INTERNOS ────────────────────────────────────────────────────

@Composable
private fun ToggleTab(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color = Color(0xFF4CAF50),
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) color.copy(0.12f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                color = if (isSelected) color else SecondaryText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) color.copy(0.2f) else Color.White.copy(0.06f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        count.toString(),
                        color = if (isSelected) color else SecondaryText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun SesionCard(
    sesion: SesionLectura,
    modoGuia: Boolean,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "ES"))
    val fechaStr = sdf.format(Date(sesion.fecha))

    com.example.zenithapp20.ui.components.SwipeToDeleteContainer(
        mensajeConfirmacion = "Se eliminará esta sesión de lectura.",
        onDelete = onDelete
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MainCardBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                if (sesion.tieneInsight) Color(0xFFFFD700).copy(0.25f) else CardBorderColor
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // ── Cabecera de la sesión ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de páginas
                    Surface(
                        color = Color(0xFF4CAF50).copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.3f))
                    ) {
                        Text(
                            "Págs. ${sesion.paginaInicio}–${sesion.paginaFin}",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "+${sesion.paginasLeidas} págs.",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(sesion.categoria.emoji, fontSize = 14.sp)
                        if (sesion.esMinimoCumplido) {
                            Text("1%", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Text(fechaStr, color = SecondaryText, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))

                // ── Insights (lección + estrategia) ──
                if (sesion.leccionClave.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightBlock(
                        label = "LECCIÓN",
                        texto = sesion.leccionClave,
                        color = Color.White,
                        labelColor = SecondaryText
                    )
                }

                if (sesion.aplicacionEstrategica.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InsightBlock(
                        label = "ESTRATEGIA",
                        texto = sesion.aplicacionEstrategica,
                        color = Color(0xFFFFD700),
                        labelColor = Color(0xFFFFD700).copy(0.7f)
                    )
                }

                // Sacrificio al final, discreto
                if (sesion.sacrificio.isNotBlank() && !modoGuia) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Reemplazó: ${sesion.sacrificio}",
                        color = SecondaryText.copy(0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightBlock(
    label: String,
    texto: String,
    color: Color,
    labelColor: Color
) {
    Column {
        Text(label, color = labelColor, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(texto, color = color, fontSize = 13.sp, lineHeight = 18.sp)
    }
}