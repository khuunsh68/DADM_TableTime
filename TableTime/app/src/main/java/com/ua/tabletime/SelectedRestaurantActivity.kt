package com.ua.tabletime

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtNomeRestauranteSelecionado: TextView
    lateinit var txtAvaliacaoRestauranteSelecionado: TextView
    lateinit var txtTipoCozinhaRestauranteSelecionado: TextView
    lateinit var textEndereco: TextView
    lateinit var imageViewRestauranteSelecionado: ImageView
    lateinit var buttonVerificarDisponibilidade: Button
    lateinit var editTextNumeroPessoas: EditText
    lateinit var editTextDataHora: EditText
    lateinit var btnVoltar: Button

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var restaurantId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selected_restaurant)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtNomeRestauranteSelecionado = findViewById(R.id.textViewNomeRestauranteSelecionado)
        txtAvaliacaoRestauranteSelecionado = findViewById(R.id.textViewAvaliacaoRestauranteSelecionado)
        txtTipoCozinhaRestauranteSelecionado = findViewById(R.id.textViewTipoCozinhaRestauranteSelecionado)
        textEndereco = findViewById(R.id.textEndereco)
        imageViewRestauranteSelecionado = findViewById(R.id.imageViewRestauranteSelecionado)
        buttonVerificarDisponibilidade = findViewById(R.id.buttonVerificarDisponibilidade)
        editTextNumeroPessoas = findViewById(R.id.editTextNumeroPessoas)
        editTextDataHora = findViewById(R.id.editTextDataHora)
        btnVoltar = findViewById(R.id.btnVoltar)

        restaurantId = intent.getIntExtra("RESTAURANT_ID", 0)
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME")
        val restaurantAvaliacao = intent.getDoubleExtra("RESTAURANT_AVALIACAO", 0.0)
        val restaurantImage = intent.getStringExtra("RESTAURANT_IMAGE")
        val restaurantCuisine = intent.getStringExtra("RESTAURANT_CUISINE")
        val restaurantEndereco = intent.getStringExtra("RESTAURANT_ENDERECO")

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("restaurant_name", restaurantName)
            putFloat("restaurant_avaliacao", restaurantAvaliacao.toFloat())
            putString("restaurant_cuisine", restaurantCuisine)
            putString("restaurant_endereco", restaurantEndereco)
            putString("restaurant_image", restaurantImage)
            apply()
        }

        Picasso.get()
            .load(restaurantImage)
            .fit()
            .centerCrop()
            .into(imageViewRestauranteSelecionado)
        txtNomeRestauranteSelecionado.text = restaurantName
        txtAvaliacaoRestauranteSelecionado.text = restaurantAvaliacao.toString()
        txtTipoCozinhaRestauranteSelecionado.text = restaurantCuisine
        textEndereco.text = restaurantEndereco

        editTextDataHora.setOnClickListener {
            showDateTimePicker()
        }

        buttonVerificarDisponibilidade.setOnClickListener {
            val numeroPessoasStr = editTextNumeroPessoas.text.toString()
            if (numeroPessoasStr.isNotEmpty()) {
                val numeroPessoas = numeroPessoasStr.toIntOrNull()
                if (numeroPessoas != null) {
                    if (numeroPessoas >= 1) {
                        if (selectedDate != null && selectedTime != null) {

                            val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("id_restaurante", restaurantId)
                                putInt("quantidade", numeroPessoas)
                                putString("dataReserva", selectedDate)
                                putString("horario", selectedTime)
                                apply()
                            }

                            buttonVerificarDisponibilidade.isEnabled = false

                            fetchVerificarDisponibilidadeFromApi(restaurantId, numeroPessoas)
                        } else {
                            showDialog("Erro", "Por favor, selecione a data e a hora.")
                        }
                    } else {
                        showDialog("Erro", "O número mínimo de pessoas é 1.")
                    }
                } else {
                    showDialog("Erro", "Número inválido de pessoas.")
                }
            } else {
                showDialog("Erro", "Por favor, insira a quantidade de pessoas.")
            }
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = dateFormat.format(calendar.time)

            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                selectedTime = "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00"

                val selectedDateTime = "$selectedDate $selectedTime"
                editTextDataHora.setText(selectedDateTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun fetchVerificarDisponibilidadeFromApi(id_restaurante: Int, quantidade: Int) {
        val url = "https://dadm-api.vercel.app/verificarDisponibilidade"

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "")

        val jsonObject = JSONObject().apply {
            put("id_restaurante", id_restaurante)
            put("data_reserva", selectedDate)
            put("horario", selectedTime)
            put("quantidade", quantidade)
        }

        val headers = HashMap<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["Content-Type"] = "application/json"

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("response", response.toString())
                val disponibilidade = response.getString("disponibilidade")

                Log.d("ddd", disponibilidade)
                if (disponibilidade == "horario disponivel") {
                    buttonVerificarDisponibilidade.isEnabled = true;
                    showDialogAndStartActivity("Disponibilidade", "Horário disponível!", ConfirmReserveSelectedRestaurantActivity::class.java)
                } else if (disponibilidade == "horario indisponivel") {
                    showDialog("Indisponível", "Horário indisponível!\nTente outro.")
                    buttonVerificarDisponibilidade.isEnabled = true
                }
            },
            { error ->
                Log.d("erro", error.toString())
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Parâmetros inválidos."
                    401 -> "Horário indisponível!\nTente outro."
                    500 -> "Erro interno do servidor."
                    else -> "Erro: ${error.toString()}"
                }
                showDialog("Erro", errorMsg)
            },
            headers
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