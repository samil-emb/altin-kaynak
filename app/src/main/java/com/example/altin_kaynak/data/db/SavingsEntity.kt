package com.example.altin_kaynak.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings")
data class SavingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val owner: String,
    val type: String,
    val quantity: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
