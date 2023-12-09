package com.example.sunweather.logic.network

import com.example.sunweather.logic.model.PlaceResponse
import com.example.sunweather.ui.map.DemoApplication
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {
    @GET("v2/city/lookup?key=${DemoApplication.TOKEN}")
    fun searchPlaces(@Query("location") location:String):retrofit2.Call<PlaceResponse>
}