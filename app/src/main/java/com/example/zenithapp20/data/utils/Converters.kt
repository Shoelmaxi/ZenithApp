package com.example.zenithapp20.data.utils

import androidx.room.TypeConverter
import com.example.zenithapp20.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- PARA ENUMS (Prioridad y TipoTransaccion) ---
    @TypeConverter
    fun fromPrioridad(prioridad: Prioridad): String = prioridad.name

    @TypeConverter
    fun toPrioridad(value: String): Prioridad = Prioridad.valueOf(value)

    @TypeConverter
    fun fromTipoTransaccion(tipo: TipoTransaccion): String = tipo.name

    @TypeConverter
    fun toTipoTransaccion(value: String): TipoTransaccion = TipoTransaccion.valueOf(value)

    // --- PARA LISTAS SIMPLES (Agenda y Habitos) ---
    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String = gson.toJson(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }

    // --- PARA EL GYM (Estructura compleja de Ejercicios) ---
    @TypeConverter
    fun fromEjercicioList(value: List<EjercicioGym>): String = gson.toJson(value)

    @TypeConverter
    fun toEjercicioList(value: String): List<EjercicioGym> {
        val listType = object : TypeToken<List<EjercicioGym>>() {}.type
        return gson.fromJson(value, listType)
    }
}