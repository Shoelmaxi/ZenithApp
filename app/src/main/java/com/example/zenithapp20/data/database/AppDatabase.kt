package com.example.zenithapp20.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.zenithapp20.data.dao.*
import com.example.zenithapp20.data.utils.Converters
import com.example.zenithapp20.data.model.*

@Database(
    entities = [
        Habito::class,
        Transaccion::class,
        RutinaDia::class,
        AgendaItem::class,
        TareaItem::class,
        AguaRegistro::class,
        Libro::class,
        SesionLectura::class
    ],
    version = 7   // ← Bumped de 6 a 7 (añadimos descripcion en habitos)
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitosDao(): HabitosDao
    abstract fun finanzasDao(): FinanzasDao
    abstract fun gymDao(): GymDao
    abstract fun agendaDao(): AgendaDao
    abstract fun tareasDao(): TareasDao
    abstract fun aguaDao(): AguaDao
    abstract fun libroDao(): LibroDao
    abstract fun sesionLecturaDao(): SesionLecturaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE agenda_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        hora TEXT NOT NULL,
                        dias TEXT NOT NULL,
                        diasCompletados TEXT NOT NULL DEFAULT '[]'
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO agenda_new (id, nombre, descripcion, hora, dias, diasCompletados)
                    SELECT id, nombre, descripcion, hora, dias, '[]' FROM agenda
                """.trimIndent())
                database.execSQL("DROP TABLE agenda")
                database.execSQL("ALTER TABLE agenda_new RENAME TO agenda")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE agenda_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        hora TEXT NOT NULL,
                        tipo TEXT NOT NULL DEFAULT 'RECURRENTE',
                        dias TEXT NOT NULL DEFAULT '[]',
                        fechaEspecificaMillis INTEGER,
                        diasCompletados TEXT NOT NULL DEFAULT '[]'
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO agenda_new (id, nombre, descripcion, hora, tipo, dias, fechaEspecificaMillis, diasCompletados)
                    SELECT id, nombre, descripcion, hora, 'RECURRENTE', dias, NULL, diasCompletados FROM agenda
                """.trimIndent())
                database.execSQL("DROP TABLE agenda")
                database.execSQL("ALTER TABLE agenda_new RENAME TO agenda")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE agua (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fechaMillis INTEGER NOT NULL,
                        cantidadMl INTEGER NOT NULL DEFAULT 250
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE libros (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        titulo TEXT NOT NULL,
                        autor TEXT NOT NULL DEFAULT '',
                        paginasTotales INTEGER NOT NULL,
                        paginaActual INTEGER NOT NULL DEFAULT 0,
                        estado TEXT NOT NULL DEFAULT 'PENDIENTE',
                        categoria TEXT NOT NULL DEFAULT 'OTRO',
                        fechaInicio INTEGER,
                        fechaFin INTEGER
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE sesiones_lectura (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        libroId INTEGER NOT NULL,
                        paginaInicio INTEGER NOT NULL,
                        paginaFin INTEGER NOT NULL,
                        leccionClave TEXT NOT NULL DEFAULT '',
                        aplicacionEstrategica TEXT NOT NULL DEFAULT '',
                        categoria TEXT NOT NULL DEFAULT 'OTRO',
                        sacrificio TEXT NOT NULL DEFAULT '',
                        esMinimoCumplido INTEGER NOT NULL DEFAULT 0,
                        fecha INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // No-op: cubre el gap entre v5 y v6 en caso de que alguien venga de v5
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Sin cambios estructurales en este tramo
            }
        }

        // v7: agrega la columna descripcion a la tabla habitos
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE habitos ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}