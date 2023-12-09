package com.example.sunweather.logic

import androidx.lifecycle.liveData
import com.example.sunweather.logic.dao.PlaceDao
import com.example.sunweather.logic.model.Place
import com.example.sunweather.logic.network.MyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {
    fun searchPlaces(location:String) = fire(Dispatchers.IO) {
        val placeResponse = MyWeatherNetwork.searchPlaces(location)
        if(placeResponse.code == "200"){
            val places = placeResponse.location
            Result.success(places)
        }else{
            Result.failure(RuntimeException("response code is ${placeResponse.code}"))
        }
    }
    fun refreshWeather(id:String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredDaily = async {
                MyWeatherNetwork.getDailyWeather(id)
            }
            val dailyResponse = deferredDaily.await()
            if(dailyResponse.code == "200"){
                val weather = dailyResponse.daily
                Result.success(weather)
            }else{
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${dailyResponse.code}"
                    )
                )
            }
        }
    }
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavedPlace() = PlaceDao.getSavedPlace()
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}