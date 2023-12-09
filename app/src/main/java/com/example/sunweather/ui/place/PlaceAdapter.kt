package com.example.sunweather.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.sunweather.R
import com.example.sunweather.logic.model.Place
import com.example.sunweather.ui.weather.WeatherActivity

class PlaceAdapter(private val fragment: PlaceFragment, private val placeList:List<Place>):
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val placeName: TextView = view.findViewById(R.id.placeName)
        val placeAddress : TextView = view.findViewById(R.id.placeAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item,parent,false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener{
            val position=holder.adapterPosition
            val place = placeList[position]
            val activity = fragment.activity
            if(activity is WeatherActivity){
                activity.findViewById<DrawerLayout>(R.id.weatherlayout).closeDrawers()
                activity.viewModel.place_name = place.name
                activity.refreshWeather()
                val intent = Intent(parent.context,WeatherActivity::class.java).apply {
                    putExtra("id",place.id)
                    putExtra("placeName",place.name)
                }
                fragment.viewModel.savePlace(place)
                fragment.startActivity(intent)
                activity.finish()
            }else{
                val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                    putExtra("id",place.id)
                    putExtra("placeName",place.name)
                }
                fragment.viewModel.savePlace(place)
                fragment.startActivity(intent)
                fragment.activity?.finish()
            }
            fragment.viewModel.savePlace(place)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.country+" "+place.province+" "+place.city
    }

    override fun getItemCount()=placeList.size
}