package com.example.sunweather.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Weather_Forecast(@PrimaryKey val id: UUID = UUID.randomUUID(),
                            val place_name:String,
                            val date:String,
                            val maxTemp:String,
                            val minTemp:String,
                            val humidity:String,
                            val pressure:String,
                            val textDay:String,
                            val textNight:String,
                            val sunrise:String,
                            val sunset:String,
                            val moonrise:String,
                            val moonset:String)