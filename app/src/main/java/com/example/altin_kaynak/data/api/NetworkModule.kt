package com.example.altin_kaynak.data.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    const val HAREM_API_KEY = "hapi_1bcd35c7e91f4d209c31d811dcb1f772"
    private const val HAREM_BASE_URL = "https://altinapi.com/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HaremAPI", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val haremApiService: HaremApiService = Retrofit.Builder()
        .baseUrl(HAREM_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HaremApiService::class.java)

    val altinkaynakService: AltinkaynakService = AltinkaynakService(httpClient)
}
