package com.example.zenithapp20.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.Prioridad
import com.example.zenithapp20.data.model.TareaItem
import com.example.zenithapp20.data.model.TipoAgenda
import com.example.zenithapp20.ui.components.*
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RimuScreen(
    navController: NavController,
    agendaViewModel: AgendaViewModel,
    tareasViewModel: TareasViewModel,
    habitosViewModel: HabitosViewModel,
    icViewModel: IngenieriaConductualViewModel,
    lecturaViewModel: LecturaViewModel
) {
    val listaAgenda  by agendaViewModel.agendaFiltrada.collectAsState()
    val listaHabitos by habitosViewModel.habitos.collectAsState()
    val oracleInsight by lecturaViewModel.oracleInsight.collectAsState()

    var agendaAEditar    by remember { mutableStateOf<AgendaItem?>(null) }
    var habitoAEditar    by remember { mutableStateOf<Habito?>(null) }
    var habitoParaAnalisis by remember { mutableStateOf<Habito?>(null) }
    var habitoInfo       by remember { mutableStateOf<Habito?>(null) }
    var agendaInfo       by remember { mutableStateOf<AgendaItem?>(null) }
    var agendaExpandida  by remember { mutableStateOf(false) }
    var showMasSheet     by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember { mutableStateOf(Calendar.getInstance()) }

    val diaSemanaActual = remember(fechaSeleccionada) {
        val diasMap = mapOf(
            Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
            Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "D"
        )
        val dia = diasMap[fechaSeleccionada.get(Calendar.DAY_OF_WEEK)] ?: ""
        agendaViewModel.cambiarDia(dia)
        dia
    }

    val inicioDiaSeleccionado = remember(fechaSeleccionada) {
        Calendar.getInstance().apply {
            timeInMillis = fechaSeleccionada.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    LaunchedEffect(inicioDiaSeleccionado) {
        agendaViewModel.cambiarFecha(inicioDiaSeleccionado)
    }

    var showAgendaSheet by remember { mutableStateOf(false) }
    var showHabitoSheet by remember { mutableStateOf(false) }

    // ── Sheet de "Más" ────────────────────────────────────────────────────────
    if (showMasSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMasSheet = false },
            containerColor   = MainCardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "MÁS SECCIONES",
                    color         = SecondaryText,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                MasOpcionItem("Finanzas",   Icons.Default.Payments,       Color(0xFF4CAF50)) {
                    showMasSheet = false; navController.navigate("rimu_finance")
                }
                MasOpcionItem("Semana",     Icons.Default.CalendarMonth,  Color(0xFF2196F3)) {
                    showMasSheet = false; navController.navigate("rimu_week")
                }
                MasOpcionItem("Tareas",     Icons.Default.CheckCircle,    Color(0xFFFFD700)) {
                    showMasSheet = false; navController.navigate("rimu_tareas")
                }
                MasOpcionItem("Backup",     Icons.Default.Settings,       SecondaryText) {
                    showMasSheet = false; navController.navigate("rimu_backup")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
                .background(MainCardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            // ── HEADER ────────────────────────────────────────────────────────
            RimuHeader(
                fecha           = fechaSeleccionada,
                onFechaCambiada = { fechaSeleccionada = it },
                navController   = navController,
                onMasClick      = { showMasSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── EL ORÁCULO ────────────────────────────────────────────────────
            oracleInsight?.let { insight ->
                OracleCard(
                    texto       = insight.texto,
                    tituloLibro = insight.tituloLibro,
                    onRefresh   = { lecturaViewModel.cargarOracle() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {

                // ── HÁBITOS (primero) ─────────────────────────────────────────
                item {
                    SeccionHeader(
                        titulo    = "HÁBITOS DE HOY",
                        onAddClick = { habitoAEditar = null; showHabitoSheet = true }
                    )
                }
                if (listaHabitos.isEmpty()) {
                    item { EmptyStateText("Agrega un hábito para empezar") }
                } else {
                    items(listaHabitos.size) { index ->
                        val habito = listaHabitos[index]
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn() + slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec  = spring(stiffness = 300f)
                            )
                        ) {
                            SwipeToDeleteContainer(
                                mensajeConfirmacion = "Se eliminará '${habito.nombre}' y toda su racha.",
                                onDelete            = { habitosViewModel.eliminarHabito(habito) }
                            ) {
                                HabitoQuickItem(
                                    habito           = habito,
                                    isCompletadoHoy  = habitosViewModel.verificarSiCompletadoEnFecha(
                                        habito, fechaSeleccionada.timeInMillis
                                    ),
                                    onCheckClick  = { habitoParaAnalisis = habito },
                                    onLongClick   = { habitoAEditar = habito; showHabitoSheet = true },
                                    onInfoClick   = { habitoInfo = habito }
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ── AGENDA (colapsable) ───────────────────────────────────────
                item {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable { agendaExpandida = !agendaExpandida },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "AGENDA",
                                color         = SecondaryText,
                                fontSize      = 12.sp,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            if (listaAgenda.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                // Badge con cantidad de eventos
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF4CAF50).copy(0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${listaAgenda.size}",
                                        color      = Color(0xFF4CAF50),
                                        fontSize   = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick  = { agendaAEditar = null; showAgendaSheet = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (agendaExpandida) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint     = SecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                item {
                    AnimatedVisibility(visible = agendaExpandida) {
                        Column(modifier = Modifier.animateContentSize()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (listaAgenda.isEmpty()) {
                                EmptyStateText("No hay eventos para este día")
                            } else {
                                listaAgenda.forEach { item ->
                                    SwipeToDeleteContainer(
                                        mensajeConfirmacion = "Se eliminará '${item.nombre}'.",
                                        onDelete            = { agendaViewModel.eliminarItem(item) }
                                    ) {
                                        Box(
                                            modifier = Modifier.combinedClickable(
                                                onClick = {
                                                    agendaViewModel.toggleCompletado(item, diaSemanaActual)
                                                },
                                                onLongClick = {
                                                    agendaAEditar = item
                                                    showAgendaSheet = true
                                                }
                                            )
                                        ) {
                                            RimuAgendaItem(
                                                item       = item,
                                                isLast     = false,
                                                diaActual  = diaSemanaActual,
                                                onInfoClick = { agendaInfo = item }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── SHEET ANÁLISIS AAA ─────────────────────────────────────────────────────
    habitoParaAnalisis?.let { habito ->
        ModalBottomSheet(
            onDismissRequest = { habitoParaAnalisis = null },
            containerColor   = MainCardBackground
        ) {
            AnalisisAAASheet(
                habito            = habito,
                estaCompletadoHoy = habitosViewModel.verificarSiCompletadoEnFecha(
                    habito, fechaSeleccionada.timeInMillis
                ),
                onSave = { analisis ->
                    icViewModel.guardarAnalisis(analisis)
                    habitosViewModel.setCheckEnFecha(
                        habito,
                        fechaSeleccionada.timeInMillis,
                        analisis.completado
                    )
                    habitoParaAnalisis = null
                },
                onSkip = { habitoParaAnalisis = null }
            )
        }
    }

    // ── SHEETS DE EDICIÓN ──────────────────────────────────────────────────────
    if (showHabitoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHabitoSheet = false; habitoAEditar = null },
            containerColor   = MainCardBackground
        ) {
            HabitoForm(
                habitoAEditar = habitoAEditar,
                onSave = { habito ->
                    habitosViewModel.guardarHabito(habito)
                    showHabitoSheet = false
                    habitoAEditar   = null
                }
            )
        }
    }

    if (showAgendaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAgendaSheet = false; agendaAEditar = null },
            containerColor   = MainCardBackground
        ) {
            AgendaForm(
                itemAEditar = agendaAEditar,
                onSave = { nuevo ->
                    val itemFinal = if (agendaAEditar != null)
                        nuevo.copy(id = agendaAEditar!!.id) else nuevo
                    agendaViewModel.guardarOActualizar(itemFinal)
                    showAgendaSheet = false
                    agendaAEditar   = null
                }
            )
        }
    }

    // ── DIÁLOGOS DE INFO ───────────────────────────────────────────────────────
    habitoInfo?.let { h ->
        ItemInfoDialog(
            titulo      = h.nombre,
            descripcion = h.descripcion,
            detalles    = buildList {
                add("META"      to h.meta)
                add("CATEGORÍA" to h.categoria)
                if (h.rachaDias > 0) {
                    add("RACHA ACTIVA" to "${h.rachaDias} día${if (h.rachaDias > 1) "s" else ""} seguido${if (h.rachaDias > 1) "s" else ""} 🔥")
                }
            },
            onDismiss = { habitoInfo = null }
        )
    }

    agendaInfo?.let { a ->
        val tipoTexto = when (a.tipo) {
            TipoAgenda.RECURRENTE       -> "Recurrente · ${a.dias.joinToString(", ")}"
            TipoAgenda.FECHA_ESPECIFICA -> "Fecha única"
        }
        ItemInfoDialog(
            titulo      = a.nombre,
            descripcion = a.descripcion,
            detalles    = buildList {
                add("HORA" to a.hora)
                add("TIPO" to tipoTexto)
            },
            onDismiss = { agendaInfo = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HEADER
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuHeader(
    fecha: Calendar,
    onFechaCambiada: (Calendar) -> Unit,
    navController: NavController,
    onMasClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha.timeInMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                        val local = Calendar.getInstance().apply {
                            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                        }
                        onFechaCambiada(local)
                    }
                    showDatePicker = false
                }) { Text("OK", color = Color.Green) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Fila de navegación: 5 botones diarios + botón Más ───────────────
        data class NavBtn(val icon: ImageVector, val desc: String, val route: String)
        val botonesDiarios = listOf(
            NavBtn(Icons.Default.FitnessCenter,        "Gym",          "rimu_gym"),
            NavBtn(Icons.Default.BarChart,             "Rendimiento",  "rimu_habits_stats"),
            NavBtn(Icons.Default.Dashboard,            "Resumen",      "rimu_summary"),
            NavBtn(Icons.AutoMirrored.Filled.MenuBook, "Lectura",      "rimu_lectura"),
            NavBtn(Icons.Default.Bolt,                 "Sistema",      "rimu_sistema"),
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            botonesDiarios.forEach { btn ->
                IconButton(
                    onClick  = { navController.navigate(btn.route) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, CardBorderColor, CircleShape)
                ) {
                    Icon(btn.icon, btn.desc, tint = Color.White, modifier = Modifier.size(17.dp))
                }
            }
            // Botón "⋯ Más"
            IconButton(
                onClick  = onMasClick,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, CardBorderColor, CircleShape)
            ) {
                Text("⋯", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Sigue firme,",      color = PrimaryText, fontSize = 26.sp, fontWeight = FontWeight.Light)
        Text("cada paso cuenta.", color = PrimaryText, fontSize = 26.sp, fontWeight = FontWeight.Light)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier          = Modifier.clickable { showDatePicker = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            val esHoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fecha.time) ==
                    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            Text(
                text = if (esHoy) "HOY"
                else SimpleDateFormat("EEEE, d MMM", Locale("es", "ES"))
                    .format(fecha.time).uppercase(),
                color      = SecondaryText,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Black
            )
            Icon(Icons.Default.ArrowDropDown, null, tint = SecondaryText, modifier = Modifier.size(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  EL ORÁCULO — card pequeña con cita estratégica del día
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OracleCard(
    texto: String,
    tituloLibro: String,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = Color(0xFFFFD700).copy(0.05f),
        shape    = RoundedCornerShape(16.dp),
        border   = BorderStroke(1.dp, Color(0xFFFFD700).copy(0.2f))
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("📖", fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ORDEN DEL DÍA",
                    color         = Color(0xFFFFD700).copy(0.7f),
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    texto,
                    color      = Color.White.copy(0.85f),
                    fontSize   = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "— $tituloLibro",
                    color    = SecondaryText,
                    fontSize = 10.sp
                )
            }
            // Botón de refrescar para obtener otra cita
            IconButton(
                onClick  = onRefresh,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Nueva cita",
                    tint     = SecondaryText.copy(0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  OPCIÓN DEL SHEET "MÁS"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MasOpcionItem(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color    = Color.White.copy(0.03f),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AGENDA ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RimuAgendaItem(
    item: AgendaItem,
    isLast: Boolean,
    diaActual: String,
    onInfoClick: () -> Unit = {}
) {
    val completadoHoy = item.diasCompletados.contains(diaActual)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(item.hora, color = SecondaryText, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .border(1.dp, CardBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(2.dp).background(SecondaryText, CircleShape))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).height(80.dp).background(CardBorderColor))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier.weight(1f).height(85.dp),
            color    = MainCardBackground,
            shape    = RoundedCornerShape(20.dp),
            border   = BorderStroke(1.dp, if (completadoHoy) Color.Green else CardBorderColor)
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(1.dp, if (completadoHoy) Color.Green else CardBorderColor, CircleShape)
                            .background(
                                if (completadoHoy) Color.Green.copy(0.1f) else Color.Transparent,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completadoHoy) {
                            Box(modifier = Modifier.size(8.dp).background(Color.Green, CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(item.nombre, color = if (completadoHoy) Color.Gray else PrimaryText, fontSize = 16.sp)
                        if (item.descripcion.isNotBlank()) {
                            Text(item.descripcion, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
                IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ver información",
                        tint     = SecondaryText.copy(0.45f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TAREA ITEM (se mantiene para RimuTareasScreen)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RimuTareaItem(
    tarea: TareaItem,
    onInfoClick: () -> Unit = {}
) {
    val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val fechaStr = sdf.format(Date(tarea.fechaLimiteMillis))

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.5.dp, if (tarea.completada) Color.Gray else tarea.prioridad.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(if (tarea.completada) Color.Gray else tarea.prioridad.color, CircleShape)
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f).height(85.dp),
            color    = MainCardBackground,
            shape    = RoundedCornerShape(20.dp),
            border   = BorderStroke(1.dp, if (tarea.completada) Color.Green.copy(0.3f) else CardBorderColor)
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = tarea.nombre,
                        color = if (tarea.completada) Color.Gray else Color.White,
                        fontSize = 17.sp,
                        style = TextStyle(textDecoration = if (tarea.completada) TextDecoration.LineThrough else null)
                    )
                    Text(
                        text       = "LÍMITE: $fechaStr".uppercase(),
                        color      = if (tarea.completada) Color.DarkGray else tarea.prioridad.color,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tarea.prioridad == Prioridad.URGENTE && !tarea.completada) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Ver información",
                            tint     = SecondaryText.copy(0.45f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SeccionHeader(titulo: String, onAddClick: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(titulo, color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, null, tint = SecondaryText)
        }
    }
}

@Composable
fun EmptyStateText(mensaje: String) {
    Text(mensaje, color = SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
}