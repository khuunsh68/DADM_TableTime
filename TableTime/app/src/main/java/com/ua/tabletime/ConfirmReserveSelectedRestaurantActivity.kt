package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.squareup.picasso.Picasso
import org.json.JSONObject

class ConfirmReserveSelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtNomeRestauranteSelecionado: TextView
    lateinit var txtAvaliacaoRestauranteSelecionado: TextView
    lateinit var txtTipoCozinhaRestauranteSelecionado: TextView
    lateinit var imageViewRestauranteSelecionado: ImageView
    lateinit var textEndereco: TextView
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
        imageViewRestauranteSelecionado = findViewById(R.id.imageViewRestauranteSelecionado)
        textEndereco = findViewById(R.id.textEndereco)
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
        txtNomeRestauranteSelecionado.text = restaurantName
        txtAvaliacaoRestauranteSelecionado.text = String.format("%.1f", restaurantAvaliacao)
        txtTipoCozinhaRestauranteSelecionado.text = restaurantCuisine
        textEndereco.text = restaurantEndereco
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
                showDialog("Atenção", "Requisição já em andamento. Por favor, aguarde.")
            }
        }

        btnVoltar.setOnClickListener {
            finish()
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

                Log.d("ddd", disponibilidade)
                if (disponibilidade == "horario disponivel") {
                    addReserva()
                } else if (disponibilidade == "horario indisponivel") {
                    showDialog("Atenção", "Horário indisponível! Tente outro.")
                }
            },
            { error ->
                isRequestInProgress = false
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Horário indisponível! Tente outro."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                showDialog("Erro", errorMsg)
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
                showDialogAndStartActivity("Sucesso", "Reserva adicionada com sucesso!", ReservaConfirmadaActivity::class.java)
            },
            { error ->
                isRequestInProgress = false
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Acesso não autorizado."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                showDialog("Erro", errorMsg)
            },
            mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
        )
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showDialogAndStartActivity(title: String, message: String, activityClass: Class<*>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            startActivity(Intent(this, activityClass))
        }
        builder.create().show()
    }
}