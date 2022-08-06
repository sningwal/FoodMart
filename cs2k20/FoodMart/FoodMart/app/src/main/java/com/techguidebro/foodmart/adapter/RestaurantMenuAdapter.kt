package com.techguidebro.foodmart.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.activity.CartActivity
import com.techguidebro.foodmart.model.RestaurantMenuItem

class RestaurantMenuAdapter(
    val context: Context,
    val restaurantId: String?,
    val restaurantName: String?,
    val proceedToCartPassed: RelativeLayout,
    val buttonProceedToCart: Button,
    val restaurantMenus: ArrayList<RestaurantMenuItem>
) : RecyclerView.Adapter<RestaurantMenuAdapter.ViewHolderRestaurantMenu>() {
    var itemSelectedCount: Int = 0
    lateinit var proceedToCart: RelativeLayout
    var itemsSelectedId = arrayListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRestaurantMenu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_restaurant_menu_single_row, parent, false)
        return ViewHolderRestaurantMenu(view)
    }

    override fun getItemCount(): Int {
        return restaurantMenus.size
    }

    fun getSelectedItemCount(): Int {
        return itemSelectedCount
    }

    override fun onBindViewHolder(holder: ViewHolderRestaurantMenu, position: Int) {
        val restaurantMenuItem = restaurantMenus[position]
        proceedToCart = proceedToCartPassed
        /*holder.txtSerialNumber.text=restaurantMenuItem.ItemId*/
        holder.btnAddToCart.tag = restaurantMenuItem.ItemId + ""
        holder.txtSerialNumber.text = (position + 1).toString()
        holder.txtItemName.text = restaurantMenuItem.ItemName
        holder.txtItemPrice.text = restaurantMenuItem.Price
        holder.btnAddToCart.setOnClickListener {
            Toast.makeText(context, restaurantMenuItem.ItemName, Toast.LENGTH_SHORT).show()
            if (holder.btnAddToCart.text.toString() == "Remove") {
                itemSelectedCount--
                itemsSelectedId.remove(holder.btnAddToCart.tag.toString())
                holder.btnAddToCart.text = "Add"
                val favColor =
                    ContextCompat.getColor(context, R.color.purple_200)
                holder.btnAddToCart.setBackgroundColor(favColor)

            } else {

                itemSelectedCount++
                itemsSelectedId.add(holder.btnAddToCart.tag.toString())
                holder.btnAddToCart.text = "Remove"
                val favColor =
                    ContextCompat.getColor(context, R.color.customPrimaryDark)
                holder.btnAddToCart.setBackgroundColor(favColor)
            }

            if (itemSelectedCount > 0) {
                buttonProceedToCart.visibility = View.VISIBLE
            } else {
                buttonProceedToCart.visibility = View.INVISIBLE
            }
        }
        buttonProceedToCart.setOnClickListener {
            Toast.makeText(context, "proceed to cart", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, CartActivity::class.java)
            intent.putExtra("restaurantId", restaurantId.toString())
            intent.putExtra("restaurantName", restaurantName)
            intent.putExtra("selectedItemsId", itemsSelectedId)
            context.startActivity(intent)
        }
    }

    class ViewHolderRestaurantMenu(view: View) : RecyclerView.ViewHolder(view) {
        val txtSerialNumber: TextView = view.findViewById(R.id.txtSerialNumber)
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }


}