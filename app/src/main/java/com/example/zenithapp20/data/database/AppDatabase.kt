package com.example.zenithapp20.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.zenithapp20.data.dao.*
import com.example.zenithapp20.data.model.*
import com.example.zenithapp20.data.utils.Converters
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.Transaccion
import com.example.zenithapp20.data.model.RutinaDia
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.TareaItem

@Database(
    entities = [
        Habito::class,
        Transaccion::class,
        RutinaDia::class,
        AgendaItem::class, // Asegúrate de incluir las entidades aquí
        TareaItem::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitosDao(): HabitosDao
    abstract fun finanzasDao(): FinanzasDao
    abstract fun gymDao(): GymDao

    // ESTAS SON LAS QUE TE ESTÁ PIDIENDO EL FACTORY:
    abstract fun agendaDao(): AgendaDao
    abstract fun tareasDao(): TareasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}