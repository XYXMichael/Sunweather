package com.example.sunweather.ui.weather

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sunweather.R
import com.example.sunweather.logic.database.WeatherDataBase
import com.example.sunweather.logic.model.DailyResponse
import com.example.sunweather.logic.model.Translate
import com.example.sunweather.logic.model.Weather_Forecast
import com.example.sunweather.logic.model.getSky
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.absoluteValue

class WeatherActivity : AppCompatActivity() {
    //定义广播机制，收取地理位置信息
    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.ACTION_MESSAGE") { // 自定义广播的Action
                val message = intent.getStringExtra("message") // 获取传递的信息
                Log.d("WeatherActivity","$message")
                findViewById<EditText>(R.id.searchPlaceEdit).setText(message)
            }
        }
    }
    private var isTable:Int = 0
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        //定义导航栏，设置主页图标
        setSupportActionBar(findViewById(R.id.toolbar1))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        // 设置自定义返回图标
        val backIcon = resources.getDrawable(R.drawable.ic_home)
        // 设置返回图标的大小
        backIcon.setBounds(5,5,5,5)
        actionBar?.setHomeAsUpIndicator(backIcon)
        //获取传递的地理位置信息，执行ViewModel中API获取天气
        if(viewModel.id.isEmpty()){
            viewModel.id = intent.getStringExtra("id")?:""
            viewModel.place_name = intent.getStringExtra("place_name")?:""
        }
        //初始化数据库，从数据库中获取响应数据进行渲染
        val db= Room.databaseBuilder(applicationContext, WeatherDataBase::class.java,"database").fallbackToDestructiveMigration().build()
        val dao = db.weatherdao()
        refreshWeather()
        //判断是否为平板
        if(findViewById<FrameLayout>(R.id.weatherDetailFragment)!=null){
            isTable=1
        }
        //实时观察获取到的数据，并进行界面渲染
        viewModel.weatherLivaData.observe(this, Observer { result ->
            val weathers = result.getOrNull()
            if(weathers != null){
                Log.d("WeatherActivity","66$weathers")
                //使用协程异步将收集的天气信息存取数据库
                viewModel.viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.deleteAllWeather()
                        for (weather in weathers) {
                            val weatherForecast = Weather_Forecast(
                                UUID.randomUUID(),
                                viewModel.place_name,
                                weather.date,
                                weather.maxTemp,
                                weather.minTemp,
                                weather.humidity,
                                weather.pressure,
                                weather.textDay,
                                weather.textNight,
                                weather.sunrise,
                                weather.sunset,
                                weather.moonrise,
                                weather.moonset
                            )
                            dao.insertWeather(weatherForecast)
                        }
                    }
                    dao.getWeather().observe(this@WeatherActivity, Observer { weatherList ->
                        Log.d("WeatherActivity", "${weatherList}")
                        showWeatherInfo(weathers)
                    })

                }
            }else{
                dao.getWeather().observe(this@WeatherActivity, Observer { weatherList ->
                    Log.d("WeatherActivity", "111${weatherList}")
                    val list = mutableListOf<DailyResponse.Daily>()
                    for(weather in weatherList){
                        val daily = DailyResponse.Daily(weather.date,weather.maxTemp,weather.minTemp,weather.humidity,weather.pressure,weather.textDay,weather.textNight,weather.sunrise,weather.sunset,weather.moonrise,weather.moonset)
                        list.add(daily)
                    }
                    showWeatherInfo(list)
                })
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).isRefreshing = false
        })
        //刷新功能，并设置刷新是否发送消息通知
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).setColorSchemeResources(R.color.colorPrimary)
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).setOnRefreshListener {
            refreshWeather()
            // 用户打开开关，发送通知消息
            if(Translate.notice == 1){
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // 创建通知渠道（适用于 Android 8.0 及以上版本）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }
                // 创建通知
                val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("SunWeather")
                    .setContentText("您所在的地区当前天气情况:"+"${findViewById<TextView>(R.id.t_weather).text}.\n"
                            +"最高温度为：${findViewById<TextView>(R.id.maxTemp).text};"+"最低温度为：${findViewById<TextView>(R.id.minTemp).text}.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // 发送通知
                notificationManager.notify(1, notificationBuilder.build())
            }

        }

    }
    //执行刷新操作，重新从天气API中获取数据
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.id)
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).isRefreshing = true
        if(isTable==1){
            var minTemp=0
            var maxTemp=0
            if(Translate.trans==1&& findViewById<TextView>(R.id.click_minTemp).text.isNotEmpty()){
                if(findViewById<TextView>(R.id.click_minTemp).text.length==4){
                    minTemp = findViewById<TextView>(R.id.click_minTemp).text.toString().substring(0,3).toInt()
                }else if(findViewById<TextView>(R.id.click_minTemp).text.length==3){
                    minTemp = findViewById<TextView>(R.id.click_minTemp).text.toString().substring(0,2).toInt()
                }else if(findViewById<TextView>(R.id.click_minTemp).text.length==2){
                    minTemp = findViewById<TextView>(R.id.click_minTemp).text.toString().substring(0,1).toInt()
                }
                minTemp = minTemp*9/5+32
                findViewById<TextView>(R.id.click_minTemp).text = minTemp.toString()+"℉"
                if(findViewById<TextView>(R.id.click_maxTemp).text.length==4){
                    maxTemp = findViewById<TextView>(R.id.click_maxTemp).text.toString().substring(0,3).toInt()
                }else if(findViewById<TextView>(R.id.click_maxTemp).text.length==3){
                    maxTemp = findViewById<TextView>(R.id.click_maxTemp).text.toString().substring(0,2).toInt()
                }else if(findViewById<TextView>(R.id.click_maxTemp).text.length==2){
                    maxTemp = findViewById<TextView>(R.id.click_maxTemp).text.toString().substring(0,1).toInt()
                }
                maxTemp = maxTemp*9/5+32
                findViewById<TextView>(R.id.click_maxTemp).text = maxTemp.toString()+"℉"
            }else if(Translate.trans==0&& findViewById<TextView>(R.id.click_minTemp).text.isNotEmpty()){
                if(findViewById<TextView>(R.id.click_minTemp).text.length==4){
                    minTemp = findViewById<TextView>(R.id.click_minTemp).text.toString().substring(0,3).toInt()
                }else if(findViewById<TextView>(R.id.click_minTemp).text.length==3){
                    minTemp = findViewById<TextView>(R.id.click_minTemp).text.toString().substring(0,2).toInt()
                }else if(findViewById<TextView>(R.id.click_minTemp).text.length==2){
                    minTemp = findViewById<TextView>(R.id.click_minTemp).text.toString().substring(0,1).toInt()
                }
                minTemp = (minTemp-32)*5/9
                if(minTemp<=0){
                    minTemp-=1
                }else if(minTemp>10){
                    minTemp += 1
                }
                findViewById<TextView>(R.id.click_minTemp).text = minTemp.toString()+"℃"
                if(findViewById<TextView>(R.id.click_maxTemp).text.length==4){
                    maxTemp = findViewById<TextView>(R.id.click_maxTemp).text.toString().substring(0,3).toInt()
                }else if(findViewById<TextView>(R.id.click_maxTemp).text.length==3){
                    maxTemp = findViewById<TextView>(R.id.click_maxTemp).text.toString().substring(0,2).toInt()
                }else if(findViewById<TextView>(R.id.click_maxTemp).text.length==2){
                    maxTemp = findViewById<TextView>(R.id.click_maxTemp).text.toString().substring(0,1).toInt()
                }
                maxTemp = (maxTemp-32)*5/9
                if(maxTemp<=0) {
                    maxTemp -= 1
                }else if(maxTemp>10){
                    maxTemp += 1
                }
                findViewById<TextView>(R.id.click_maxTemp).text = maxTemp.toString()+"℃"
            }
        }
    }
    //将天气数据渲染在界面中
    private fun showWeatherInfo(weather: List<DailyResponse.Daily>){
        findViewById<LinearLayout>(R.id.forcast_layout).removeAllViews()
        val dailys = weather
        if(isTable==0){
            val max_temp = "${dailys.get(0).maxTemp}"
            val min_temp = "${dailys.get(0).minTemp}"
            if(Translate.trans==0){
                findViewById<TextView>(R.id.minTemp).text = min_temp+"℃"
                findViewById<TextView>(R.id.maxTemp).text = max_temp+"℃"
            }else if(Translate.trans==1){
                var mintemp = min_temp.toInt()
                mintemp = mintemp*9/5+32
                var maxtemp = max_temp.toInt()
                maxtemp = maxtemp*9/5+32
                findViewById<TextView>(R.id.minTemp).text = mintemp.toString()+"°F"
                findViewById<TextView>(R.id.maxTemp).text = maxtemp.toString()+"°F"
            }
            findViewById<TextView>(R.id.todaydate).setText("Today,"+"${dailys[0].date}")
            findViewById<RelativeLayout>(R.id.todayLayout).setBackgroundResource(getSky(dailys[0].textDay).bg)
            findViewById<TextView>(R.id.t_weather).text = dailys[0].textDay
            findViewById<ImageView>(R.id.today_icon).setImageResource(getSky(dailys[0].textDay).icon)

            val days = dailys.size
            for (i in 1 until days){
                val day = dailys[i]
                val view = LayoutInflater.from(this).inflate(R.layout.forcast_item,findViewById<ScrollView>(R.id.forcast_layout),false)
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = simpleDateFormat.parse(day.date)
                val calendar = Calendar.getInstance()
                calendar.time = date

                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                val dayOfWeekString = when (dayOfWeek) {
                    Calendar.SUNDAY -> "周日"
                    Calendar.MONDAY -> "周一"
                    Calendar.TUESDAY -> "周二"
                    Calendar.WEDNESDAY -> "周三"
                    Calendar.THURSDAY -> "周四"
                    Calendar.FRIDAY -> "周五"
                    Calendar.SATURDAY -> "周六"
                    else -> "未知"
                }
                if(i==1){
                    view.findViewById<TextView>(R.id.forcast_date).text="Tomorrow"
                }else{
                    view.findViewById<TextView>(R.id.forcast_date).text= dayOfWeekString
                }
                view.findViewById<TextView>(R.id.forcast_weather).text = day.textDay
                val max_temp = "${day.maxTemp}"
                val min_temp = "${day.minTemp}"
                var mintemp = min_temp.toInt()
                var maxtemp = max_temp.toInt()
                if(Translate.trans==0){
                    view.findViewById<TextView>(R.id.forcast_minTemp).text = min_temp+"℃"
                    view.findViewById<TextView>(R.id.forcast_maxTemp).text = max_temp+"℃"
                }else if(Translate.trans==1){
                    mintemp = mintemp*9/5+32
                    maxtemp = maxtemp*9/5+32
                    view.findViewById<TextView>(R.id.forcast_minTemp).text = mintemp.toString()+"°F"
                    view.findViewById<TextView>(R.id.forcast_maxTemp).text = maxtemp.toString()+"°F"
                }
                view.findViewById<ImageView>(R.id.skyIcon).setImageResource(getSky(day.textDay).icon)
                view.isClickable = true
                view.isFocusable = true
                view.isHovered = true
                view.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // 长按开始，可以在此处执行相关操作
                            v.setBackgroundColor(Color.rgb(253, 246, 184 ))
                        }
                        MotionEvent.ACTION_UP -> {
                            // 长按结束，可以在此处执行相关操作
                            v.setBackgroundColor(Color.WHITE)
                        }
                    }
                    false
                }
                view.setOnClickListener {
                    val intent = Intent(applicationContext, WeatherDetailActivity::class.java).apply {
                        if(i==1){
                            putExtra("date","Tomorrow")
                        }else{
                            putExtra("date","${dayOfWeekString}")
                        }
                        putExtra("date_detail","${day.date}")
                        putExtra("textday",day.textDay)
                        putExtra("maxTemp",day.maxTemp)
                        putExtra("minTemp",day.minTemp)
                        putExtra("humidity",day.humidity)
                        putExtra("pressure",day.pressure)
                        putExtra("sunrise",day.sunrise)
                        putExtra("sunset",day.sunset)
                        putExtra("moonrise",day.moonrise)
                        putExtra("moonset",day.moonset)
                        startActivity(intent)
                    }
                    startActivity(intent)
                }
                findViewById<LinearLayout>(R.id.forcast_layout).addView(view)
            }
        }else{
            val days = dailys.size
            for (i in 0 until days){
                val day = dailys[i]
                val view = LayoutInflater.from(this).inflate(R.layout.forcast_item,findViewById<ScrollView>(R.id.forcast_layout),false)
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = simpleDateFormat.parse(day.date)
                val calendar = Calendar.getInstance()
                calendar.time = date

                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                val dayOfWeekString = when (dayOfWeek) {
                    Calendar.SUNDAY -> "周日"
                    Calendar.MONDAY -> "周一"
                    Calendar.TUESDAY -> "周二"
                    Calendar.WEDNESDAY -> "周三"
                    Calendar.THURSDAY -> "周四"
                    Calendar.FRIDAY -> "周五"
                    Calendar.SATURDAY -> "周六"
                    else -> "未知"
                }
                if(i == 0){
                    view.findViewById<TextView>(R.id.forcast_date).text="Today"
                }else if(i==1){
                    view.findViewById<TextView>(R.id.forcast_date).text="Tomorrow"
                }else{
                    view.findViewById<TextView>(R.id.forcast_date).text= dayOfWeekString
                }
                view.findViewById<TextView>(R.id.forcast_weather).text = day.textDay
                val max_temp = "${day.maxTemp}"
                val min_temp = "${day.minTemp}"
                var mintemp = min_temp.toInt()
                var maxtemp = max_temp.toInt()
                if(Translate.trans==0){
                    view.findViewById<TextView>(R.id.forcast_minTemp).text = min_temp+"℃"
                    view.findViewById<TextView>(R.id.forcast_maxTemp).text = max_temp+"℃"
                }else if(Translate.trans==1){
                    mintemp = mintemp*9/5+32
                    maxtemp = maxtemp*9/5+32
                    view.findViewById<TextView>(R.id.forcast_minTemp).text = mintemp.toString()+"°F"
                    view.findViewById<TextView>(R.id.forcast_maxTemp).text = maxtemp.toString()+"°F"
                }
                view.findViewById<ImageView>(R.id.skyIcon).setImageResource(getSky(day.textDay).icon)
                view.isClickable = true
                view.isFocusable = true
                view.isHovered = true
                view.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // 长按开始，可以在此处执行相关操作
                            v.setBackgroundColor(Color.rgb(253, 246, 184 ))
                        }
                        MotionEvent.ACTION_UP -> {
                            // 长按结束，可以在此处执行相关操作
                            v.setBackgroundColor(Color.WHITE)
                        }
                    }
                    false
                }
                view.setOnClickListener {
                    findViewById<TextView>(R.id.click_date).text=dayOfWeekString
                    findViewById<TextView>(R.id.click_date_detail).text=day.date
                    if(Translate.trans == 0){
                        findViewById<TextView>(R.id.click_maxTemp).text = day.maxTemp+"℃"
                        findViewById<TextView>(R.id.click_minTemp).text = day.minTemp+"℃"
                    }else{
                        val maxtemp = day.maxTemp.toInt() *9/5+32
                        val mintemp = day.minTemp.toInt() *9/5+32
                        findViewById<TextView>(R.id.click_maxTemp).text = maxtemp.toString()+"℉"
                        findViewById<TextView>(R.id.click_minTemp).text = mintemp.toString()+"℉"
                    }

                    val textday = day.textDay
                    findViewById<TextView>(R.id.click_textday).text = textday
                    textday?.let { getSky(it).icon }
                        ?.let { findViewById<ImageView>(R.id.click_icon).setImageResource(it) }
                    textday?.let { getSky(it).bg }
                        ?.let { findViewById<LinearLayout>(R.id.detailback).setBackgroundResource(it) }
                    findViewById<TextView>(R.id.click_humidity).text = "Humidity:"+day.humidity+"%"
                    findViewById<TextView>(R.id.click_pressure).text = "Pressure:"+day.pressure+"hPa"
                    findViewById<TextView>(R.id.click_sunrise).text = "Sunrise:"+day.sunrise
                    findViewById<TextView>(R.id.click_sunset).text = "Sunset:"+day.sunset
                    findViewById<TextView>(R.id.click_moonrise).text = "Moonrise:"+day.moonrise
                    findViewById<TextView>(R.id.click_moonset).text = "Moonset:"+day.moonset
                }
                findViewById<LinearLayout>(R.id.forcast_layout).addView(view)
            }
        }

        findViewById<DrawerLayout>(R.id.weatherlayout).visibility= View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.example.ACTION_MESSAGE") // 自定义广播的Action
        registerReceiver(messageReceiver, filter) // 注册广播接收器
    }
    //导航栏中图标的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //主页图标打开左侧边主页，进行地图搜索
        if (item.itemId == android.R.id.home) {
            val drawerLayout = findViewById<DrawerLayout>(R.id.weatherlayout)
            drawerLayout.openDrawer(GravityCompat.START)
            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {}
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {
                    val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    manager.hideSoftInputFromWindow(drawerView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)
                }
            })
            return true
        }
        //打开设置右侧主页，进行华氏度转换以及是否开启消息通知
        if(item.itemId == R.id.action_switches){
            val drawerLayout = findViewById<DrawerLayout>(R.id.weatherlayout)
            drawerLayout.openDrawer(GravityCompat.END)
            val switchButton = drawerLayout.findViewById<Switch>(R.id.switchButton)
            if(Translate.trans==1){
                switchButton.isChecked=true
            }
            switchButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // 开关打开时执行的代码
                    Translate.trans=1

                } else {
                    // 开关关闭时执行的代码
                    Translate.trans=0
                }
                refreshWeather()
            }
            val noticeswitch = drawerLayout.findViewById<Switch>(R.id.noticeswitchButton)
            if(Translate.notice == 1){
                noticeswitch.isChecked=true
            }
            noticeswitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Translate.notice=1
                } else {
                    Translate.notice=0
                }
            }
            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {}
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {
                    val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    manager.hideSoftInputFromWindow(drawerView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)
                }
            })
        }
        //短信分享
        if (item.itemId == R.id.action_share) {
            val phoneNumber = "13421800127" // 指定联系人的电话号码
            val message = "所在的地区当前天气情况:"+"${findViewById<TextView>(R.id.t_weather).text}.\n"+"最低温度为：${findViewById<TextView>(R.id.maxTemp).text};"+"最高温度为：${findViewById<TextView>(R.id.minTemp).text}." // 要分享的信息文本

            val sendIntent = Intent(Intent.ACTION_SENDTO)
            sendIntent.data = Uri.parse("smsto:$phoneNumber")
            sendIntent.putExtra("sms_body", message)
            startActivity(sendIntent)
            return true
        }
        if(item.itemId == R.id.email_share){
            val recipientEmail = "recipient@example.com" // 收件人邮箱地址
            val subject = "邮件主题" // 邮件主题
            val message = "所在的地区当前天气情况:"+"${findViewById<TextView>(R.id.t_weather).text}.\n"+"最低温度为：${findViewById<TextView>(R.id.maxTemp).text};"+"最高温度为：${findViewById<TextView>(R.id.minTemp).text}." // 要分享的信息文本

            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            sendIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(sendIntent, "选择邮件应用"))

            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}