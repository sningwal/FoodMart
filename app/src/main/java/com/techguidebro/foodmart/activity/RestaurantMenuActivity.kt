package com.techguidebro.foodmart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.adapter.RestaurantMenuAdapter
import com.techguidebro.foodmart.model.RestaurantMenuItem
import com.techguidebro.foodmart.util.ConnectionManager

class RestaurantMenuActivity : AppCompatActivity() {
    lateinit var relativeLayout: RelativeLayout
    lateinit var proceedToCartLayout: RelativeLayout
    lateinit var recyclerRestaurantMenu: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var buttonProceedToCart: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout
    lateinit var toolbar: Toolbar
    var resName: String? = "ResName"
    var restaurant_id: String? = "200"
    val restaurantList = arrayListOf<RestaurantMenuItem>()
    lateinit var recyclerAdapter: RestaurantMenuAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)
        toolbar = findViewById(R.id.toolBar)
        setupToolbar()
        if (intent != null) {
            resName = intent.getStringExtra("restaurantName")
        }
        restaurant_id = intent.getStringExtra("restaurantId")
        progressBar = findViewById(R.id.progessBar)
        proceedToCartLayout = findViewById(R.id.rlProceedToCart)
        buttonProceedToCart = findViewById(R.id.btnProceedToCart)
        progressLayout = findViewById(R.id.progressLayout)
        recyclerRestaurantMenu = findViewById(R.id.recyclerRestaurantMenu)
        relativeLayout = findViewById(R.id.relLayout)
        progressLayout.visibility = View.VISIBLE
        supportActionBar?.title = resName
        layoutManager = LinearLayoutManager(this)
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/$restaurant_id"
        if (ConnectionManager().checkConnectivity(this)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                    println("Response is $it")
                    val response = it.getJSONObject("data")
                    val success = response.getBoolean("success")
                    if (success) {
                        restaurantList.clear()
                        progressLayout.visibility = View.GONE
                        val data = response.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val restaurant = data.getJSONObject(i)
                            val menuItemObject = RestaurantMenuItem(
                                restaurant.getString("id"),
                                restaurant.getString("name"),
                                restaurant.getString("cost_for_one")
                            )
                            restaurantList.add(menuItemObject)
                            recyclerAdapter = RestaurantMenuAdapter(
                                this, restaurant_id, resName, proceedToCartLayout,
                                buttonProceedToCart, restaurantList
                            )
                            recyclerRestaurantMenu.adapter = recyclerAdapter
                            recyclerRestaurantMenu.layoutManager = layoutManager
                        }
                    }

                }, Response.ErrorListener {
                    println("volley error is $it")
                    Toast.makeText(
                        this,
                        "Some Error occurred!!!",
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
            queue.add(jsonObjectRequest)
        } else {
            val alterDialog = AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { text, listener ->
                finishAffinity()//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()
        }
    }

    fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = resName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                if (recyclerAdapter.getSelectedItemCount() > 0) {

                    val alterDialog = AlertDialog.Builder(this)
                    alterDialog.setTitle("Alert!")
                    alterDialog.setMessage("Going back will remove everything from cart")
                    alterDialog.setPositiveButton("Okay") { text, listener ->
                        super.onBackPressed()
                    }
                    alterDialog.setNegativeButton("No") { text, listener ->

                    }
                    alterDialog.show()
                } else {
                    super.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        if (recyclerAdapter.getSelectedItemCount() > 0) {

            val alterDialog = AlertDialog.Builder(this)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will remove everything from cart")
            alterDialog.setPositiveButton("Okay") { text, listener ->
                super.onBackPressed()
            }
            alterDialog.setNegativeButton("No") { text, listener ->

            }
            alterDialog.show()
        } else {
            super.onBackPressed()
        }

    }


}