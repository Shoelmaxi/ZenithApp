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
import com.example.zenithapp20.ui.screen.*
import com.example.zenithapp20.ui.viewmodel.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

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
        composable("rimu_screen") {
            val habitosVM: HabitosViewModel = viewModel(factory = factory)
            val agendaVM: AgendaViewModel = viewModel(factory = factory)
            val tareasVM: TareasViewModel = viewModel(factory = factory)
            val icVM: IngenieriaConductualViewModel = viewModel(factory = factory)  // NUEVO
            RimuScreen(
                navController = navController,
                habitosViewModel = habitosVM,
                agendaViewModel = agendaVM,
                tareasViewModel = tareasVM,
                icViewModel = icVM  // NUEVO
            )
        }

        composable("rimu_habits_stats") {
            val habitosVM: HabitosViewModel = viewModel(factory = factory)
            RimuHabitsStatsScreen(navController = navController, habitosViewModel = habitosVM)
        }

        composable("rimu_gym") {
            val gymVM: GymViewModel = viewModel(factory = factory)
            RimuGymScreen(navController = navController, viewModel = gymVM)
        }

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

        // ── LECTURA ──────────────────────────────────────────────────────────
        composable("rimu_lectura") {
            val lecturaVM: LecturaViewModel = viewModel(factory = factory)
            RimuLecturaScreen(navController = navController, viewModel = lecturaVM)
        }

        composable("rimu_lectura_detail/{libroId}") { backStackEntry ->
            val libroId = backStackEntry.arguments?.getString("libroId")?.toLongOrNull() ?: return@composable
            val lecturaVM: LecturaViewModel = viewModel(factory = factory)
            RimuLibroDetailScreen(
                navController = navController,
                libroId = libroId,
                viewModel = lecturaVM
            )
        }

        composable("rimu_sistema") {
            val icVM: IngenieriaConductualViewModel = viewModel(factory = factory)
            RimuSistemaScreen(navController = navController, viewModel = icVM)
        }
        composable("rimu_deep_work") {
            val icVM: IngenieriaConductualViewModel = viewModel(factory = factory)
            RimuDeepWorkScreen(navController = navController, viewModel = icVM)
        }
        composable("rimu_resiliencia") {
            val icVM: IngenieriaConductualViewModel = viewModel(factory = factory)
            RimuResilienciaScreen(navController = navController, viewModel = icVM)
        }
        composable("rimu_reflexion") {
            val icVM: IngenieriaConductualViewModel = viewModel(factory = factory)
            RimuReflexionScreen(navController = navController, viewModel = icVM)
        }
        composable("rimu_sueno") {
            val icVM: IngenieriaConductualViewModel = viewModel(factory = factory)
            RimuSuenoScreen(navController = navController, viewModel = icVM)
        }
    }
}