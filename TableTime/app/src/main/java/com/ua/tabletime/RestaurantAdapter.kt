package com.ua.tabletime

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RestaurantAdapter(
    private var restaurantList: List<Restaurant>,
    private val onRestaurantClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewRestaurant: ImageView = itemView.findViewById(R.id.imageViewRestaurant)
        val textViewRestaurantName: TextView = itemView.findViewById(R.id.textViewRestaurantName)
        val textViewRestaurantCuisine: TextView = itemView.findViewById(R.id.textViewRestaurantCuisine)
        val textViewRestaurantRating: TextView = itemView.findViewById(R.id.textViewRestaurantRating)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRestaurantClick(restaurantList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val currentRestaurant = restaurantList[position]

        Glide.with(holder.itemView.context)
            .load(currentRestaurant.imageResource)
            .into(holder.imageViewRestaurant)

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