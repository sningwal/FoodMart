package com.techguidebro.foodmart.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.adapter.OrderHistoryAdapter
import com.techguidebro.foodmart.model.OrderHistoryRestaurant
import com.techguidebro.foodmart.util.ConnectionManager
import org.json.JSONException


class OrderHistoryFragment(val contextParam: Context) : Fragment() {
    lateinit var layoutManager1: RecyclerView.LayoutManager
    lateinit var menuAdapter1: OrderHistoryAdapter
    lateinit var recyclerViewAllOrders: RecyclerView
    lateinit var order_activity_history_Progressdialog: RelativeLayout
    lateinit var order_history_fragment_no_orders: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_order_history, container, false)
        recyclerViewAllOrders = view.findViewById(R.id.recyclerViewAllOrders)
        order_activity_history_Progressdialog =view.findViewById(R.id.order_activity_history_Progressdialog)
        order_history_fragment_no_orders = view.findViewById(R.id.order_history_fragment_no_orders)
        return view
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(contextParam)) {
            setItemsForEachRestaurant()//if internet is available fetch data
        }else
        {
            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(contextParam)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit"){ text,listener->
                finishAffinity(Activity())//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }
        super.onResume()
    }
    fun setItemsForEachRestaurant(){
        layoutManager1 = LinearLayoutManager(contextParam)
        val orderedRestaurantList = ArrayList<OrderHistoryRestaurant>()
        val sharedPreferencess = contextParam.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        val user_id = sharedPreferencess.getString("user_id", "000")
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            order_activity_history_Progressdialog.visibility = View.VISIBLE
            try {

                val queue = Volley.newRequestQueue(contextParam)
                val url = "http://13.235.250.119/v2/orders/fetch_result/$user_id"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            val data = responseJsonObjectData.getJSONArray("data")

                            if (data.length() == 0) {//no items present display toast

                                Toast.makeText(
                                    contextParam,
                                    "No Orders Placed yet!!!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                order_history_fragment_no_orders.visibility = View.VISIBLE

                            } else {
                                order_history_fragment_no_orders.visibility = View.INVISIBLE



                                for (i in 0 until data.length()) {
                                    val restaurantItemJsonObject = data.getJSONObject(i)

                                    val eachRestaurantObject = OrderHistoryRestaurant(
                                        restaurantItemJsonObject.getString("order_id"),
                                        restaurantItemJsonObject.getString("restaurant_name"),
                                        restaurantItemJsonObject.getString("total_cost"),
                                        restaurantItemJsonObject.getString("order_placed_at")
                                            .substring(0, 10)
                                    )// only date is extracted

                                    orderedRestaurantList.add(eachRestaurantObject)

                                    menuAdapter1 = OrderHistoryAdapter(
                                        activity as Context,
                                        orderedRestaurantList
                                    )//set the adapter with the data
                                    recyclerViewAllOrders.adapter =
                                        menuAdapter1//bind the  recyclerView to the adapter

                                    recyclerViewAllOrders.layoutManager =
                                        layoutManager1 //bind the  recyclerView to the layoutManager

                                }

                            }

                        }
                        order_activity_history_Progressdialog.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        order_activity_history_Progressdialog.visibility = View.INVISIBLE
if(activity!=null) {
    Toast.makeText(
        activity as Context,
        "Some Error occurred!!!",
        Toast.LENGTH_SHORT
    ).show()

}                    }) {
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
            val alterDialog = AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(Activity())//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()

        }
    }
}