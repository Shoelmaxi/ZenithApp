package com.example.zenithapp20.data.dao

import androidx.room.*
import com.example.zenithapp20.data.model.Habito
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitosDao {

    @Query("SELECT * FROM habitos")
    fun getAllHabitos(): Flow<List<Habito>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabito(habito: Habito)

    @Update
    suspend fun updateHabito(habito: Habito)

    @Delete
    suspend fun deleteHabito(habito: Habito)

    @Query("SELECT * FROM habitos WHERE id = :id")
    suspend fun getHabitoById(id: Long): Habito?
}