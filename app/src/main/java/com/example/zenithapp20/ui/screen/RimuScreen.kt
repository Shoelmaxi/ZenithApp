package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.Prioridad
import com.example.zenithapp20.data.model.TareaItem
import com.example.zenithapp20.ui.components.AgendaForm
import com.example.zenithapp20.ui.components.HabitoQuickItem
import com.example.zenithapp20.ui.components.TareaForm
import java.text.SimpleDateFormat
import java.util.*
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.ui.components.HabitoForm
import androidx.compose.material.icons.filled.Payments

import androidx.compose.runtime.collectAsState
import com.example.zenithapp20.ui.components.SwipeToDeleteContainer
import com.example.zenithapp20.ui.viewmodel.AgendaViewModel
import com.example.zenithapp20.ui.viewmodel.TareasViewModel // Asumiendo este nombre
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel // Asumiendo este nombre

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RimuScreen(
    navController: NavController,
    agendaViewModel: AgendaViewModel,
    tareasViewModel: TareasViewModel,
    habitosViewModel: HabitosViewModel
) {
    // --- ESTADOS DE ROOM ---
    val listaAgenda by agendaViewModel.agendaFiltrada.collectAsState()
    val listaTareas by tareasViewModel.tareasFiltradas.collectAsState()
    val listaHabitos by habitosViewModel.habitos.collectAsState()

    // Estados de UI para Edición
    var agendaAEditar by remember { mutableStateOf<AgendaItem?>(null) }
    var tareaAEditar by remember { mutableStateOf<TareaItem?>(null) }
    var habitoAEditar by remember { mutableStateOf<Habito?>(null) } // NUEVO: Estado para editar hábito

    var fechaSeleccionada by remember { mutableStateOf(Calendar.getInstance()) }

    // (Lógica de fecha se mantiene igual...)
    val diaSemanaActual = remember(fechaSeleccionada) {
        val diasMap = mapOf(
            Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
            Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S", Calendar.SUNDAY to "D"
        )
        val dia = diasMap[fechaSeleccionada.get(Calendar.DAY_OF_WEEK)] ?: ""
        agendaViewModel.cambiarDia(dia)
        dia
    }

    val inicioDiaSeleccionado = remember(fechaSeleccionada) {
        Calendar.getInstance().apply {
            timeInMillis = fechaSeleccionada.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    LaunchedEffect(inicioDiaSeleccionado) {
        tareasViewModel.cambiarFechaFiltro(inicioDiaSeleccionado)
    }

    var showAgendaSheet by remember { mutableStateOf(false) }
    var showTareaSheet by remember { mutableStateOf(false) }
    var showHabitoSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(DeepBackground).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
                .background(MainCardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            RimuHeader(fecha = fechaSeleccionada, onFechaCambiada = { fechaSeleccionada = it }, navController = navController)

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 20.dp)) {

                // --- SECCIÓN AGENDA ---
                item { SeccionHeader(titulo = "AGENDA", onAddClick = { agendaAEditar = null; showAgendaSheet = true }) }
                if (listaAgenda.isEmpty()) {
                    item { EmptyStateText("No hay eventos para este día") }
                } else {
                    items(listaAgenda) { item ->
                        SwipeToDeleteContainer(
                            mensajeConfirmacion = "Se eliminará el evento '${item.nombre}'.",
                            onDelete = { agendaViewModel.eliminarItem(item) }
                        ) {
                            Box(modifier = Modifier.combinedClickable(
                                onClick = { agendaViewModel.toggleCompletado(item,diaSemanaActual) },
                                onLongClick = { agendaAEditar = item; showAgendaSheet = true } // EDITAR
                            )) {
                                RimuAgendaItem(item = item, isLast = false, diaActual = diaSemanaActual)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // --- SECCIÓN HÁBITOS ---
                item { SeccionHeader(titulo = "HÁBITOS DE HOY", onAddClick = { habitoAEditar = null; showHabitoSheet = true }) }
                if (listaHabitos.isEmpty()) {
                    item { EmptyStateText("Agrega un hábito para empezar") }
                } else {
                    items(listaHabitos) { habito ->
                        // Agregamos Swipe para borrar hábitos también
                        SwipeToDeleteContainer(
                            mensajeConfirmacion = "Se eliminará el hábito '${habito.nombre}' y toda su racha.",
                            onDelete = { habitosViewModel.eliminarHabito(habito) }
                        ) {
                            HabitoQuickItem(
                                habito = habito,
                                isCompletadoHoy = habitosViewModel.verificarSiCompletadoEnFecha(
                                    habito,
                                    fechaSeleccionada.timeInMillis
                                ),
                                onCheckClick = {
                                    habitosViewModel.toggleCheckEnFecha(habito, fechaSeleccionada.timeInMillis)
                                },
                                onLongClick = { habitoAEditar = habito; showHabitoSheet = true }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // --- SECCIÓN TAREAS ---
                item { SeccionHeader(titulo = "TAREAS", onAddClick = { tareaAEditar = null; showTareaSheet = true }) }
                if (listaTareas.isEmpty()) {
                    item { EmptyStateText("Sin tareas pendientes") }
                } else {
                    items(listaTareas) { tarea ->
                        SwipeToDeleteContainer(
                            mensajeConfirmacion = "Se eliminará la tarea '${tarea.nombre}'.",
                            onDelete = { tareasViewModel.eliminarTarea(tarea) }
                        ) {
                            Box(modifier = Modifier.combinedClickable(
                                onClick = { tareasViewModel.toggleCompletada(tarea) },
                                onLongClick = { tareaAEditar = tarea; showTareaSheet = true } // EDITAR
                            )) {
                                RimuTareaItem(tarea = tarea)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- MODALS ACTUALIZADOS PARA EDICIÓN ---
    if (showHabitoSheet) {
        ModalBottomSheet(onDismissRequest = { showHabitoSheet = false; habitoAEditar = null }, containerColor = MainCardBackground) {
            HabitoForm(
                habitoAEditar = habitoAEditar, // Pasamos el hábito para editar
                onSave = { habitos ->
                    habitosViewModel.guardarHabito(habitos)
                    showHabitoSheet = false
                    habitoAEditar = null
                }
            )
        }
    }

    if (showAgendaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAgendaSheet = false; agendaAEditar = null },
            containerColor = MainCardBackground
        ) {
            AgendaForm(
                itemAEditar = agendaAEditar,
                onSave = { nuevo ->
                    val itemFinal = if (agendaAEditar != null)
                        nuevo.copy(id = agendaAEditar!!.id) else nuevo
                    agendaViewModel.guardarOActualizar(itemFinal)
                    showAgendaSheet = false
                    agendaAEditar = null
                }
            )
        }
    }

    if (showTareaSheet) {
        ModalBottomSheet(onDismissRequest = { showTareaSheet = false; tareaAEditar = null }, containerColor = MainCardBackground) {
            TareaForm(
                // Aquí igual, pasar 'tareaAEditar = tareaAEditar' al componente
                onSave = { nueva ->
                    val tareaFinal = if (tareaAEditar != null) nueva.copy(id = tareaAEditar!!.id) else nueva
                    tareasViewModel.guardarOActualizar(tareaFinal)
                    showTareaSheet = false
                    tareaAEditar = null
                }
            )
        }
    }
}

// --- CONTENEDOR PARA DESLIZAR Y BORRAR ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuHeader(fecha: Calendar, onFechaCambiada: (Calendar) -> Unit, navController: NavController) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha.timeInMillis)

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                    val local = Calendar.getInstance().apply { set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0) }
                    onFechaCambiada(local)
                }
                showDatePicker = false
            }) { Text("OK", color = Color.Green) }
        }) { DatePicker(state = datePickerState) }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Sigue firme,", color = PrimaryText, fontSize = 28.sp, fontWeight = FontWeight.Light)
            Text("cada paso cuenta.", color = PrimaryText, fontSize = 28.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.clickable { showDatePicker = true }, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fecha.time) == SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())) "HOY"
                    else SimpleDateFormat("EEEE, d MMM", Locale("es", "ES")).format(fecha.time).uppercase(),
                    color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Black
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = SecondaryText, modifier = Modifier.size(16.dp))
            }
        }

        // --- BOTONES DE ACCIÓN ACTUALIZADOS (AHORA SON 3) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Reducimos un poco el espacio para que quepan mejor los 3
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // Botón Finanzas (NUEVO)
            IconButton(
                onClick = { navController.navigate("rimu_finance") },
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).border(1.dp, CardBorderColor, CircleShape)
            ) { Icon(Icons.Default.Payments, "Finanzas", tint = Color.White, modifier = Modifier.size(18.dp)) }

            // Botón Gym
            IconButton(
                onClick = { navController.navigate("rimu_gym") },
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).border(1.dp, CardBorderColor, CircleShape)
            ) { Icon(Icons.Default.FitnessCenter, "Gym", tint = Color.White, modifier = Modifier.size(18.dp)) }

            // Botón Estadísticas
            IconButton(
                onClick = { navController.navigate("rimu_habits_stats") },
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).border(1.dp, CardBorderColor, CircleShape)
            ) { Icon(Icons.Default.BarChart, "Estadísticas", tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun RimuAgendaItem(item: AgendaItem, isLast: Boolean, diaActual: String) {
    val completadoHoy = item.diasCompletados.contains(diaActual)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(item.hora, color = SecondaryText, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.size(14.dp).border(1.dp, CardBorderColor, CircleShape), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(2.dp).background(SecondaryText, CircleShape))
            }
            if (!isLast) Box(modifier = Modifier.width(1.dp).height(80.dp).background(CardBorderColor))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.weight(1f).height(85.dp),
            color = MainCardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (completadoHoy) Color.Green else CardBorderColor)
        ) {
            Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).border(1.dp, if (completadoHoy) Color.Green else CardBorderColor, CircleShape).background(if (completadoHoy) Color.Green.copy(0.1f) else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                    if (completadoHoy) Box(modifier = Modifier.size(8.dp).background(Color.Green, CircleShape))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(item.nombre, color = if (completadoHoy) Color.Gray else PrimaryText, fontSize = 18.sp)
                    Text(item.descripcion, color = SecondaryText, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RimuTareaItem(tarea: TareaItem) {
    // --- SOLUCIÓN AL DESFASE: Forzamos TimeZone UTC al formatear ---
    val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val fechaStr = sdf.format(Date(tarea.fechaLimiteMillis))

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(22.dp).border(1.5.dp, if(tarea.completada) Color.Gray else tarea.prioridad.color, CircleShape), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(4.dp).background(if(tarea.completada) Color.Gray else tarea.prioridad.color, CircleShape))
            }
        }
        Surface(
            modifier = Modifier.weight(1f).height(85.dp),
            color = MainCardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if(tarea.completada) Color.Green.copy(0.3f) else CardBorderColor)
        ) {
            Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = tarea.nombre, color = if(tarea.completada) Color.Gray else Color.White, fontSize = 18.sp, style = TextStyle(textDecoration = if(tarea.completada) TextDecoration.LineThrough else null))
                    Text(text = "LÍMITE: $fechaStr".uppercase(), color = if(tarea.completada) Color.DarkGray else tarea.prioridad.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (tarea.prioridad == Prioridad.URGENTE && !tarea.completada) Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable fun SeccionHeader(titulo: String, onAddClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Add, null, tint = SecondaryText) }
    }
}

@Composable fun EmptyStateText(mensaje: String) {
    Text(mensaje, color = SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
}