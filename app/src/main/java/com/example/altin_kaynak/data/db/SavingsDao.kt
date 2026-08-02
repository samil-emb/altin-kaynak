package com.example.altin_kaynak.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings ORDER BY createdAt DESC")
    fun getAllSavings(): Flow<List<SavingsEntity>>

    @Query("SELECT DISTINCT owner FROM savings ORDER BY owner")
    fun getOwners(): Flow<List<String>>

    @Query("SELECT DISTINCT note FROM savings WHERE note != '' ORDER BY note")
    fun getNotes(): Flow<List<String>>

    @Insert
    suspend fun insert(savings: SavingsEntity)

    @Delete
    suspend fun delete(savings: SavingsEntity)

    @Update
    suspend fun update(savings: SavingsEntity)
}
