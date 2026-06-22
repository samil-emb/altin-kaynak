package com.example.altin_kaynak.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings ORDER BY createdAt DESC")
    fun getAllSavings(): Flow<List<SavingsEntity>>

    @Insert
    suspend fun insert(savings: SavingsEntity)

    @Delete
    suspend fun delete(savings: SavingsEntity)

    @Update
    suspend fun update(savings: SavingsEntity)
}
