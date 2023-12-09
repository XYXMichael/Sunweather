package com.example.sunweather.logic.network

import com.example.sunweather.ui.map.DemoApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object MyWeatherNetwork {
    private val placeService = ServiceCreator.create<PlaceService>()
    private val weatherService = WeatherServiceCreator.create(WeatherService::class.java)
    suspend fun searchPlaces(location: String) = placeService.searchPlaces(location).await()
    suspend fun getDailyWeather(id:String) = weatherService.getDailyWeather(id,DemoApplication.TOKEN).await()
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}