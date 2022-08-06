package com.techguidebro.foodmart.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.adapter.CartRecyclerAdapter
import com.techguidebro.foodmart.model.CartItems
import com.techguidebro.foodmart.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


lateinit var toolbar: androidx.appcompat.widget.Toolbar
lateinit var textViewOrderingFrom: TextView
lateinit var buttonPlaceOrder: Button
lateinit var recyclerView: RecyclerView
lateinit var layoutManager: RecyclerView.LayoutManager
lateinit var menuAdapter: CartRecyclerAdapter
lateinit var restaurantId: String
lateinit var restaurantName: String
lateinit var linearLayout: LinearLayout
lateinit var activity_cart_Progressdialog: RelativeLayout
lateinit var selectedItemsId: ArrayList<String>
var totalAmount = 0
var cartListItems = arrayListOf<CartItems>()

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder)
        textViewOrderingFrom = findViewById(R.id.textViewOrderingFrom)
        linearLayout = findViewById(R.id.linearLayout)
        toolbar = findViewById(R.id.toolBar)
        activity_cart_Progressdialog = findViewById(R.id.activity_cart_Progressdialog)
        restaurantId = intent.getStringExtra("restaurantId").toString()
        restaurantName = intent.getStringExtra("restaurantName").toString()
        selectedItemsId = intent.getStringArrayListExtra("selectedItemsId") as ArrayList<String>
        //set the restaurant name
        textViewOrderingFrom.text = restaurantName
        buttonPlaceOrder.setOnClickListener(View.OnClickListener {

            val sharedPreferencess = this.getSharedPreferences(
                getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            if (ConnectionManager().checkConnectivity(this)) {
                activity_cart_Progressdialog.visibility = View.GONE
                try {
                    val foodJsonArray = JSONArray()
                    for (foodItem in selectedItemsId) {
                        val singleItemObject = JSONObject()
                        singleItemObject.put("food_item_id", foodItem)
                        foodJsonArray.put(singleItemObject)
                    }
                    val sendOrder = JSONObject()
                    sendOrder.put("user_id", sharedPreferencess.getString("user_id", "0"))
                    sendOrder.put("restaurant_id", restaurantId)
                    sendOrder.put("total_cost", totalAmount)
                    sendOrder.put("food", foodJsonArray)
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://13.235.250.119/v2/place_order/fetch_result/"
                    val jsonObjectRequest = object : JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        sendOrder,
                        Response.Listener {
                            println("Placed $it")
                            val responseJsonObjectData = it.getJSONObject("data")
                            val success = responseJsonObjectData.getBoolean("success")
                            if (success) {
                                Toast.makeText(
                                    this,
                                    "Order Placed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                createNotification()
                                val intent = Intent(this, OrderConfirmActivity::class.java)
                                startActivity(intent)
                                finishAffinity()
                            } else {
                                val responseMessageServer =
                                    responseJsonObjectData.getString("errorMessage")
                                Toast.makeText(
                                    this,
                                    responseMessageServer.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            activity_cart_Progressdialog.visibility = View.INVISIBLE
                        },
                        Response.ErrorListener {

                            println("Cart Volley error $it")

                            Toast.makeText(
                                this,
                                " Server Placed Error occurred!!!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()

                            headers["Content-type"] = "application/json"
                            headers["token"] = "0cd579d1cf2a35"
                            return headers
                        }
                    }
                    jsonObjectRequest.setRetryPolicy(
                        DefaultRetryPolicy(
                            15000,
                            1,
                            1f
                        )
                    )

                    queue.add(jsonObjectRequest)

                } catch (e: JSONException) {
                    Toast.makeText(
                        this,
                        "JSON Some unexpected error occured!!!",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            } else {

                val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                alterDialog.setTitle("No Internet")
                alterDialog.setMessage("Internet Connection can't be establish!")
                alterDialog.setPositiveButton("Open Settings") { text, listener ->
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(settingsIntent)
                }

                alterDialog.setNegativeButton("Exit") { text, listener ->
                    finishAffinity()
                }
                alterDialog.setCancelable(false)

                alterDialog.create()
                alterDialog.show()
            }
        })


        setToolBar()
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerViewCart)
    }

    fun fetchData() {

        if (ConnectionManager().checkConnectivity(this)) {
            activity_cart_Progressdialog.visibility = View.VISIBLE
            try {

                val queue = Volley.newRequestQueue(this)

                val url = "http://13.235.250.119/v2/restaurants/fetch_result/$restaurantId"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            val data = responseJsonObjectData.getJSONArray("data")


                            cartListItems.clear()

                            totalAmount = 0

                            for (i in 0 until data.length()) {
                                val cartItemJsonObject = data.getJSONObject(i)

                                if (selectedItemsId.contains(cartItemJsonObject.getString("id"))) {

                                    val menuObject = CartItems(
                                        cartItemJsonObject.getString("id"),
                                        cartItemJsonObject.getString("name"),
                                        cartItemJsonObject.getString("cost_for_one"),
                                        cartItemJsonObject.getString("restaurant_id")
                                    )

                                    totalAmount += cartItemJsonObject.getString("cost_for_one")
                                        .toString().toInt()

                                    cartListItems.add(menuObject)

                                }

                                menuAdapter = CartRecyclerAdapter(
                                    this,
                                    cartListItems
                                )

                                recyclerView.adapter = menuAdapter
                                recyclerView.layoutManager = layoutManager

                            }

                            buttonPlaceOrder.text = "Place Order(Total:Rs. $totalAmount)"
                        }
                        activity_cart_Progressdialog.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        activity_cart_Progressdialog.visibility = View.INVISIBLE

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
                    this,
                    "Some Unexpected error occured!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                finishAffinity()
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

    }

    fun setToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {

        if (ConnectionManager().checkConnectivity(this)) {
            fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                finishAffinity()
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

        super.onResume()
    }

    fun createNotification() {
        val notificationId = 1;
        val channelId = "personal_notification"
        val notificationBulider = NotificationCompat.Builder(this, channelId)
        notificationBulider.setSmallIcon(R.drawable.ic_order)
        notificationBulider.setContentTitle("Order Placed")
        notificationBulider.setContentText("Your order has been successfully placed!")
        notificationBulider.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("Ordered from $restaurantName and amounting to Rs.$totalAmount")
        )
        notificationBulider.setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, notificationBulider.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "Order Placed"
            val description = "Your order has been successfully placed!"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            val notificationManager =
                (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}

