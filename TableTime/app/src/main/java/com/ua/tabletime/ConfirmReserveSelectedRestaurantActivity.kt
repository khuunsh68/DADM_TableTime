package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import org.json.JSONObject

class ConfirmReserveSelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtNomeRestauranteSelecionado: TextView
    lateinit var txtAvaliacaoRestauranteSelecionado: TextView
    lateinit var txtTipoCozinhaRestauranteSelecionado: TextView
    lateinit var txtViewInfoNumeroPessoas: TextView
    lateinit var txtViewInfoData: TextView
    lateinit var txtViewInfoHorario: TextView
    lateinit var btnConfirmarReserva: Button
    lateinit var btnVoltar: Button
    private var id_restaurante: Int = 0
    private var quantidade: Int = 0
    private var dataReserva: String? = null
    private var horario: String? = null
    private var isRequestInProgress = false

    private var restaurantName: String? = null
    private var restaurantAvaliacao: Double = 0.0
    private var restaurantCuisine: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirm_reserve_selected_restaurant)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtNomeRestauranteSelecionado = findViewById(R.id.textViewNomeRestauranteSelecionado)
        txtAvaliacaoRestauranteSelecionado =
            findViewById(R.id.textViewAvaliacaoRestauranteSelecionado)
        txtTipoCozinhaRestauranteSelecionado =
            findViewById(R.id.textViewTipoCozinhaRestauranteSelecionado)
        txtViewInfoNumeroPessoas = findViewById(R.id.textViewInfoNumeroPessoas)
        txtViewInfoData = findViewById(R.id.textViewInfoData)
        txtViewInfoHorario = findViewById(R.id.textViewInfoHorario)
        btnConfirmarReserva = findViewById(R.id.buttonConfirmarReserva)
        btnVoltar = findViewById(R.id.buttonVoltar)

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        id_restaurante = sharedPref.getInt("id_restaurante", 0)
        quantidade = sharedPref.getInt("quantidade", 0)
        dataReserva = sharedPref.getString("dataReserva", "")
        horario = sharedPref.getString("horario", "")
        restaurantName = sharedPref.getString("restaurant_name", "")
        restaurantAvaliacao = sharedPref.getFloat("restaurant_avaliacao", 0.0f).toDouble()
        restaurantCuisine = sharedPref.getString("restaurant_cuisine", "")

        txtNomeRestauranteSelecionado.text = restaurantName
        txtAvaliacaoRestauranteSelecionado.text = restaurantAvaliacao.toString()
        txtTipoCozinhaRestauranteSelecionado.text = restaurantCuisine
        txtViewInfoNumeroPessoas.text = quantidade.toString()
        txtViewInfoData.text = dataReserva
        txtViewInfoHorario.text = horario

        Log.d(
            "ccc", "id_restaurante: $id_restaurante,\n" +
                    "data: $dataReserva,\n" +
                    "horario: $horario,\n" +
                    "quantidade: $quantidade"
        )

        btnConfirmarReserva.setOnClickListener {
            if (!isRequestInProgress) {
                fetchVerificarDisponibilidadeFromApi()
                btnConfirmarReserva.isEnabled = false
            } else {
                Toast.makeText(this, "Requisição já em andamento. Por favor, aguarde.", Toast.LENGTH_SHORT).show()
            }
        }

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, SelectedRestaurantActivity::class.java))
        }
    }

    private fun fetchVerificarDisponibilidadeFromApi() {
        if (isRequestInProgress) return
        isRequestInProgress = true

        val url = "https://dadm-api.vercel.app/verificarDisponibilidade"
        val token = getSharedPreferences("appPrefs", MODE_PRIVATE).getString("jwt_token", "")
        val jsonObject = JSONObject().apply {
            put("id_restaurante", id_restaurante)
            put("data_reserva", dataReserva)
            put("horario", horario)
            put("quantidade", quantidade)
        }

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST, url, jsonObject,
            { response ->
                isRequestInProgress = false
                val disponibilidade = response.getString("disponibilidade")
                Toast.makeText(this, disponibilidade, Toast.LENGTH_LONG).show()

                Log.d("ddd", disponibilidade)
                if (disponibilidade == "horario disponivel") {
                    addReserva()
                } else if (disponibilidade == "horario indisponivel") {
                    Toast.makeText(this, "Horario indisponivel!\n Tente outro.", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                isRequestInProgress = false
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Horario indisponivel!\n Tente outro."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            },
            mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
        )
    }

    private fun addReserva() {
        if (isRequestInProgress) return
        isRequestInProgress = true

        val url = "https://dadm-api.vercel.app/addReserva"
        val token = getSharedPreferences("appPrefs", MODE_PRIVATE).getString("jwt_token", "")
        val id_utilizador = getSharedPreferences("appPrefs", MODE_PRIVATE).getInt("user_id", 0)

        val jsonObject = JSONObject().apply {
            put("id_utilizador", id_utilizador)
            put("id_restaurante", id_restaurante)
            put("data_reserva", dataReserva)
            put("horario", horario)
            put("quantidade", quantidade)
        }

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST, url, jsonObject,
            { response ->
                isRequestInProgress = false
                Toast.makeText(this, "Reserva adicionada com sucesso!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, ReservaConfirmadaActivity::class.java))
            },
            { error ->
                isRequestInProgress = false
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Acesso não autorizado."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            },
            mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
        )
    }
}