package com.example.zenithapp20.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.ui.screen.RimuBackupScreen
import com.example.zenithapp20.ui.screen.RimuFinanceScreen
import com.example.zenithapp20.ui.screen.RimuGymScreen
import com.example.zenithapp20.ui.screen.RimuHabitsStatsScreen
import com.example.zenithapp20.ui.screen.RimuScreen
import com.example.zenithapp20.ui.screen.RimuSummaryScreen
import com.example.zenithapp20.ui.screen.RimuWeekScreen
import com.example.zenithapp20.ui.viewmodel.AgendaViewModel
import com.example.zenithapp20.ui.viewmodel.AguaViewModel
import com.example.zenithapp20.ui.viewmodel.FinanzasViewModel
import com.example.zenithapp20.ui.viewmodel.GymViewModel
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel
import com.example.zenithapp20.ui.viewmodel.TareasViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 1. Inicializamos la base de datos y la fábrica de ViewModels
    val database = remember { AppDatabase.getDatabase(context) }
    val factory = remember { AppViewModelFactory(database, context) }

    NavHost(
        navController = navController,
        startDestination = "rimu_screen",
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        // --- PANTALLA PRINCIPAL (HÁBITOS) ---
        composable("rimu_screen") {
            val habitosVM: HabitosViewModel = viewModel(factory = factory)
            val agendaVM: AgendaViewModel = viewModel(factory = factory)
            val tareasVM: TareasViewModel = viewModel(factory = factory)

            RimuScreen(
                navController = navController,
                habitosViewModel = habitosVM, // Asegúrate de usar los nombres de parámetros correctos
                agendaViewModel = agendaVM,
                tareasViewModel = tareasVM
            )
        }

        // --- PANTALLA DE ESTADÍSTICAS (HÁBITOS) ---
        composable("rimu_habits_stats") {
            // Usamos el mismo ViewModel para que los datos sean consistentes
            val habitosVM: HabitosViewModel = viewModel(factory = factory)
            RimuHabitsStatsScreen(navController = navController, habitosViewModel = habitosVM)
        }

        // --- PANTALLA DE GYM ---
        composable("rimu_gym") {
            val gymVM: GymViewModel = viewModel(factory = factory)
            RimuGymScreen(navController = navController, viewModel = gymVM)
        }

        // --- PANTALLA DE FINANZAS ---
        composable("rimu_finance") {
            val finanzasVM: FinanzasViewModel = viewModel(factory = factory)
            RimuFinanceScreen(navController = navController, viewModel = finanzasVM)
        }
        composable("rimu_week") {
            val agendaVM: AgendaViewModel = viewModel(factory = factory)
            val habitosVM: HabitosViewModel = viewModel(factory = factory)
            RimuWeekScreen(
                navController = navController,
                agendaViewModel = agendaVM,
                habitosViewModel = habitosVM
            )
        }
        composable("rimu_backup") {
            RimuBackupScreen(navController = navController)
        }

        composable("rimu_summary") {
            val habitosVM: HabitosViewModel = viewModel(factory = factory)
            val agendaVM: AgendaViewModel = viewModel(factory = factory)
            val finanzasVM: FinanzasViewModel = viewModel(factory = factory)
            val aguaVM: AguaViewModel = viewModel(factory = factory)
            RimuSummaryScreen(
                navController = navController,
                habitosViewModel = habitosVM,
                agendaViewModel = agendaVM,
                finanzasViewModel = finanzasVM,
                aguaViewModel = aguaVM
            )
        }
    }
}