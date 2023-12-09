package com.example.sunweather.logic.model

import com.google.gson.annotations.SerializedName

data class PlaceResponse (val code:String,val location:List<Place>)
data class Place(@SerializedName("name")val name: String,
                 @SerializedName("country") val country: String,
                 @SerializedName("adm1") val province: String,
                 @SerializedName("adm2") val city: String,
                 @SerializedName("id") val id: String)