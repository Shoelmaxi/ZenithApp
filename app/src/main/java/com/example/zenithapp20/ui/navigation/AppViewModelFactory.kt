package com.example.zenithapp20.ui.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.ui.viewmodel.AgendaViewModel
import com.example.zenithapp20.ui.viewmodel.AguaViewModel
import com.example.zenithapp20.ui.viewmodel.FinanzasViewModel
import com.example.zenithapp20.ui.viewmodel.GymViewModel
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel
import com.example.zenithapp20.ui.viewmodel.TareasViewModel

class AppViewModelFactory(
    private val db: AppDatabase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HabitosViewModel::class.java) ->
                HabitosViewModel(db.habitosDao(), context) as T
            modelClass.isAssignableFrom(FinanzasViewModel::class.java) ->
                FinanzasViewModel(db.finanzasDao()) as T
            modelClass.isAssignableFrom(GymViewModel::class.java) ->
                GymViewModel(db.gymDao()) as T
            // --- ESTO ES LO QUE FALTABA ---
            modelClass.isAssignableFrom(AgendaViewModel::class.java) ->
                AgendaViewModel(db.agendaDao()) as T
            modelClass.isAssignableFrom(TareasViewModel::class.java) ->
                TareasViewModel(db.tareasDao()) as T
            modelClass.isAssignableFrom(AguaViewModel::class.java) ->
                AguaViewModel(db.aguaDao(), db.habitosDao()) as T
            // ------------------------------
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}