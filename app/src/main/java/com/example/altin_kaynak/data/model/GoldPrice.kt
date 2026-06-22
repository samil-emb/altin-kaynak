package com.example.altin_kaynak.data.model

data class GoldPrice(
    val symbol: String,
    val name: String,
    val buy: Double,
    val sell: Double,
    val updatedAt: Long = 0L
)
