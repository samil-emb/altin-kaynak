package com.example.altin_kaynak.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header

data class HaremPriceItem(
    val symbol: String = "",
    val category: String = "",
    val description: String = "",
    val bid: Double = 0.0,
    val ask: Double = 0.0,
    val timestamp: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class HaremPricesResponse(
    val data: List<HaremPriceItem> = emptyList()
)

interface HaremApiService {
    @GET("prices")
    suspend fun getAllPrices(
        @Header("X-API-Key") apiKey: String
    ): HaremPricesResponse
}
