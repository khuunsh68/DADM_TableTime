package com.ua.tabletime

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import org.json.JSONArray
import org.json.JSONObject

class ContaInformacoesActivity : AppCompatActivity() {

    private lateinit var buttonTerminarSessao: Button
    private lateinit var textTitulo: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var containerReservas: LinearLayout
    private lateinit var frameProgressHome: FrameLayout
    private lateinit var progressBarHome: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conta_informacoes)

        progressBarHome = findViewById(R.id.progressBarHome)
        frameProgressHome = findViewById(R.id.frameProgressHome)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        buttonTerminarSessao = findViewById(R.id.buttonTerminarSessao)
        textTitulo = findViewById(R.id.textTitulo)
        containerReservas = findViewById(R.id.containerReservas)
        sharedPreferences = getSharedPreferences("appPrefs", MODE_PRIVATE)

        frameProgressHome.visibility = View.VISIBLE
        progressBarHome.visibility = View.VISIBLE
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
            showDialog("Erro", "Erro ao obter token ou ID do utilizador")
            frameProgressHome.visibility = View.GONE
            progressBarHome.visibility = View.GONE
            setButtonsEnabled(true)
            return
        }


        val url = "https://dadm-api.vercel.app/getUser"

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.GET, url, null,
            { response ->
                val name = response.getString("nome")
                val email = response.getString("email")

                userName.text = name
                userEmail.text = email

                setButtonsEnabled(true)
            },
            { error ->
                showDialog("Erro", "Falha ao recuperar dados do utilizador: ${error.message}")
                frameProgressHome.visibility = View.GONE
                progressBarHome.visibility = View.GONE
                setButtonsEnabled(true)
            },
            mapOf("Authorization" to "Bearer $token")
        )
    }

    private fun fetchReservas() {
        setButtonsEnabled(false)
        val token = sharedPreferences.getString("jwt_token", null)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (token == null || userId == -1) {
            showDialog("Erro", "Erro ao obter token ou ID do usuário")
            frameProgressHome.visibility = View.GONE
            progressBarHome.visibility = View.GONE
            setButtonsEnabled(true)
            return
        }

        val url = "https://dadm-api.vercel.app/get_all_reservas_from_user"

        NetworkUtils.sendJsonArrayRequest(
            this,
            Request.Method.GET,
            url,
            { response ->
                displayReservas(response)
                setButtonsEnabled(true)
            },
            { error ->
                showDialog("Erro", "Falha ao buscar reservas: ${error.message}")
                frameProgressHome.visibility = View.GONE
                progressBarHome.visibility = View.GONE
                setButtonsEnabled(true)
            },
            mapOf("Authorization" to "Bearer $token")
        )
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
                val btnDeleteReserva = cardView.findViewById<ImageView>(R.id.btnDeleteReserva)

                textViewNomeReserva.text = restaurantName
                textViewQuantidade.text = reserva.getInt("quantidade").toString()
                textViewDataReserva.text = "${reserva.getString("data_reserva")} - ${reserva.getString("horario")}"

                btnDeleteReserva.setOnClickListener {
                    showDeleteConfirmationDialog(reserva)
                }

                containerReservas.addView(cardView)
            }
        }
    }

    private fun showDeleteConfirmationDialog(reserva: JSONObject) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Exclusão")
        builder.setMessage("Tem certeza de que deseja excluir esta reserva?")
        builder.setPositiveButton("Sim") { dialog, _ ->
            deleteReserva(reserva)
            dialog.dismiss()
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteReserva(reserva: JSONObject) {
        val token = sharedPreferences.getString("jwt_token", null)

        if (token == null) {
            showDialog("Erro", "Erro ao obter token")
            return
        }

        val url = "https://dadm-api.vercel.app/removeReserva"

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST, url, reserva,
            { response ->
                val message = response.optString("message")
                showDialog("Informação", message)
                if (response.optBoolean("success")) {
                    fetchReservas()
                }
            },
            { error ->
                val response = error.networkResponse
                if (response != null && response.data != null) {
                    val jsonError = String(response.data)
                    val errorObj = JSONObject(jsonError)
                    val errorMessage = errorObj.optString("message", "Erro desconhecido")
                    showDialog("Erro", errorMessage)
                } else {
                    showDialog("Erro", "Falha ao remover reserva: ${error.message}")
                }
            },
            mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
        )
    }

    private fun fetchRestaurantName(restaurantId: Int, callback: (String) -> Unit) {
        setButtonsEnabled(false)
        val token = sharedPreferences.getString("jwt_token", null)

        if (token == null) {
            showDialog("Erro", "Erro ao obter token")
            frameProgressHome.visibility = View.GONE
            progressBarHome.visibility = View.GONE
            setButtonsEnabled(true)
            return
        }

        val url = "https://dadm-api.vercel.app/get_name_restaurant/$restaurantId"

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.GET, url, null,
            { response ->
                val restaurantName = response.getString("nome")
                callback(restaurantName)
                frameProgressHome.visibility = View.GONE
                progressBarHome.visibility = View.GONE
                setButtonsEnabled(true)
            },
            { error ->
                showDialog("Erro", "Falha ao obter nome do restaurante: ${error.message}")
                frameProgressHome.visibility = View.GONE
                progressBarHome.visibility = View.GONE
                setButtonsEnabled(true)
            },
            mapOf("Authorization" to "Bearer $token")
        )
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        buttonTerminarSessao.isEnabled = enabled
        textTitulo.isEnabled = enabled
    }
}
