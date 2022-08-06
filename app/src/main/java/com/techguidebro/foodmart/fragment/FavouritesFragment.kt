package com.techguidebro.foodmart.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.adapter.DashboardRecyclerAdapter
import com.techguidebro.foodmart.database.RestaurantDatabase
import com.techguidebro.foodmart.database.RestaurantEntity
import com.techguidebro.foodmart.model.Restaurant
import com.techguidebro.foodmart.util.ConnectionManager
import kotlinx.android.synthetic.main.fragment_favourites.*
import org.json.JSONException


class FavouritesFragment(val contextParam: Context) : Fragment() {
    var restaurantInfoList = arrayListOf<Restaurant>()
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var favouriteAdapter: DashboardRecyclerAdapter//using the same adapter here as it has the same functionality
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)
        recyclerView =
            view.findViewById(R.id.recyclerFavourite)//recycler view from Dashboard fragment
        layoutManager = LinearLayoutManager(activity)//set the layout manager
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progessBar)
        progressLayout.visibility = View.VISIBLE
        return view
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {

            try {
                progressLayout.visibility = View.GONE
                val queue = Volley.newRequestQueue(activity as Context)

                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {
                        println("Response12 is " + it)

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            restaurantInfoList.clear()//old listener of jsonObjectRequest are still listening therefore clear is used


                            val data = responseJsonObjectData.getJSONArray("data")

                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)

                                val restaurantEntity = RestaurantEntity(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name")
                                )

                                if (DBAsynTask(contextParam, restaurantEntity, 1).execute()
                                        .get()
                                )//if restaurant present add
                                {

                                    val restaurantObject = Restaurant(
                                        restaurantJsonObject.getString("id"),
                                        restaurantJsonObject.getString("name"),
                                        restaurantJsonObject.getString("rating"),
                                        restaurantJsonObject.getString("cost_for_one"),
                                        restaurantJsonObject.getString("image_url")
                                    )

                                    restaurantInfoList.add(restaurantObject)

                                    //progressBar.visibility = View.GONE

                                    favouriteAdapter = DashboardRecyclerAdapter(
                                        activity as Context,
                                        restaurantInfoList
                                    )//set the adapter with the data

                                    recyclerView.adapter =
                                        favouriteAdapter//bind the  recyclerView to the adapter

                                    recyclerView.layoutManager =
                                        layoutManager //bind the  recyclerView to the layoutManager

                                }
                            }
                            if (restaurantInfoList.size == 0) {//no items found
                                Toast.makeText(
                                    activity as Context,
                                    "Nothing is added to Favourites",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                    },
                    Response.ErrorListener {
                        println("Error12 is " + it)
                        if (activity != null) {


                            Toast.makeText(
                                activity as Context,
                                "mSome Error occurred!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()

                        headers["Content-type"] = "application/json"
                        headers["token"] = "0cd579d1cf2a35"
                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    activity as Context,
                    "Some Unexpected error occured!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()
        }

    }

    class DBAsynTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            /*
            * Mode 1->check if restaurant is in favourites
            * Mode 2->Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant*/


            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurant_id)
                    db.close()
                    return restaurant != null
                }
                else -> return false

            }

        }


    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            fetchData()//if internet is available fetch data
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }
}