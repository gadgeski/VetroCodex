package com.gadgeski.vetro.data

import com.gadgeski.vetro.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data Models
data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val name: String
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

// Retrofit Service
interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ja"
    ): WeatherResponse
}

// Repository Interface
interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherResponse>
}

// Repository Implementation
class WeatherRepositoryImpl : WeatherRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(WeatherApiService::class.java)

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = service.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.OPEN_WEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
