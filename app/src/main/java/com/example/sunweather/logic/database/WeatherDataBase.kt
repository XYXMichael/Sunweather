package com.example.sunweather.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sunweather.logic.dao.WeatherDao
import com.example.sunweather.logic.model.Weather_Forecast

@Database(entities = [Weather_Forecast::class], version = 2, exportSchema = false)
abstract class WeatherDataBase : RoomDatabase(){
    abstract fun weatherdao(): WeatherDao
}