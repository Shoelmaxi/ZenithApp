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
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.Transaccion
import com.example.zenithapp20.data.model.RutinaDia
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.AguaRegistro
import com.example.zenithapp20.data.model.TareaItem

@Database(
    entities = [
        Habito::class,
        Transaccion::class,
        RutinaDia::class,
        AgendaItem::class,
        TareaItem::class,
        AguaRegistro::class
    ],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitosDao(): HabitosDao
    abstract fun finanzasDao(): FinanzasDao
    abstract fun gymDao(): GymDao
    abstract fun agendaDao(): AgendaDao
    abstract fun tareasDao(): TareasDao

    abstract fun aguaDao(): AguaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration definida aquí afuera para que sea reutilizable (ej: en tests)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Crear tabla temporal con la estructura nueva
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

                // 2. Copiar datos existentes
                database.execSQL("""
                    INSERT INTO agenda_new (id, nombre, descripcion, hora, dias, diasCompletados)
                    SELECT id, nombre, descripcion, hora, dias, '[]'
                    FROM agenda
                """.trimIndent())

                // 3. Borrar tabla vieja
                database.execSQL("DROP TABLE agenda")

                // 4. Renombrar la nueva
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
            SELECT id, nombre, descripcion, hora, 'RECURRENTE', dias, NULL, diasCompletados
            FROM agenda
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Doble check dentro del bloque sincronizado
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}