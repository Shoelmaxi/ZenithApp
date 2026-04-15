package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.zenithapp20.ui.components.ConfirmDeleteDialog
import com.example.zenithapp20.ui.components.LibroForm
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.LecturaViewModel
import com.example.zenithapp20.ui.components.SwipeToDeleteContainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuLecturaScreen(
    navController: NavController,
    viewModel: LecturaViewModel
) {
    val libros by viewModel.libros.collectAsState()
    var showLibroSheet by remember { mutableStateOf(false) }
    var libroAEditar by remember { mutableStateOf<Libro?>(null) }
    var libroAEliminar by remember { mutableStateOf<Libro?>(null) }

    // Stats derivadas
    val enCurso = libros.count { it.estado == EstadoLibro.LEYENDO }
    val terminados = libros.count { it.estado == EstadoLibro.TERMINADO }
    val totalPaginas = libros.sumOf { it.paginaActual }

    if (libroAEliminar != null) {
        ConfirmDeleteDialog(
            mensaje = "Se eliminará '${libroAEliminar!!.titulo}' y todas sus sesiones de lectura.",
            onConfirm = { viewModel.eliminarLibro(libroAEliminar!!); libroAEliminar = null },
            onDismiss = { libroAEliminar = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
                }
                Text("LECTURA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = { libroAEditar = null; showLibroSheet = true }) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF4CAF50))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── STATS BAR ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MainCardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LecturaStatItem("EN CURSO", enCurso.toString(), Color(0xFF4CAF50))
            VerticalDivider(modifier = Modifier.height(32.dp), color = CardBorderColor)
            LecturaStatItem("TERMINADOS", terminados.toString(), Color.White)
            VerticalDivider(modifier = Modifier.height(32.dp), color = CardBorderColor)
            LecturaStatItem("PÁGINAS LEÍDAS", totalPaginas.toString(), Color(0xFFFFD700))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (libros.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📚", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sin libros todavía", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Toca + para agregar tu primer libro", color = SecondaryText, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Sección por estado
                val leyendo = libros.filter { it.estado == EstadoLibro.LEYENDO }
                val pendientes = libros.filter { it.estado == EstadoLibro.PENDIENTE }
                val terminadosList = libros.filter { it.estado == EstadoLibro.TERMINADO }
                val abandonados = libros.filter { it.estado == EstadoLibro.ABANDONADO }

                if (leyendo.isNotEmpty()) {
                    item { SeccionLabel("LEYENDO AHORA") }
                    items(leyendo) { libro ->
                        LibroCard(
                            libro = libro,
                            onClick = { navController.navigate("rimu_lectura_detail/${libro.id}") },
                            onLongClick = { libroAEditar = libro; showLibroSheet = true },
                            onDelete = { libroAEliminar = libro }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                if (pendientes.isNotEmpty()) {
                    item { SeccionLabel("EN COLA") }
                    items(pendientes) { libro ->
                        LibroCard(
                            libro = libro,
                            onClick = { navController.navigate("rimu_lectura_detail/${libro.id}") },
                            onLongClick = { libroAEditar = libro; showLibroSheet = true },
                            onDelete = { libroAEliminar = libro }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                if (terminadosList.isNotEmpty()) {
                    item { SeccionLabel("COMPLETADOS") }
                    items(terminadosList) { libro ->
                        LibroCard(
                            libro = libro,
                            onClick = { navController.navigate("rimu_lectura_detail/${libro.id}") },
                            onLongClick = { libroAEditar = libro; showLibroSheet = true },
                            onDelete = { libroAEliminar = libro }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                if (abandonados.isNotEmpty()) {
                    item { SeccionLabel("ABANDONADOS") }
                    items(abandonados) { libro ->
                        LibroCard(
                            libro = libro,
                            onClick = { navController.navigate("rimu_lectura_detail/${libro.id}") },
                            onLongClick = { libroAEditar = libro; showLibroSheet = true },
                            onDelete = { libroAEliminar = libro }
                        )
                    }
                }
            }
        }
    }

    if (showLibroSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLibroSheet = false; libroAEditar = null },
            containerColor = MainCardBackground
        ) {
            LibroForm(
                libroAEditar = libroAEditar,
                onSave = { libro ->
                    viewModel.guardarLibro(libro)
                    showLibroSheet = false
                    libroAEditar = null
                }
            )
        }
    }
}

@Composable
private fun SeccionLabel(texto: String) {
    Text(
        texto,
        color = SecondaryText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun LecturaStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(label, color = SecondaryText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun LibroCard(
    libro: Libro,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val estadoColor = when (libro.estado) {
        EstadoLibro.LEYENDO    -> Color(0xFF4CAF50)
        EstadoLibro.TERMINADO  -> Color(0xFF00C853)
        EstadoLibro.PENDIENTE  -> SecondaryText
        EstadoLibro.ABANDONADO -> Color(0xFF666666)
    }

    SwipeToDeleteContainer(
        mensajeConfirmacion = "Se eliminará '${libro.titulo}' y todas sus sesiones.",
        onDelete = onDelete
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            color = MainCardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                if (libro.estado == EstadoLibro.LEYENDO) Color(0xFF4CAF50).copy(0.3f) else CardBorderColor
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            libro.titulo,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (libro.autor.isNotBlank()) {
                            Text(
                                libro.autor,
                                color = SecondaryText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Badge de categoría + estado
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = estadoColor.copy(0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, estadoColor.copy(0.4f))
                        ) {
                            Text(
                                "${libro.estado.emoji} ${libro.estado.label.uppercase()}",
                                color = estadoColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            libro.categoria.emoji,
                            fontSize = 16.sp
                        )
                    }
                }

                // Solo si no está pendiente o terminado sin páginas
                if (libro.estado != EstadoLibro.PENDIENTE || libro.paginaActual > 0) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Pág. ${libro.paginaActual} / ${libro.paginasTotales}",
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                        Text(
                            "${libro.porcentaje}%",
                            color = estadoColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(0.05f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(libro.progresoFloat)
                                .height(4.dp)
                                .background(estadoColor, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

// Necesitamos importar SwipeToDeleteContainer que ya existe en el proyecto