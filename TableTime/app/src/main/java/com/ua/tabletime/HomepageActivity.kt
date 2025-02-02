package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.Volley

class HomepageActivity : AppCompatActivity() {

    private lateinit var btnProfilePage: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private lateinit var searchView: SearchView
    private var restaurantList: List<Restaurant> = listOf()
    private lateinit var btnSearch: Button
    private lateinit var helloUser: TextView
    private lateinit var frameProgressHome: FrameLayout
    private lateinit var progressBarHome: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBarHome = findViewById(R.id.progressBarHome)
        btnProfilePage = findViewById(R.id.btnProfilePage)
        frameProgressHome = findViewById(R.id.frameProgressHome)
        btnProfilePage = findViewById(R.id.btnProfilePage)
        btnSearch = findViewById(R.id.buttonSearch)
        helloUser = findViewById(R.id.helloUser)
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerViewRestaurants)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RestaurantAdapter(restaurantList) { selectedRestaurant ->
            val intent = Intent(this, SelectedRestaurantActivity::class.java).apply {
                putExtra("RESTAURANT_ID", selectedRestaurant.id)
                putExtra("RESTAURANT_NAME", selectedRestaurant.name)
                putExtra("RESTAURANT_AVALIACAO", selectedRestaurant.avaliacao)
                putExtra("RESTAURANT_IMAGE", selectedRestaurant.imageResource)
                putExtra("RESTAURANT_CUISINE", selectedRestaurant.cuisineType)
                putExtra("RESTAURANT_ENDERECO", selectedRestaurant.endereco)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        frameProgressHome.visibility = View.VISIBLE
        progressBarHome.visibility = View.VISIBLE
        fetchRestaurantDataFromApi()

        btnProfilePage.setOnClickListener {
            startActivity(Intent(this, ContaInformacoesActivity::class.java))
        }

        btnSearch.setOnClickListener {
            val query = searchView.query.toString()
            filter(query)
        }

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val name = sharedPref.getString("name", "")

        // Atualiza o TextView com o nome do usuário
        if (!name.isNullOrEmpty()) {
            helloUser.text = "Olá, $name"
        }

    }

    private fun fetchRestaurantDataFromApi() {
        setButtonsEnabled(false)
        val url = "https://dadm-api.vercel.app/getAllRestaurants"

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "")

        val headers = mapOf("Authorization" to "Bearer $token")

        NetworkUtils.sendJsonArrayRequest(
            this,
            Request.Method.GET,
            url,
            { response ->
                val restaurants = mutableListOf<Restaurant>()
                for (i in 0 until response.length()) {
                    val restaurantJson = response.getJSONObject(i)
                    if (restaurantJson.has("nome")) {
                        val restaurant = Restaurant(
                            id = restaurantJson.getInt("id"),
                            name = restaurantJson.getString("nome"),
                            avaliacao = restaurantJson.getDouble("avaliacao"),
                            imageResource = restaurantJson.getString("imagem_url"),
                            cuisineType = restaurantJson.getString("tipo_cozinha"),
                            endereco = restaurantJson.getString("endereco")
                        )
                        restaurants.add(restaurant)
                    }
                }
                restaurantList = restaurants
                adapter.updateData(restaurantList)
                frameProgressHome.visibility = View.GONE
                progressBarHome.visibility = View.GONE
                setButtonsEnabled(true)
            },
            { error ->
                Toast.makeText(this, "Failed to fetch restaurants: ${error.message}", Toast.LENGTH_LONG).show()
                frameProgressHome.visibility = View.GONE
                progressBarHome.visibility = View.GONE
                setButtonsEnabled(true)
            },
            headers
        )
    }

    private fun filter(query: String?) {
        if (query.isNullOrBlank()) {
            adapter.updateData(restaurantList)
        } else {
            val filteredList = restaurantList.filter { restaurant ->
                restaurant.name.contains(query, ignoreCase = true)
            }
            adapter.updateData(filteredList)
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        btnProfilePage.isEnabled = enabled
        btnSearch.isEnabled = enabled
    }

}
