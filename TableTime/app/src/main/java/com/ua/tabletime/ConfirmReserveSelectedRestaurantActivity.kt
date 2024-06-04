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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ConfirmReserveSelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtViewInfoNumeroPessoas: TextView
    lateinit var txtViewInfoData: TextView
    lateinit var txtViewInfoHorario: TextView
    lateinit var btnConfirmarReserva: Button
    lateinit var btnVoltar: Button
    private var id_restaurante: Int = 0
    private var quantidade: Int = 0
    private var dataReserva: String? = null
    private var horario: String? = null
    private var isAddingReserva = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirm_reserve_selected_restaurant)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        txtViewInfoData.text = dataReserva
        txtViewInfoHorario.text = horario
        txtViewInfoNumeroPessoas.text = quantidade.toString()

        Log.d(
            "ccc", "id_restaurante: $id_restaurante,\n" +
                    "data: $dataReserva,\n" +
                    "horario: $horario,\n" +
                    "quantidade: $quantidade"
        )

        btnConfirmarReserva.setOnClickListener {
            if (!isAddingReserva) {
                fetchVerificarDisponibilidadeFromApi()
            }
        }

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, SelectedRestaurantActivity::class.java))
        }
    }

    private fun fetchVerificarDisponibilidadeFromApi() {
        val url = "https://dadm-api.vercel.app/verificarDisponibilidade"
        val requestQueue = Volley.newRequestQueue(this)

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "")
        val editor = sharedPref.edit()
        editor.putInt("id_restaurante", id_restaurante)
        editor.putInt("quantidade", quantidade)
        editor.putString("dataReserva", dataReserva)
        editor.putString("horario", horario)
        editor.apply()

        val jsonObject = JSONObject().apply {
            put("id_restaurante", id_restaurante)
            put("data_reserva", dataReserva)
            put("horario", horario)
            put("quantidade", quantidade)
        }

        Log.d(
            "aaa", "id_restaurante: $id_restaurante,\n" +
                    "data: $dataReserva,\n" +
                    "horario: $horario,\n" +
                    "quantidade: $quantidade"
        )

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
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
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Horario indisponivel!\n Tente outro."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return HashMap<String, String>().apply {
                    put("Authorization", "Bearer $token")
                    put("Content-Type", "application/json")
                }
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    private fun addReserva() {
        if (isAddingReserva) return
        isAddingReserva = true

        val url = "https://dadm-api.vercel.app/addReserva"
        val requestQueue = Volley.newRequestQueue(this)

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "")
        val id_utilizador = sharedPref.getInt("user_id", 0)

        val jsonObject = JSONObject().apply {
            put("id_utilizador", id_utilizador)
            put("id_restaurante", id_restaurante)
            put("data_reserva", dataReserva)
            put("horario", horario)
            put("quantidade", quantidade)
        }

        Log.d(
            "bbb", "id_utilizador: $id_utilizador,\n" +
                    "id_restaurante: $id_restaurante,\n" +
                    "data: $dataReserva,\n" +
                    "horario: $horario,\n" +
                    "quantidade: $quantidade"
        )

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                isAddingReserva = false
                Toast.makeText(this, "Reserva adicionada com sucesso!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, ReservaConfirmadaActivity::class.java))

            },
            { error ->
                isAddingReserva = false
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Acesso não autorizado."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return HashMap<String, String>().apply {
                    put("Authorization", "Bearer $token")
                    put("Content-Type", "application/json")
                }
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}