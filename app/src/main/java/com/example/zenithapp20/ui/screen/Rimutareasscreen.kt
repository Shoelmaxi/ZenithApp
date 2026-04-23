package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.zenithapp20.data.model.TareaItem
import com.example.zenithapp20.ui.components.*
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.TareasViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RimuTareasScreen(
    navController: NavController,
    viewModel: TareasViewModel
) {
    val listaTareas  by viewModel.tareasFiltradas.collectAsState()
    var tareaAEditar by remember { mutableStateOf<TareaItem?>(null) }
    var showSheet    by remember { mutableStateOf(false) }
    var tareaInfo    by remember { mutableStateOf<TareaItem?>(null) }

    LaunchedEffect(Unit) {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        viewModel.cambiarFechaFiltro(hoy)
    }

    val pendientes  = listaTareas.filter { !it.completada }
    val completadas = listaTareas.filter { it.completada }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
                }
                Column {
                    Text("TAREAS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        "${pendientes.size} pendiente${if (pendientes.size != 1) "s" else ""}",
                        color    = SecondaryText,
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(onClick = { tareaAEditar = null; showSheet = true }) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF4CAF50))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (listaTareas.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sin tareas pendientes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Toca + para agregar una", color = SecondaryText, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding      = PaddingValues(bottom = 32.dp)
            ) {
                if (pendientes.isNotEmpty()) {
                    item {
                        Text(
                            "PENDIENTES",
                            color         = SecondaryText,
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier      = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pendientes) { tarea ->
                        SwipeToDeleteContainer(
                            mensajeConfirmacion = "Se eliminará '${tarea.nombre}'.",
                            onDelete = { viewModel.eliminarTarea(tarea) }
                        ) {
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick     = { viewModel.toggleCompletada(tarea) },
                                    onLongClick = { tareaAEditar = tarea; showSheet = true }
                                )
                            ) {
                                RimuTareaItem(tarea = tarea, onInfoClick = { tareaInfo = tarea })
                            }
                        }
                    }
                }

                if (completadas.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "COMPLETADAS",
                            color         = SecondaryText,
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier      = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(completadas) { tarea ->
                        SwipeToDeleteContainer(
                            mensajeConfirmacion = "Se eliminará '${tarea.nombre}'.",
                            onDelete = { viewModel.eliminarTarea(tarea) }
                        ) {
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick     = { viewModel.toggleCompletada(tarea) },
                                    onLongClick = { tareaAEditar = tarea; showSheet = true }
                                )
                            ) {
                                RimuTareaItem(tarea = tarea, onInfoClick = { tareaInfo = tarea })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false; tareaAEditar = null },
            containerColor   = MainCardBackground
        ) {
            TareaForm(
                tareaAEditar = tareaAEditar,
                onSave = { nueva ->
                    val final = if (tareaAEditar != null) nueva.copy(id = tareaAEditar!!.id) else nueva
                    viewModel.guardarOActualizar(final)
                    showSheet    = false
                    tareaAEditar = null
                }
            )
        }
    }

    tareaInfo?.let { t ->
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        ItemInfoDialog(
            titulo      = t.nombre,
            descripcion = t.descripcion,
            detalles    = buildList {
                add("FECHA LÍMITE" to sdf.format(Date(t.fechaLimiteMillis)))
                add("PRIORIDAD"    to t.prioridad.nombre.uppercase())
                if (t.completada) add("ESTADO" to "✅ Completada")
            },
            onDismiss = { tareaInfo = null }
        )
    }
}