package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.GymViewModel // IMPORTANTE
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuGymScreen(
    navController: NavController,
    viewModel: GymViewModel // 1. Recibimos el ViewModel
) {
    // 2. Observamos las rutinas desde la base de datos
    val rutinasCargadas by viewModel.todasLasRutinas.collectAsState()

    val diaHoy = remember {
        val calendar = Calendar.getInstance()
        val diasMap = mapOf(
            Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
            Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S", Calendar.SUNDAY to "D"
        )
        diasMap[calendar.get(Calendar.DAY_OF_WEEK)] ?: "L"
    }

    var diaSeleccionado by remember { mutableStateOf(diaHoy) }
    var showConfigSheet by remember { mutableStateOf(false) }
    var entrenandoActivo by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // 3. La rutina se busca automáticamente en la lista reactiva de Room
    val rutinaSeleccionada = rutinasCargadas.find { it.dia == diaSeleccionado }

    Column(
        modifier = Modifier.fillMaxSize().background(DeepBackground).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text("GYM PERFORMANCE", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
                .background(MainCardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SIN EXCUSAS", color = PrimaryText, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = "Día de ${getNombreRutina(diaSeleccionado, rutinasCargadas)}",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = { showConfigSheet = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurar", tint = SecondaryText)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ejerciciosHoy = rutinaSeleccionada?.ejercicios ?: emptyList()
                if (ejerciciosHoy.isEmpty()) {
                    item {
                        Text("No hay ejercicios planificados para hoy.", color = SecondaryText, fontSize = 14.sp)
                    }
                } else {
                    items(ejerciciosHoy) { ejercicio ->
                        GymExercisePreviewItem(ejercicio = ejercicio)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val hayEjercicios = rutinaSeleccionada?.ejercicios?.isNotEmpty() == true

            Button(
                onClick = { entrenandoActivo = true },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    disabledContainerColor = Color.White.copy(alpha = 0.02f)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !entrenandoActivo && hayEjercicios
            ) {
                Text(
                    text = if (hayEjercicios) "COMENZAR ENTRENAMIENTO" else "CONFIGURA TU RUTINA",
                    color = if (hayEjercicios) Color.White else Color.Gray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showConfigSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfigSheet = false },
            sheetState = sheetState,
            containerColor = MainCardBackground
        ) {
            GymConfigContent(
                diaActual = diaSeleccionado,
                onDiaSelect = { diaSeleccionado = it },
                onSaveRutina = { nombre, ejercicios ->
                    // 4. Guardamos en la Base de Datos vía ViewModel
                    viewModel.guardarRutina(RutinaDia(dia = diaSeleccionado, nombreRutina = nombre, ejercicios = ejercicios))
                    showConfigSheet = false
                }
            )
        }
    }

    if (entrenandoActivo) {
        ActiveWorkoutOverlay(
            ejercicios = rutinaSeleccionada?.ejercicios ?: emptyList(),
            onFinish = { ejerciciosActualizados ->
                // 5. Persistimos los resultados del entrenamiento (PRs y Logs)
                viewModel.actualizarEjerciciosPostEntreno(diaSeleccionado, ejerciciosActualizados)
                entrenandoActivo = false
            }
        )
    }
}

fun getNombreRutina(dia: String, lista: List<RutinaDia>): String {


    val rutina = lista.find { it.dia == dia }


    return if (rutina == null || rutina.nombreRutina.isEmpty()) "Descanso" else rutina.nombreRutina


}