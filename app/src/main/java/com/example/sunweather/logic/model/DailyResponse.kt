package com.example.sunweather.logic.model

import com.google.gson.annotations.SerializedName

data class DailyResponse(val code:String,val daily:List<Daily>){
    val dailys = daily
    data class Daily(@SerializedName("fxDate")val date:String,
                     @SerializedName("tempMax")val maxTemp:String,
                     @SerializedName("tempMin")val minTemp:String,
                     @SerializedName("humidity")val humidity:String,
                     @SerializedName("pressure")val pressure:String,
                     @SerializedName("textDay")val textDay:String,
                     @SerializedName("textNight")val textNight:String,
                     @SerializedName("sunrise")val sunrise:String,
                     @SerializedName("sunset")val sunset:String,
                     @SerializedName("moonrise")val moonrise:String,
                     @SerializedName("moonset")val moonset:String)
}