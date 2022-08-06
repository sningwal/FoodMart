package com.techguidebro.foodmart.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val restaurant_id: String,
    @ColumnInfo(name = "restaurant_name") val restaurantName: String
)

