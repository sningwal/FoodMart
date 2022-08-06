package com.techguidebro.foodmart.fragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.R.*
import com.techguidebro.foodmart.adapter.DashboardRecyclerAdapter
import com.techguidebro.foodmart.model.Restaurant
import com.techguidebro.foodmart.util.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class DashboardFragment(val contextParam: Context) : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var editTextSearch: EditText
    lateinit var radioButtonView: View
    lateinit var dashboardAdapter: DashboardRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var dashboard_fragment_cant_find_restaurant: RelativeLayout
    var restaurantList = arrayListOf<Restaurant>()
    var ratingComparator = Comparator<Restaurant> { rest1, rest2 ->
        if (rest1.restaurantRating.compareTo(rest2.restaurantRating, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.restaurantRating.compareTo(rest2.restaurantRating, true)
        }

    }
    var costComparator = Comparator<Restaurant> { rest1, rest2 ->

        rest1.cost_for_one.compareTo(rest2.cost_for_one, true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layout.fragment_dashboard, container, false)
        setHasOptionsMenu(true)
        layoutManager = LinearLayoutManager(activity)
        recyclerView =
            view.findViewById(R.id.recyclerViewDashboard)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        progressLayout =
            view.findViewById(R.id.dashboard_fragment_Progressdialog)
        dashboard_fragment_cant_find_restaurant =
            view.findViewById(R.id.dashboard_fragment_cant_find_restaurant)
        fun filterFun(strTyped: String) {
            val filteredList = arrayListOf<Restaurant>()

            for (item in restaurantList) {
                if (item.restaurantName.toLowerCase()
                        .contains(strTyped.toLowerCase())
                ) {

                    filteredList.add(item)

                }
            }
            if (filteredList.size == 0) {
                dashboard_fragment_cant_find_restaurant.visibility = View.VISIBLE
            } else {
                dashboard_fragment_cant_find_restaurant.visibility = View.INVISIBLE
            }
            dashboardAdapter.filterList(filteredList)
        }
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        }
        )
        return view
    }

    fun fetchData() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            progressLayout.visibility = View.VISIBLE
            try {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {
                        println("Response is $it")

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            val data = responseJsonObjectData.getJSONArray("data")

                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = Restaurant(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )
                                restaurantList.add(restaurantObject)
                                dashboardAdapter = DashboardRecyclerAdapter(
                                    activity as Context,
                                    restaurantList
                                )
                                recyclerView.adapter =
                                    dashboardAdapter
                                recyclerView.layoutManager =
                                    layoutManager

                            }

                        }
                        progressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        progressLayout.visibility = View.VISIBLE

                        println("Volley error $it")
                        if (activity != null) {
                            Toast.makeText(
                                activity as Context,
                                "Server Down!!!",
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
                    "Server Down!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when (id) {

            R.id.sort -> {
                View.inflate(
                    contextParam,
                    layout.sort_radio_button,
                    null
                ).also { radioButtonView = it }
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { text, listener ->
                        if (radioButtonView.radio_high_to_low.isChecked) {
                            Collections.sort(restaurantList, costComparator)
                            restaurantList.reverse()
                            dashboardAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_low_to_high.isChecked) {
                            Collections.sort(restaurantList, costComparator)
                            dashboardAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_rating.isChecked) {
                            Collections.sort(restaurantList, ratingComparator)
                            restaurantList.reverse()
                            dashboardAdapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("CANCEL") { text, listener ->

                    }
                    .create()
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (restaurantList.isEmpty())
                fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }
}




