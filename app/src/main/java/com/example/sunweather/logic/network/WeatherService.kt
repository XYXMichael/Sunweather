package com.example.sunweather.logic.network

import com.example.sunweather.logic.model.DailyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("v7/weather/7d?")
    fun getDailyWeather(@Query("location") id: String, @Query("key") key:String): Call<DailyResponse>
}