package com.kreadivegalaxy.kuzmixos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherData(
    val tempC: Float = 25.0f,
    val tempF: Float = 77.0f,
    val isCelsius: Boolean = true,
    val condition: String = "Mostly Cloudy",
    val weatherCode: Int = 2,
    val humidity: Int = 65,
    val windSpeedKmH: Float = 12.0f,
    val cityName: String = "Lagos, Nigeria",
    val lat: Double = 6.5244,
    val lon: Double = 3.3792,
    val isFetching: Boolean = false,
    val lastUpdated: String = "12:00 PM",
    val errorMessage: String? = null
) {
    val displayTemp: String
        get() = if (isCelsius) "${tempC.toInt()}°C" else "${tempF.toInt()}°F"
}

class WeatherManager(private val context: Context) {

    private val _weatherState = MutableStateFlow(
        WeatherData(
            tempC = 25.0f,
            tempF = 77.0f,
            condition = "Mostly Cloudy",
            weatherCode = 2,
            humidity = 65,
            windSpeedKmH = 12.0f,
            cityName = "Lagos, Nigeria"
        )
    )
    val weatherState: StateFlow<WeatherData> = _weatherState.asStateFlow()

    fun toggleTempUnit() {
        _weatherState.value = _weatherState.value.copy(
            isCelsius = !_weatherState.value.isCelsius
        )
    }

    suspend fun fetchCurrentWeather() {
        // Safe mock stub without blocking network or location queries
        withContext(Dispatchers.IO) {
            kotlinx.coroutines.delay(100)
            _weatherState.value = _weatherState.value.copy(
                isFetching = false,
                tempC = 25.0f,
                tempF = 77.0f,
                condition = "Mostly Cloudy",
                cityName = "Lagos, Nigeria",
                humidity = 65,
                windSpeedKmH = 12.0f
            )
        }
    }

    private fun mapWeatherCodeToCondition(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Partly Cloudy"
        }
    }
}
