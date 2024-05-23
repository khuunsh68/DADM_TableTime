package com.ua.tabletime

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
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
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class HomepageActivity : AppCompatActivity() {

    private lateinit var btnProfilePage: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private lateinit var searchView: SearchView
    private var restaurantList: List<Restaurant> = listOf()
    private lateinit var btnSearch: Button
    private lateinit var helloUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewRestaurants)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar o adaptador com uma lista vazia
        adapter = RestaurantAdapter(restaurantList)
        recyclerView.adapter = adapter

        // Receber os dados da API
        fetchRestaurantDataFromApi()

        btnProfilePage = findViewById(R.id.btnProfilePage)
        btnSearch = findViewById(R.id.buttonSearch)
        helloUser = findViewById(R.id.helloUser)

        btnProfilePage.setOnClickListener {
            startActivity(Intent(this, ContaInformacoesActivity::class.java))
        }

        btnSearch.setOnClickListener {
            val query = searchView.query.toString()
            filter(query)
        }

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val userName = sharedPref.getString("userName", "")

        // Atualiza o TextView com o nome do usuário
        if (!userName.isNullOrEmpty()) {
            helloUser.text = "Olá, $userName"
        }

        // Filtra o recyclerview após pesquisar na barra de pesquisa
        searchView = findViewById(R.id.searchView)
        // Servia para pesquisar sem botão, só escrevendo na barra de pesquisa
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filter(newText)
//                return true
//            }
//        })
    }

    private fun fetchRestaurantDataFromApi() {
        val url = "https://dadm-api.vercel.app/getAllRestaurants"
        val requestQueue = Volley.newRequestQueue(this)

        // Recuperar o token das SharedPreferences
        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "")

        val jsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                val restaurants = mutableListOf<Restaurant>()
                for (i in 0 until response.length()) {
                    val restaurantJson = response.getJSONObject(i)
                    // Verificar se a chave "nome" está presente no objeto JSON
                    if (restaurantJson.has("nome")) {
                        val restaurant = Restaurant(
                            name = restaurantJson.getString("nome"),
                            avaliacao = restaurantJson.getDouble("avaliacao"),
                            imageResource = restaurantJson.getString("imagem_url"),
                            cuisineType = restaurantJson.getString("tipo_cozinha")
                        )
                        restaurants.add(restaurant)
                    }
                }
                // Atualize a lista de restaurantes e o adaptador
                restaurantList = restaurants
                adapter.updateData(restaurantList)
            },
            { error ->
                // Handle error
                Toast.makeText(this, "Failed to fetch restaurants: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            // Adicionar o token ao cabeçalho da solicitação
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(jsonArrayRequest)
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
}