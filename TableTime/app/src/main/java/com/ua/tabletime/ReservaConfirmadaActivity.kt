package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReservaConfirmadaActivity: AppCompatActivity() {

    lateinit var btnVoltarHomepage: Button
    lateinit var textViewNomeRestaurante: TextView
    lateinit var textViewAvaliacaoRestaurante: TextView
    lateinit var textViewTipoCozinhaRestaurante: TextView
    lateinit var textEndereco: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reserva_confirmada)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltarHomepage = findViewById(R.id.buttonVoltarHomepage)
        textViewNomeRestaurante = findViewById(R.id.textViewNomeRestauranteSelecionado)
        textViewAvaliacaoRestaurante = findViewById(R.id.textViewAvaliacaoRestauranteSelecionado)
        textViewTipoCozinhaRestaurante = findViewById(R.id.textViewTipoCozinhaRestauranteSelecionado)
        textEndereco = findViewById(R.id.textEndereco)

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)

        val id_restaurante = sharedPref.getInt("id_restaurante", 0)
        val quantidade = sharedPref.getInt("quantidade", 0)
        val dataReserva = sharedPref.getString("dataReserva", "")
        val horario = sharedPref.getString("horario", "")
        val restaurantName = sharedPref.getString("restaurant_name", "")
        val restaurantAvaliacao = sharedPref.getFloat("restaurant_avaliacao", 0.0f).toDouble()
        val restaurantCuisine = sharedPref.getString("restaurant_cuisine", "")
        val restaurantEndereco = sharedPref.getString("restaurant_endereco", "")

        textViewNomeRestaurante.text = restaurantName
        textViewAvaliacaoRestaurante.text = String.format("%.1f", restaurantAvaliacao)
        textViewTipoCozinhaRestaurante.text = restaurantCuisine
        textEndereco.text = restaurantEndereco

        btnVoltarHomepage.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }
    }
}