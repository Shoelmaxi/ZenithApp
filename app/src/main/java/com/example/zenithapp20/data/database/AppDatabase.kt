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
        Habito::class, Transaccion::class, RutinaDia::class,
        AgendaItem::class, TareaItem::class, AguaRegistro::class,
        Libro::class, SesionLectura::class,
        AnalisisHabito::class, SesionDeepWork::class,
        RegistroResiliencia::class, ReflexionDiaria::class
    ],
    version = 9   // ← bumped from 8
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
    abstract fun analisisHabitoDao(): AnalisisHabitoDao
    abstract fun deepWorkDao(): DeepWorkDao
    abstract fun resilienciaDao(): ResilienciaDao
    abstract fun reflexionDao(): ReflexionDao

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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) { }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE habitos ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE analisis_habito (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitoId INTEGER NOT NULL,
                        habitoNombre TEXT NOT NULL,
                        focusLevel INTEGER NOT NULL,
                        frictionFactor TEXT NOT NULL,
                        adjustmentNote TEXT NOT NULL DEFAULT '',
                        fechaMillis INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE sesiones_deep_work (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        duracionObjetivoMin INTEGER NOT NULL,
                        duracionRealSegundos INTEGER NOT NULL,
                        distracciones INTEGER NOT NULL,
                        calidadSesion REAL NOT NULL,
                        fechaMillis INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE registros_resiliencia (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fechaDia INTEGER NOT NULL,
                        duchaFria INTEGER NOT NULL DEFAULT 0,
                        ayunoDopamina INTEGER NOT NULL DEFAULT 0,
                        entrenamientoResistencia INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE reflexiones_diarias (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fechaMillis INTEGER NOT NULL,
                        movimientoMaestro TEXT NOT NULL,
                        puntoCiego TEXT NOT NULL,
                        aperturaMañana TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // ── NUEVA MIGRACIÓN ────────────────────────────────────────────────
        // Agrega los campos 'completado' y 'razonNoCompletado' a analisis_habito.
        // Los registros existentes asumen completado = 1 (true) por defecto.
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE analisis_habito ADD COLUMN completado INTEGER NOT NULL DEFAULT 1"
                )
                database.execSQL(
                    "ALTER TABLE analisis_habito ADD COLUMN razonNoCompletado TEXT NOT NULL DEFAULT ''"
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
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}