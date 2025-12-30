package com.gadgeski.vetro.data

import com.gadgeski.vetro.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
// 追加: JSONキーのマッピング用(com.squareup.moshi.Json)

// --- Data Models ---

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
    // 【修正】警告対応: アノテーションのターゲットをパラメータ(@param)に明示的に指定
    // これにより "annotation is currently applied to the value parameter only" の警告が解消されます
    @param:Json(name = "feels_like") val feelsLike: Double,
    val humidity: Int
)

// --- Retrofit Service ---

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

// --- Repository Interface ---

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherResponse>
}

// --- Repository Implementation ---

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
            // 【修正】エラー対応: APIキーの定義
            // build.gradle.kts で buildConfigField を設定していない場合、
            // BuildConfig.OPEN_WEATHER_API_KEY は存在しないためエラーになります。
            // 暫定的にここに直接キーを記述するか、後でgradle設定を追加してください。

            // ★ここにOpenWeatherMapのAPIキーを入れてください
            // build.gradle.kts の設定により BuildConfig から参照可能になりました
            val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

            // もしgradle設定済みなら以下のように書けます
            // val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

            val response = service.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = apiKey
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}