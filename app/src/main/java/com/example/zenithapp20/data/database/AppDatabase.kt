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
import com.example.zenithapp20.data.model.TareaItem

@Database(
    entities = [
        Habito::class,
        Transaccion::class,
        RutinaDia::class,
        AgendaItem::class, // Asegúrate de incluir las entidades aquí
        TareaItem::class
    ],
    version = 2
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
                val MIGRATION_1_2 = object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        // 1. crear tabla temporal con la estructura nueva
                        database.execSQL("""
            CREATE TABLE agenda_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                hora TEXT NOT NULL,
                dias TEXT NOT NULL,
                diasCompletados TEXT NOT NULL DEFAULT '[]'
            )
        """)
                        // 2. copiar datos existentes
                        database.execSQL("""
            INSERT INTO agenda_new (id, nombre, descripcion, hora, dias, diasCompletados)
            SELECT id, nombre, descripcion, hora, dias, '[]'
            FROM agenda
        """)
                        // 3. borrar tabla vieja
                        database.execSQL("DROP TABLE agenda")
                        // 4. renombrar la nueva
                        database.execSQL("ALTER TABLE agenda_new RENAME TO agenda")
                    }
                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        }
}
