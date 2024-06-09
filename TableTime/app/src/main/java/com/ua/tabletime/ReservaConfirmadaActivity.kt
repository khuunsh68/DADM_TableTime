package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso

class ReservaConfirmadaActivity: AppCompatActivity() {

    lateinit var btnVoltarHomepage: Button
    lateinit var textViewNomeRestaurante: TextView
    lateinit var textViewAvaliacaoRestaurante: TextView
    lateinit var textViewTipoCozinhaRestaurante: TextView
    lateinit var textEndereco: TextView
    lateinit var imageViewRestauranteSelecionado: ImageView

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
        imageViewRestauranteSelecionado = findViewById(R.id.imageViewRestauranteSelecionado)

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)

        val restaurantName = sharedPref.getString("restaurant_name", "")
        val restaurantAvaliacao = sharedPref.getFloat("restaurant_avaliacao", 0.0f).toDouble()
        val restaurantCuisine = sharedPref.getString("restaurant_cuisine", "")
        val restaurantEndereco = sharedPref.getString("restaurant_endereco", "")
        val restaurantImage = sharedPref.getString("restaurant_image", "")

        Picasso.get()
            .load(restaurantImage)
            .fit()
            .centerCrop()
            .into(imageViewRestauranteSelecionado)
        textViewNomeRestaurante.text = restaurantName
        textViewAvaliacaoRestaurante.text = String.format("%.1f", restaurantAvaliacao)
        textViewTipoCozinhaRestaurante.text = restaurantCuisine
        textEndereco.text = restaurantEndereco

        btnVoltarHomepage.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }
    }
}