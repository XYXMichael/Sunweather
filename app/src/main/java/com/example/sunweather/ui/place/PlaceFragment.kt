package com.example.sunweather.ui.place

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sunweather.ui.map.MapActivity
import com.example.sunweather.R
import com.example.sunweather.ui.map.MainActivity
import com.example.sunweather.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(activity is MainActivity && viewModel.isPlaceSaved()){
            val place = viewModel.getSavedPlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("id",place.id)
                putExtra("placeName",place.name)
            }
            startActivity(intent)
        }
        val layoutManager = LinearLayoutManager(activity)
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.layoutManager = layoutManager
        adapter = PlaceAdapter(this,viewModel.placeList)
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = adapter
        view?.findViewById<EditText>(R.id.searchPlaceEdit)?.addTextChangedListener{ editable ->
            val content = editable.toString()
            if(content.isNotEmpty()){
                viewModel.searchPlaces(content)
            }else{
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.visibility = View.GONE
                view?.findViewById<ImageView>(R.id.bgImageView)?.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        view?.findViewById<ImageButton>(R.id.location)?.setOnClickListener{
            val intent=Intent(context, MapActivity::class.java)
            startActivity(intent)
        }
        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
            val places = result.getOrNull()
            if(places != null){
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.visibility = View.VISIBLE
                view?.findViewById<ImageView>(R.id.bgImageView)?.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            }else{
                Toast.makeText(activity,"未能查询到任何地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }

}