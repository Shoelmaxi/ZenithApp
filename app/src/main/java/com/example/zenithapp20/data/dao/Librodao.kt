package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.Libro
import kotlinx.coroutines.flow.Flow

@Dao
interface LibroDao {

    // Orden: Leyendo primero, luego Pendiente, Terminado, Abandonado
    @Query("""
        SELECT * FROM libros
        ORDER BY
            CASE estado
                WHEN 'LEYENDO'    THEN 0
                WHEN 'PENDIENTE'  THEN 1
                WHEN 'TERMINADO'  THEN 2
                WHEN 'ABANDONADO' THEN 3
            END ASC,
            id DESC
    """)
    fun getAllLibros(): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE id = :id LIMIT 1")
    fun getLibroById(id: Long): Flow<Libro?>

    @Query("SELECT * FROM libros WHERE id = :id LIMIT 1")
    suspend fun getLibroByIdSync(id: Long): Libro?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibro(libro: Libro)

    @Update
    suspend fun updateLibro(libro: Libro)

    @Delete
    suspend fun deleteLibro(libro: Libro)
}