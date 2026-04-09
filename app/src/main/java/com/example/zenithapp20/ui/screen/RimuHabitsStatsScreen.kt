package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // IMPORTANTE
import androidx.compose.runtime.getValue     // IMPORTANTE
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.components.CardRadarDisciplina
import com.example.zenithapp20.ui.components.SeguimientoSemanalHabitos
import com.example.zenithapp20.ui.components.CardActividadDiaria
import com.example.zenithapp20.ui.theme.DeepBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel // Importa tu ViewModel

@Composable
fun RimuHabitsStatsScreen(
    navController: NavController,
    habitosViewModel: HabitosViewModel // Cambiamos la lista por el ViewModel
) {
    // Observamos los hábitos desde la base de datos
    // Cada vez que marques un check en la pantalla principal,
    // esta lista se actualizará sola y los gráficos se redibujarán.
    val listaHabitos by habitosViewModel.habitos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Cabecera de Navegación
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text(
                text = "RENDIMIENTO",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Si no hay hábitos, podrías mostrar un estado vacío,
        // pero LazyColumn maneja bien una lista vacía por defecto.
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Gráfico Radar: Equilibrio de categorías
            item {
                CardRadarDisciplina(listaHabitos = listaHabitos)
            }

            // 2. Tabla de Seguimiento: Estrellas de Semana Perfecta
            item {
                SeguimientoSemanalHabitos(listaHabitos = listaHabitos)
            }

            // 3. Resumen de Actividad Diaria: Tendencia últimos 7 días
            item {
                CardActividadDiaria(listaHabitos = listaHabitos)
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}