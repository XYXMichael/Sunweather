package com.example.sunweather.ui.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdate
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.GeoCodeOption
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import com.example.sunweather.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapActivity : AppCompatActivity() {
    var mLocationClient = LocationClient(this)
    private lateinit var mMapView: MapView
    private lateinit var mBaiduMap : com.baidu.mapapi.map.BaiduMap
    private lateinit var tv_Lat: TextView
    private lateinit var tv_Lon: TextView
    private lateinit var tv_Add: TextView
    private lateinit var select: Button
    private lateinit var editText: EditText
    private var isFirstLocate = true
    private var isFirstText = true
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 获取地图控件引用
        mMapView = findViewById(R.id.bmapView)
        mBaiduMap = mMapView.map
        mBaiduMap.isMyLocationEnabled = true
        mBaiduMap = findViewById<MapView>(R.id.bmapView).map
        val mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING
        val mLocationConfiguration = MyLocationConfiguration(mCurrentMode, true, null, 0xAAFFFF88.toInt(), 0xAA00FF00.toInt())
        mBaiduMap.setMyLocationConfiguration(mLocationConfiguration)
        //定位初始化
        tv_Lat = findViewById(R.id.tv_Lat);
        tv_Lon = findViewById(R.id.tv_Lon);
        tv_Add = findViewById(R.id.tv_Add);
        select = findViewById(R.id.btn_search);
        editText = findViewById(R.id.address);

    //通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption()
        mBaiduMap.isMyLocationEnabled=true
        option.isOpenGps = true // 打开gps

        mLocationClient = LocationClient(getApplicationContext());

        // 设置扫描时间
        option.setScanSpan(1000);
        // 设置定位参数
        option.setCoorType("bd09ll"); // 设置坐标类型为百度经纬度
        option.setIsNeedAddress(true); // 设置需要获取地址信息
        // 设置定位模式
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//            option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
//            option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true); // 设置需要地址信息
        // 保存定位参数
        mLocationClient.setLocOption(option);
        findViewById<Button>(R.id.admit).setOnClickListener{
            val intent = Intent("com.example.ACTION_MESSAGE") // 自定义广播的Action
            intent.putExtra("message", tv_Add.text) // 添加要传递的信息
            sendBroadcast(intent) // 发送广播
            onBackPressed()
        }
        select.setOnClickListener {
            val address = editText.text.toString()
            if (address.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val geoCodeDeferred = async(Dispatchers.IO) {
                        searchGeoCode(address)
                    }
                    // 在searchGeoCode方法执行期间进行页面渲染
                    val geoCodeResult = geoCodeDeferred.await()

                    // 执行searchReverseGeoCode方法
                    searchReverseGeoCode(address)
                }
            } else {
                Toast.makeText(this, "请输入地点", Toast.LENGTH_SHORT).show()
            }
        }
        mLocationClient.registerLocationListener(MyLocationListener())
        //开启地图定位图层
        mLocationClient.start()

    }
    override fun onResume() {
        super.onResume()
        // 在 activity 执行 onResume 时执行 mMapView.onResume()，实现地图生命周期管理
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 在 activity 执行 onPause 时执行 mMapView.onPause()，实现地图生命周期管理
        mMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop();
        mBaiduMap?.setMyLocationEnabled(false);
        mMapView?.onDestroy();
    }
    private inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation) {
            if (bdLocation == null || mMapView == null){
                return;
            }
            if(isFirstLocate){
                tv_Add.text = bdLocation.addrStr.toString();
                tv_Lat.text = bdLocation.latitude.toString();
                tv_Lon.text = bdLocation.longitude.toString();
                isFirstLocate=false
                val locationData = MyLocationData.Builder()
                    .accuracy(bdLocation.radius)
                    .direction(bdLocation.direction)
                    .latitude(bdLocation.latitude)
                    .longitude(bdLocation.latitude)
                    .build();
                mBaiduMap?.setMyLocationData(locationData)
                val ll = LatLng(bdLocation.latitude,bdLocation.longitude);
                val update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap?.animateMapStatus(update);
                val bitmap =BitmapDescriptorFactory.fromResource(R.drawable.location)
                val width = 100
                val height = 100
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap.bitmap, width, height, false)
                val scaledBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                val markerOptions = MarkerOptions().position(ll).icon(scaledBitmapDescriptor).anchor(0.5f, 0.5f).draggable(true)
                mBaiduMap?.addOverlay(markerOptions)
            }

            // GPS 定位或网格定位时
//            if (bdLocation.locType == BDLocation.TypeGpsLocation || bdLocation.locType == BDLocation.TypeNetWorkLocation) {
//                navigateTo(bdLocation)
//            }
        }

//        private fun navigateTo(bdLocation: BDLocation) {
//            if (isFirstLocate) {
//                val ll = LatLng(bdLocation.latitude, bdLocation.longitude)
//                val update = MapStatusUpdateFactory.newLatLng(ll)
//                // 以动画更新方式，实现对手势引起的地图状态的更新
//                mBaiduMap?.animateMapStatus(update)
////                // 创建自定义标记
//                val markerOptions = MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.location)).anchor(0.5f, 0.5f)
//                mBaiduMap?.addOverlay(markerOptions)
//
//                isFirstLocate = false
//            }
//        }
    }
    private fun searchGeoCode(address: String) {
        mBaiduMap.clear()
        val geoCoder = GeoCoder.newInstance()
        val geoCodeOption = GeoCodeOption()
        geoCodeOption.address(address)
        geoCodeOption.city(address)
        var latitude:Double
        var longitude:Double
        val listener = object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult?) {
                if (null != geoCodeResult && null != geoCodeResult.location) {
                    if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                        //没有检索到结果
                        return
                    } else {
                        tv_Lat.text = geoCodeResult.location.latitude.toString()
                        tv_Lon.text = geoCodeResult.location.longitude.toString()
                        searchReverseGeoCode(address)
                        val ll = LatLng(geoCodeResult.location.latitude,geoCodeResult.location.longitude);
                        val update = MapStatusUpdateFactory.newLatLng(ll);
                        mBaiduMap?.animateMapStatus(update);
                        val bitmap =BitmapDescriptorFactory.fromResource(R.drawable.location)
                        val width = 100
                        val height = 100
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap.bitmap, width, height, false)
                        val scaledBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                        val markerOptions = MarkerOptions().position(ll).icon(scaledBitmapDescriptor).anchor(0.5f, 0.5f).draggable(true)
                        mBaiduMap?.addOverlay(markerOptions)
                        Log.d("Location","${geoCodeResult.location.latitude.toString()}"+" ${geoCodeResult.location.longitude.toString()}")
                    }
                }
            }

            override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                    return;
                } else {
                    //详细地址
                    tv_Add.text = reverseGeoCodeResult.getAddress()
                    //行政区号
                    val adCode = reverseGeoCodeResult. getCityCode();
                }
            }
        }
        geoCoder.setOnGetGeoCodeResultListener(listener)
        geoCoder.geocode(geoCodeOption)
        val latlon = LatLng(tv_Lat.text.toString().toDouble(),tv_Lon.text.toString().toDouble())
        geoCoder.reverseGeoCode(ReverseGeoCodeOption().location(latlon).newVersion(1).radius(500))
        geoCoder.destroy()
    }
    private fun searchReverseGeoCode(address: String) {
        val geoCoder = GeoCoder.newInstance()
        val geoCodeOption = GeoCodeOption()
        val listener = object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult?) {
                if (null != geoCodeResult && null != geoCodeResult.location) {
                    if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                        //没有检索到结果
                        return
                    } else {
                        tv_Lat.text = geoCodeResult.location.latitude.toString()
                        tv_Lon.text = geoCodeResult.location.longitude.toString()
                        Log.d("Location","${geoCodeResult.location.latitude.toString()}"+" ${geoCodeResult.location.longitude.toString()}")
                    }
                }

            }

            override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                    return;
                } else {
                    //详细地址
                    tv_Add.text = reverseGeoCodeResult.address
                    //行政区号
                    val adCode = reverseGeoCodeResult. getCityCode();
                }
            }
        }
        geoCoder.setOnGetGeoCodeResultListener(listener)
        val latlon = LatLng(tv_Lat.text.toString().toDouble(),tv_Lon.text.toString().toDouble())
        geoCoder.reverseGeoCode(ReverseGeoCodeOption().location(latlon).newVersion(1).radius(500))
        geoCoder.destroy()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            onBackPressed() // 执行返回操作
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
//class MyLocationListener : BDAbstractLocationListener() {
//    override fun onReceiveLocation(location: BDLocation?) {
//        //mapView 销毁后不在处理新接收的位置
//        if (location == null || mMapView == null) {
//            return
//        }
//        val locData = MyLocationData.Builder()
//            .accuracy(location.radius)
//            // 此处设置开发者获取到的方向信息，顺时针0-360
//            .direction(location.direction)
//            .latitude(location.latitude)
//            .longitude(location.longitude)
//            .build()
//        mBaiduMap?.setMyLocationData(locData)
//    }
//}
