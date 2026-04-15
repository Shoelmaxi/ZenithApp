package com.example.zenithapp20.data.utils

import androidx.room.TypeConverter
import com.example.zenithapp20.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- ENUMS EXISTENTES ---
    @TypeConverter fun fromPrioridad(v: Prioridad): String = v.name
    @TypeConverter fun toPrioridad(v: String): Prioridad = Prioridad.valueOf(v)

    @TypeConverter fun fromTipoTransaccion(v: TipoTransaccion): String = v.name
    @TypeConverter fun toTipoTransaccion(v: String): TipoTransaccion = TipoTransaccion.valueOf(v)

    @TypeConverter fun fromTipoAgenda(v: TipoAgenda): String = v.name
    @TypeConverter fun toTipoAgenda(v: String): TipoAgenda = TipoAgenda.valueOf(v)

    // --- ENUMS NUEVOS (Lectura) ---
    @TypeConverter fun fromEstadoLibro(v: EstadoLibro): String = v.name
    @TypeConverter fun toEstadoLibro(v: String): EstadoLibro = EstadoLibro.valueOf(v)

    @TypeConverter fun fromCategoriaLibro(v: CategoriaLibro): String = v.name
    @TypeConverter fun toCategoriaLibro(v: String): CategoriaLibro = CategoriaLibro.valueOf(v)

    // --- LISTAS SIMPLES ---
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

    // --- GYM ---
    @TypeConverter
    fun fromEjercicioList(value: List<EjercicioGym>): String = gson.toJson(value)

    @TypeConverter
    fun toEjercicioList(value: String): List<EjercicioGym> {
        val listType = object : TypeToken<List<EjercicioGym>>() {}.type
        return gson.fromJson(value, listType)
    }
}