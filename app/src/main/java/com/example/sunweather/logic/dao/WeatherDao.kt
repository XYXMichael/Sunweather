package com.example.sunweather.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sunweather.logic.model.Weather_Forecast

@Dao
interface WeatherDao {
    @Query("SELECT * From weather_forecast")
    fun getWeather(): LiveData<List<Weather_Forecast>>
    @Query("DELETE FROM weather_forecast")
    fun deleteAllWeather()
    @Insert
    fun insertWeather(weatherForecast: Weather_Forecast)
}