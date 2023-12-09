package com.example.sunweather.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer

class DemoApplication:Application() {
    companion object{
        const val TOKEN = "37b9da749cbf4c12a668c79f4c8fe68f"
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        SDKInitializer.setAgreePrivacy(this, true);
        SDKInitializer.initialize(this);
        LocationClient.setAgreePrivacy(true)
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }
}