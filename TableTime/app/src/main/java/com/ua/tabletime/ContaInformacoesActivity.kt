package com.ua.tabletime

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class ContaInformacoesActivity : AppCompatActivity() {

    private lateinit var buttonTerminarSessao: Button
    private lateinit var textTitulo: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var containerReservas: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conta_informacoes)

        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        buttonTerminarSessao = findViewById(R.id.buttonTerminarSessao)
        textTitulo = findViewById(R.id.textTitulo)
        containerReservas = findViewById(R.id.containerReservas)
        sharedPreferences = getSharedPreferences("appPrefs", MODE_PRIVATE)

        fetchUserData()
        fetchReservas()

        buttonTerminarSessao.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        textTitulo.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }
    }

    private fun fetchUserData() {
        val token = sharedPreferences.getString("jwt_token", null)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (token == null || userId == -1) {
            Toast.makeText(this, "Erro ao obter token ou ID do usuário", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://dadm-api.vercel.app/getUser/$userId"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val name = response.getString("nome")
                val email = response.getString("email")

                userName.text = name
                userEmail.text = email
            },
            { error ->
                Toast.makeText(this, "Falha ao buscar dados do usuário: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchReservas() {
        val token = sharedPreferences.getString("jwt_token", null)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (token == null || userId == -1) {
            Toast.makeText(this, "Erro ao obter token ou ID do usuário", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://dadm-api.vercel.app/get_all_reservas_from_user/$userId"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                displayReservas(response)
            },
            { error ->
                Toast.makeText(this, "Falha ao buscar reservas: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(jsonArrayRequest)
    }

    private fun displayReservas(reservas: JSONArray) {
        containerReservas.removeAllViews()

        for (i in 0 until reservas.length()) {
            val reserva = reservas.getJSONObject(i)
            val restaurantId = reserva.getInt("id_restaurante")

            fetchRestaurantName(restaurantId) { restaurantName ->
                val cardView = layoutInflater.inflate(R.layout.card_reserva, null)

                val textViewNomeReserva = cardView.findViewById<TextView>(R.id.textViewNomeRestaurante)
                val textViewQuantidade = cardView.findViewById<TextView>(R.id.textViewQuantidade)
                val textViewDataReserva = cardView.findViewById<TextView>(R.id.textViewDataReserva)
                val imageView = cardView.findViewById<ImageView>(R.id.imageView)
                val imageView2 = cardView.findViewById<ImageView>(R.id.imageView2)

                textViewNomeReserva.text = restaurantName
                textViewQuantidade.text = reserva.getInt("quantidade").toString()
                textViewDataReserva.text = "${reserva.getString("data_reserva")} - ${reserva.getString("horario")}"

                containerReservas.addView(cardView)
            }
        }
    }

    private fun fetchRestaurantName(restaurantId: Int, callback: (String) -> Unit) {
        val token = sharedPreferences.getString("jwt_token", null)

        if (token == null) {
            Toast.makeText(this, "Erro ao obter token", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://dadm-api.vercel.app/get_name_restaurant/$restaurantId"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val restaurantName = response.getString("nome")
                callback(restaurantName)
            },
            { error ->
                Toast.makeText(this, "Falha ao buscar nome do restaurante: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }
}