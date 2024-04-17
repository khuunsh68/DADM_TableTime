package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomepageActivity : AppCompatActivity() {

    private lateinit var btnProfilePage: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private lateinit var searchView: SearchView
    private lateinit var restaurantList: List<Restaurant>
    private lateinit var btnSearch: Button

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

        // Receber os dados da API(Testes para já sem api)
        restaurantList = fetchRestaurantDataFromApi()

        // Configurar o adaptador e definir no RecyclerView
        adapter = RestaurantAdapter(restaurantList)
        recyclerView.adapter = adapter

        btnProfilePage = findViewById(R.id.btnProfilePage)
        btnSearch = findViewById(R.id.buttonSearch)

        btnProfilePage.setOnClickListener {
            startActivity(Intent(this, ContaInformacoesActivity::class.java))
        }

        btnSearch.setOnClickListener {
            val query = searchView.query.toString()
            filter(query)
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

    private fun fetchRestaurantDataFromApi(): List<Restaurant> {
        // Aqui você buscaria os dados dos restaurantes da API e retornaria uma lista de objetos Restaurant
        // Exemplo simplificado:
        return listOf(
            Restaurant(R.drawable.restaurante1, "Restaurante 1", "Pizza", 4.2),
            Restaurant(R.drawable.restaurante1, "Restaurante 2", "Sushi", 4.5),
            Restaurant(R.drawable.restaurante1, "Restaurante 3", "Hamburgueria", 4.1),
            Restaurant(R.drawable.restaurante1, "Restaurante 4", "Comida Mexicana", 4.7),
            Restaurant(R.drawable.restaurante1, "Restaurante 5", "Churrascaria", 4.3),
            // Adicione mais restaurantes conforme necessário
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
}