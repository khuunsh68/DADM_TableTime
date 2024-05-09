package com.ua.tabletime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RestaurantAdapter(private var restaurantList: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewRestaurant: ImageView = itemView.findViewById(R.id.imageViewRestaurant)
        val textViewRestaurantName: TextView = itemView.findViewById(R.id.textViewRestaurantName)
        val textViewRestaurantCuisine: TextView = itemView.findViewById(R.id.textViewRestaurantCuisine)
        val textViewRestaurantRating: TextView = itemView.findViewById(R.id.textViewRestaurantRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val currentRestaurant = restaurantList[position]
        holder.imageViewRestaurant.setImageResource(currentRestaurant.imageResource)
        holder.textViewRestaurantName.text = currentRestaurant.name
        holder.textViewRestaurantCuisine.text = currentRestaurant.cuisineType
        holder.textViewRestaurantRating.text = currentRestaurant.avaliacao.toString()
    }

    override fun getItemCount() = restaurantList.size

    fun updateData(newList: List<Restaurant>) {
        restaurantList = newList
        notifyDataSetChanged()
    }
}
