package com.example.altin_kaynak.data.api

import android.util.Log
import com.example.altin_kaynak.data.model.GoldPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class AltinkaynakService(private val client: OkHttpClient) {

    private val goldUrl = "https://static.altinkaynak.com/public/Gold"
    private val currencyUrl = "https://static.altinkaynak.com/public/Currency"

    suspend fun getPrices(): List<GoldPrice> = withContext(Dispatchers.IO) {
        val goldDeferred = async { fetch(goldUrl) }
        val currencyDeferred = async { fetch(currencyUrl) }
        disambiguateNames(goldDeferred.await() + currencyDeferred.await())
    }

    // Altınkaynak API'sinde farklı kodlara sahip birden fazla kalem aynı ada
    // sahip olabiliyor (ör. "GA" ve "PGA" ikisi de "Gram Altın"). Kullanıcının
    // bunları karıştırıp yanlışlıkla ikisini de favoriye eklemesini önlemek için
    // tekrar eden adların yanına kodu ekleyerek ayırt edilebilir hale getiriyoruz.
    private fun disambiguateNames(prices: List<GoldPrice>): List<GoldPrice> {
        val nameCounts = prices.groupingBy { it.name }.eachCount()
        return prices.map { price ->
            if ((nameCounts[price.name] ?: 0) > 1) {
                price.copy(name = "${price.name} (${price.symbol})")
            } else {
                price
            }
        }
    }

    private fun fetch(url: String): List<GoldPrice> {
        return try {
            val request = Request.Builder().url(url).get().build()
            val body = client.newCall(request).execute().body?.string() ?: return emptyList()
            parseJson(body)
        } catch (e: Exception) {
            Log.e("AltinkaynakAPI", "Error fetching $url", e)
            emptyList()
        }
    }

    private fun parseJson(json: String): List<GoldPrice> {
        val prices = mutableListOf<GoldPrice>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val kod = obj.optString("Kod", "")
                val aciklama = obj.optString("Aciklama", kod).trim()
                val alis = parseTurkishNumber(obj.optString("Alis", "0"))
                val satis = parseTurkishNumber(obj.optString("Satis", "0"))
                if (kod.isNotEmpty()) {
                    prices.add(GoldPrice(symbol = kod, name = aciklama, buy = alis, sell = satis))
                }
            }
        } catch (e: Exception) {
            Log.e("AltinkaynakAPI", "Parse error", e)
        }
        return prices
    }

    private fun parseTurkishNumber(value: String): Double {
        return value.trim()
            .replace(".", "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0
    }
}
