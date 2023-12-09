package com.example.sunweather.ui.weather

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.sunweather.R
import com.example.sunweather.logic.model.Translate
import com.example.sunweather.logic.model.getSky

class WeatherDetailActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_datail)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<TextView>(R.id.click_date).text=intent.getStringExtra("date")
        findViewById<TextView>(R.id.click_date_detail).text=intent.getStringExtra("date_detail")
        if(Translate.trans == 0){
            findViewById<TextView>(R.id.click_maxTemp).text = intent.getStringExtra("maxTemp")+"℃"
            findViewById<TextView>(R.id.click_minTemp).text = intent.getStringExtra("minTemp")+"℃"
        }else{
            val maxtemp = (intent.getStringExtra("maxTemp")?.toInt() ?: 0) *9/5+32
            val mintemp = (intent.getStringExtra("minTemp")?.toInt() ?: 0) *9/5+32
            findViewById<TextView>(R.id.click_maxTemp).text = maxtemp.toString()+"℉"
            findViewById<TextView>(R.id.click_minTemp).text = mintemp.toString()+"℉"
        }

        val textday = intent.getStringExtra("textday")
        findViewById<TextView>(R.id.click_textday).text = textday
        textday?.let { getSky(it).icon }
            ?.let { findViewById<ImageView>(R.id.click_icon).setImageResource(it) }
        textday?.let { getSky(it).bg }
            ?.let { findViewById<LinearLayout>(R.id.detailback).setBackgroundResource(it) }
        findViewById<TextView>(R.id.click_humidity).text = "Humidity:"+intent.getStringExtra("humidity")+"%"
        findViewById<TextView>(R.id.click_pressure).text = "Pressure:"+intent.getStringExtra("pressure")+"hPa"
        findViewById<TextView>(R.id.click_sunrise).text = "Sunrise:"+intent.getStringExtra("sunrise")
        findViewById<TextView>(R.id.click_sunset).text = "Sunset:"+intent.getStringExtra("sunset")
        findViewById<TextView>(R.id.click_moonrise).text = "Moonrise:"+intent.getStringExtra("moonrise")
        findViewById<TextView>(R.id.click_moonset).text = "Moonset:"+intent.getStringExtra("moonset")
        findViewById<DrawerLayout>(R.id.detail_weatherlayout).visibility= View.VISIBLE
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed() // 执行返回操作
            return true
        }
        if(item.itemId == R.id.action_switches){
            val drawerLayout = findViewById<DrawerLayout>(R.id.detail_weatherlayout)
            drawerLayout.openDrawer(GravityCompat.END)
            val switchButton = drawerLayout.findViewById<Switch>(R.id.switchButton)
            if(Translate.trans==1){
                switchButton.isChecked=true
            }
            switchButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // 开关打开时执行的代码
                    Translate.trans=1
                    val maxtemp = (intent.getStringExtra("maxTemp")?.toInt() ?: 0) *9/5+32
                    val mintemp = (intent.getStringExtra("minTemp")?.toInt() ?: 0) *9/5+32
                    findViewById<TextView>(R.id.click_maxTemp).text = maxtemp.toString()+"℉"
                    findViewById<TextView>(R.id.click_minTemp).text = mintemp.toString()+"℉"
                } else {
                    // 开关关闭时执行的代码
                    Translate.trans=0
                    findViewById<TextView>(R.id.click_maxTemp).text = intent.getStringExtra("maxTemp")+"℃"
                    findViewById<TextView>(R.id.click_minTemp).text = intent.getStringExtra("minTemp")+"℃"
                }
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
            val message = "所在的地区当前天气情况:"+"${findViewById<TextView>(R.id.click_textday).text}.\n"+"最高温度为：${findViewById<TextView>(R.id.click_maxTemp).text};"+"最低温度为：${findViewById<TextView>(R.id.click_minTemp).text}." // 要分享的信息文本

            val sendIntent = Intent(Intent.ACTION_SENDTO)
            sendIntent.data = Uri.parse("smsto:$phoneNumber")
            sendIntent.putExtra("sms_body", message)
            startActivity(sendIntent)
            return true
        }
        if(item.itemId == R.id.email_share){
            val recipientEmail = "recipient@example.com" // 收件人邮箱地址
            val subject = "邮件主题" // 邮件主题
            val message = "所在的地区当前天气情况:"+"${findViewById<TextView>(R.id.click_textday).text}.\n"+"最高温度为：${findViewById<TextView>(R.id.click_maxTemp).text};"+"最低温度为：${findViewById<TextView>(R.id.click_minTemp).text}." // 要分享的信息文本

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