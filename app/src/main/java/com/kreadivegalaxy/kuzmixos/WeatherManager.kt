package com.kreadivegalaxy.kuzmixos

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherManager(private val context: Context) {

    data class WeatherData(
        val temperature: String = "N/A",
        val condition: String = "Unknown",
        val humidity: String = "N/A",
        val windSpeed: String = "N/A",
        val location: String = "Unknown"
    )

    suspend fun fetchCurrentWeather(): WeatherData {
        return try {
            val url = URL("https://api.duckduckgo.com/?q=current+weather+now&format=json&no_html=1&skip_disambig=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val abstract = json.optString("AbstractText", "")
                val heading = json.optString("Heading", "")

                WeatherData(
                    temperature = extractTemperature(abstract),
                    condition = heading.ifEmpty { "Unknown" },
                    location = "Current Location"
                )
            } else {
                WeatherData()
            }
        } catch (e: Exception) {
            android.util.Log.w("WeatherManager", "Failed to fetch weather: ${e.message}")
            WeatherData()
        }
    }

    private fun extractTemperature(text: String): String {
        val regex = Regex("(\\d+)\\s*°\\s*([CF])")
        val match = regex.find(text)
        return if (match != null) {
            "${match.groupValues[1]}°${match.groupValues[2]}"
        } else {
            "N/A"
        }
    }
}
