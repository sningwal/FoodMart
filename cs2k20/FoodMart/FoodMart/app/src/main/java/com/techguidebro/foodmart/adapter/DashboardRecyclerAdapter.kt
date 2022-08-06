package com.techguidebro.foodmart.adapter
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.squareup.picasso.Picasso
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.activity.RestaurantMenuActivity
import com.techguidebro.foodmart.database.RestaurantDatabase
import com.techguidebro.foodmart.database.RestaurantEntity
import com.techguidebro.foodmart.model.Restaurant
import java.util.ArrayList
class DashboardRecyclerAdapter(val context: Context, var itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<DashboardRecyclerAdapter.DashboardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_dashboard_single_row, parent, false)
        return DashboardViewHolder(view)
    }
    fun filterList(filteredList: ArrayList<Restaurant>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val restaurant = itemList[position]
        val restaurantEntity = RestaurantEntity(
            restaurant.Id,
            restaurant.restaurantName
        )
        holder.txtRestaurantName.tag = restaurant.Id + ""
        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtPrice.text = restaurant.cost_for_one
        holder.txtRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.ic_default_restaurant)
            .into(holder.imgrestaurantImage)
        holder.txtFavouriteRestaurant.setOnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val result = DBAsyncTask(context, restaurantEntity, 2).execute().get()
                if (result) {
                    Toast.makeText(
                        context,
                        "${restaurant.restaurantName} added to Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    holder.txtFavouriteRestaurant.tag = "liked"
                    holder.txtFavouriteRestaurant.background =
                        context.resources.getDrawable(R.drawable.ic_fav_fill)

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            } else {
                val result = DBAsyncTask(context, restaurantEntity, 3).execute().get()
                if (result) {
                    Toast.makeText(
                        context,
                        "${restaurant.restaurantName} removed from Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    holder.txtFavouriteRestaurant.tag = "unliked"
                    holder.txtFavouriteRestaurant.background =
                        context.resources.getDrawable(R.drawable.ic_fav_out)

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
        holder.llContent.setOnClickListener {
            Toast.makeText(
                context, restaurant.restaurantName,
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantName", restaurant.restaurantName)
            intent.putExtra("restaurantId", restaurant.Id)
            context.startActivity(intent)
        }
        val checkFav = DBAsyncTask(context, restaurantEntity, 1).execute()
        val isFav = checkFav.get()
        if (isFav) {
            holder.txtFavouriteRestaurant.tag = "liked"
            holder.txtFavouriteRestaurant.background =
                context.resources.getDrawable(R.drawable.ic_fav_fill)

        } else {
            holder.txtFavouriteRestaurant.tag = "unliked"
            holder.txtFavouriteRestaurant.background =
                context.resources.getDrawable(R.drawable.ic_fav_out)
        }
    }
    class DashboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgrestaurantImage: ImageView = view.findViewById(R.id.imgRestaurant)
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurant)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtFavouriteRestaurant: TextView = view.findViewById(R.id.txtFavorites)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.restaurant_id)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                else -> return false
            }
        }
    }
}

