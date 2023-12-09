package com.example.sunweather.logic.model

import com.example.sunweather.R

class Sky(val info:String,val icon:Int,val bg:Int)
private val sky = mapOf(
    "晴" to Sky("晴", R.drawable.ic_sun, R.drawable.bg_clear_day),
//        "CLEAR_NIGHT" to Sky("晴", R.drawable.ic_clear_night, R.drawable.bg_clear_night),
    "多云" to Sky("多云", R.drawable.ic_partly_cloud_day,
        R.drawable.bg_partly_cloudy_day),
//        "PARTLY_CLOUDY_NIGHT" to Sky("多云", R.drawable.ic_partly_cloud_night,
//            R.drawable.bg_partly_cloudy_night),
    "阵雨" to Sky("阵雨",R.drawable.shower,R.drawable.bg_rain),
    "阴" to Sky("阴", R.drawable.ic_cloudy, R.drawable.bg_cloudy),
    "大风" to Sky("大风", R.drawable.ic_cloudy, R.drawable.bg_wind),
    "小雨" to Sky("小雨", R.drawable.ic_light_rain, R.drawable.bg_rain),
    "中雨" to Sky("中雨", R.drawable.ic_moderate_rain, R.drawable.bg_rain),
    "大雨" to Sky("大雨", R.drawable.ic_heavy_rain, R.drawable.bg_rain),
    "暴雨" to Sky("暴雨", R.drawable.ic_storm_rain, R.drawable.bg_rain),
    "雷阵雨" to Sky("雷阵雨", R.drawable.ic_thunder_shower, R.drawable.bg_rain),
    "雨夹雪" to Sky("雨夹雪", R.drawable.ic_sleet, R.drawable.bg_rain),
    "小雪" to Sky("小雪", R.drawable.ic_light_snow, R.drawable.bg_snow),
    "中雪" to Sky("中雪", R.drawable.ic_moderate_snow, R.drawable.bg_snow),
    "大雪" to Sky("大雪", R.drawable.ic_heavy_snow, R.drawable.bg_snow),
    "暴雪" to Sky("暴雪", R.drawable.ic_heavy_snow, R.drawable.bg_snow),
    "冰雹" to Sky("冰雹", R.drawable.ic_hail, R.drawable.bg_snow),
    "轻度雾霾" to Sky("轻度雾霾", R.drawable.ic_light_haze, R.drawable.bg_fog),
    "中度雾霾" to Sky("中度雾霾", R.drawable.ic_moderate_haze, R.drawable.bg_fog),
    "重度雾霾" to Sky("重度雾霾", R.drawable.ic_heavy_haze, R.drawable.bg_fog),
    "雾" to Sky("雾", R.drawable.ic_fog, R.drawable.bg_fog),
    "浮尘" to Sky("浮尘", R.drawable.ic_fog, R.drawable.bg_fog)
)
fun getSky(skycon: String): Sky {
    return sky[skycon] ?: sky["CLEAR_DAY"]!!
}