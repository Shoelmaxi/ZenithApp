package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.RutinaDia
import com.example.zenithapp20.ui.components.ActiveWorkoutOverlay
import com.example.zenithapp20.ui.components.GymConfigContent
import com.example.zenithapp20.ui.components.GymExercisePreviewItem
import com.example.zenithapp20.ui.components.SwipeToDeleteContainer
import com.example.zenithapp20.ui.components.WorkoutResumeDialog
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.GymViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuGymScreen(
    navController: NavController,
    viewModel: GymViewModel
) {
    val rutinasCargadas  by viewModel.todasLasRutinas.collectAsState()
    val workoutState     by viewModel.workoutState.collectAsState()
    val savedWorkout     by viewModel.savedWorkoutState.collectAsState()
    val entrenandoActivo = workoutState != null

    val diaHoy = remember {
        val diasMap = mapOf(
            Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
            Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "D"
        )
        diasMap[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)] ?: "L"
    }

    var diaSeleccionado  by remember { mutableStateOf(diaHoy) }
    var showConfigSheet  by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val rutinaSeleccionada = rutinasCargadas.find { it.dia == diaSeleccionado }

    // ── Diálogo de sesión guardada ─────────────────────────────────────────
    savedWorkout?.let { saved ->
        WorkoutResumeDialog(
            savedState = saved,
            onResume   = { viewModel.reanudarEntrenamientoGuardado() },
            onDiscard  = { viewModel.descartarEntrenamientoGuardado() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Cabecera ──────────────────────────────────────────────────────
        GymScreenHeader(
            diaSeleccionado  = diaSeleccionado,
            rutinasCargadas  = rutinasCargadas,
            onBack           = { navController.popBackStack() },
            onOpenSettings   = { showConfigSheet = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Selector de días ──────────────────────────────────────────────
        GymDaySelectorRow(
            diaActual       = diaHoy,
            diaSeleccionado = diaSeleccionado,
            rutinasCargadas = rutinasCargadas,
            onDiaClick      = { diaSeleccionado = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Contenido de la rutina ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
                .background(MainCardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            GymRoutineContent(
                rutinaSeleccionada = rutinaSeleccionada,
                diaSeleccionado    = diaSeleccionado,
                entrenandoActivo   = entrenandoActivo,
                onEliminarEjercicio = { ej -> viewModel.eliminarEjercicio(diaSeleccionado, ej) },
                onComenzar = {
                    rutinaSeleccionada?.ejercicios?.let { ejercicios ->
                        viewModel.iniciarEntrenamiento(ejercicios)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Sheet de configuración ────────────────────────────────────────────
    if (showConfigSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfigSheet = false },
            sheetState       = sheetState,
            containerColor   = MainCardBackground
        ) {
            GymConfigContent(
                diaActual         = diaSeleccionado,
                rutinasExistentes = rutinasCargadas,
                onDiaSelect       = { diaSeleccionado = it },
                onSaveRutina      = { nombre, ejercicios ->
                    viewModel.guardarRutina(
                        RutinaDia(dia = diaSeleccionado, nombreRutina = nombre, ejercicios = ejercicios)
                    )
                    showConfigSheet = false
                }
            )
        }
    }

    // ── Overlay de entrenamiento activo ───────────────────────────────────
    workoutState?.let { state ->
        ActiveWorkoutOverlay(
            workoutState  = state,
            onStateUpdate = { viewModel.actualizarWorkoutState(it) },
            onFinish      = { ejerciciosActualizados ->
                viewModel.finalizarEntrenamiento(diaSeleccionado, ejerciciosActualizados)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Subcomponentes de RimuGymScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GymScreenHeader(
    diaSeleccionado: String,
    rutinasCargadas: List<RutinaDia>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Column {
                Text("GYM", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(
                    text  = getNombreRutina(diaSeleccionado, rutinasCargadas),
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, "Configurar", tint = SecondaryText)
        }
    }
}

@Composable
private fun GymDaySelectorRow(
    diaActual: String,
    diaSeleccionado: String,
    rutinasCargadas: List<RutinaDia>,
    onDiaClick: (String) -> Unit
) {
    val dias = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dias.forEach { d ->
            val tieneRutina = rutinasCargadas.any { it.dia == d && it.ejercicios.isNotEmpty() }
            val isSelected  = diaSeleccionado == d
            val isHoy       = diaActual == d

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                isSelected -> Color.White
                                isHoy      -> Color.White.copy(0.12f)
                                else       -> Color.Transparent
                            },
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White else Color.White.copy(0.2f),
                            CircleShape
                        )
                        .clickable { onDiaClick(d) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        d,
                        color      = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
                // Punto indicador si hay rutina
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            if (tieneRutina) Color(0xFF4CAF50) else Color.Transparent,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun GymRoutineContent(
    rutinaSeleccionada: RutinaDia?,
    diaSeleccionado: String,
    entrenandoActivo: Boolean,
    onEliminarEjercicio: (com.example.zenithapp20.data.model.EjercicioGym) -> Unit,
    onComenzar: () -> Unit
) {
    val ejercicios = rutinaSeleccionada?.ejercicios ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Título de la rutina
        Text(
            text  = rutinaSeleccionada?.nombreRutina?.uppercase() ?: "SIN RUTINA",
            color = if (rutinaSeleccionada != null) PrimaryText else SecondaryText,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de ejercicios
        LazyColumn(
            modifier              = Modifier.weight(1f),
            verticalArrangement   = Arrangement.spacedBy(6.dp)
        ) {
            if (ejercicios.isEmpty()) {
                item {
                    Text(
                        "No hay ejercicios configurados.\nToca ⚙ para añadir tu rutina.",
                        color    = SecondaryText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            } else {
                items(ejercicios) { ejercicio ->
                    SwipeToDeleteContainer(
                        mensajeConfirmacion = "Se eliminará '${ejercicio.nombre}' de la rutina.",
                        onDelete            = { onEliminarEjercicio(ejercicio) }
                    ) {
                        GymExercisePreviewItem(ejercicio = ejercicio)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de inicio
        Button(
            onClick  = onComenzar,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor        = Color.White.copy(0.1f),
                disabledContainerColor = Color.White.copy(0.02f)
            ),
            shape   = RoundedCornerShape(16.dp),
            enabled = !entrenandoActivo && ejercicios.isNotEmpty()
        ) {
            Text(
                text = when {
                    entrenandoActivo    -> "ENTRENAMIENTO EN CURSO..."
                    ejercicios.isNotEmpty() -> "COMENZAR ENTRENAMIENTO"
                    else                -> "CONFIGURA TU RUTINA"
                },
                color      = if (ejercicios.isNotEmpty() && !entrenandoActivo) Color.White else Color.Gray,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 16.sp
            )
        }
    }
}

fun getNombreRutina(dia: String, lista: List<RutinaDia>): String {
    val rutina = lista.find { it.dia == dia }
    return if (rutina == null || rutina.nombreRutina.isEmpty()) "Descanso" else rutina.nombreRutina
}