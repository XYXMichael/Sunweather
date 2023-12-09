package com.example.sunweather.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunweather.logic.Repository

class WeatherViewModel : ViewModel(){
    private val dailyLiveData = MutableLiveData<String>()
    var id = ""
    var place_name=""
    val weatherLivaData = Transformations.switchMap(dailyLiveData){ id->
        Repository.refreshWeather(id)
    }
    fun refreshWeather(id:String){
        dailyLiveData.value = id
    }

}