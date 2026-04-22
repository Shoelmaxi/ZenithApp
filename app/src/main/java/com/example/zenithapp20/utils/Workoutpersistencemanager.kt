package com.example.zenithapp20.utils

import android.content.Context
import com.example.zenithapp20.ui.viewmodel.WorkoutState
import com.google.gson.Gson

object WorkoutPersistenceManager {

    private const val PREFS_NAME = "zenith_workout_session"
    private const val KEY_STATE  = "active_workout_state"

    private val gson = Gson()

    /** Persiste el estado actual del entrenamiento. Llamar en cada cambio. */
    fun save(context: Context, state: WorkoutState) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STATE, gson.toJson(state))
            .apply()
    }

    /**
     * Carga el último estado guardado.
     * Retorna null si no hay sesión activa o si el JSON es inválido.
     */
    fun load(context: Context): WorkoutState? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_STATE, null) ?: return null
        return try {
            gson.fromJson(json, WorkoutState::class.java)
                ?.takeIf { it.ejerciciosFinales.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    /** Borra el estado al terminar o descartar un entrenamiento. */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_STATE)
            .apply()
    }

    /** Retorna true si hay una sesión guardada sin terminar. */
    fun hasSavedSession(context: Context): Boolean = load(context) != null
}