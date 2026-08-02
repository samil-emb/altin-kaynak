package com.example.altin_kaynak.data.db

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["source", "symbol"])
data class FavoriteEntity(
    val source: String,
    val symbol: String
)
