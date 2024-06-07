package com.ua.tabletime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtNomeRestauranteSelecionado: TextView
    lateinit var txtAvaliacaoRestauranteSelecionado: TextView
    lateinit var txtTipoCozinhaRestauranteSelecionado: TextView
    lateinit var imageViewRestauranteSelecionado: ImageView
    lateinit var buttonVerificarDisponibilidade: Button
    lateinit var editTextNumeroPessoas: EditText
    lateinit var editTextDataHora: EditText

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var restaurantId: Int = 0

    // Atributos para armazenar os dados do restaurante
    private var restaurantName: String? = null
    private var restaurantAvaliacao: Double = 0.0
    private var restaurantImage: String? = null
    private var restaurantCuisine: String? = null

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
        txtAvaliacaoRestauranteSelecionado =
            findViewById(R.id.textViewAvaliacaoRestauranteSelecionado)
        txtTipoCozinhaRestauranteSelecionado =
            findViewById(R.id.textViewTipoCozinhaRestauranteSelecionado)
        imageViewRestauranteSelecionado =
            findViewById(R.id.imageViewRestauranteSelecionado)
        buttonVerificarDisponibilidade = findViewById(R.id.buttonVerificarDisponibilidade)
        editTextNumeroPessoas = findViewById(R.id.editTextNumeroPessoas)
        editTextDataHora = findViewById(R.id.editTextDataHora)

        restaurantId = intent.getIntExtra("RESTAURANT_ID", 0)
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME")
        val restaurantAvaliacao = intent.getDoubleExtra("RESTAURANT_AVALIACAO", 0.0)
        val restaurantImage = intent.getStringExtra("RESTAURANT_IMAGE")
        val restaurantCuisine = intent.getStringExtra("RESTAURANT_CUISINE")

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("restaurant_name", restaurantName)
            putFloat("restaurant_avaliacao", restaurantAvaliacao.toFloat())
            putString("restaurant_cuisine", restaurantCuisine)
            apply()
        }

        txtNomeRestauranteSelecionado.text = restaurantName
        txtAvaliacaoRestauranteSelecionado.text = restaurantAvaliacao.toString()
        txtTipoCozinhaRestauranteSelecionado.text = restaurantCuisine

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
                            val message =
                                "Data selecionada: $selectedDate\nHora selecionada: $selectedTime"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

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
                            Toast.makeText(
                                this,
                                "Por favor, selecione a data e a hora.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "O número mínimo de pessoas é 1.", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Número inválido de pessoas.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Por favor, insira a quantidade de pessoas.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time);

                TimePickerDialog(this, { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    selectedTime = "${hourOfDay.toString().padStart(2, '0')}:${
                        minute.toString().padStart(2, '0')
                    }:00"

                    val selectedDateTime = "$selectedDate $selectedTime"
                    editTextDataHora.setText(selectedDateTime)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

        Log.d(
            "aaa", "id_restaurante: $id_restaurante,\n" +
                    "data: $selectedDate,\n" +
                    "horario: $selectedTime,\n" +
                    "quantidade: $quantidade"
        )

        val headers = HashMap<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["Content-Type"] = "application/json"

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST, url, jsonObject,
            { response ->
                val disponibilidade = response.getString("disponibilidade")
                Toast.makeText(this, disponibilidade, Toast.LENGTH_LONG).show()

                Log.d("ddd", disponibilidade)
                if(disponibilidade == "horario disponivel") {
                    startActivity(Intent(this,  ConfirmReserveSelectedRestaurantActivity::class.java))
                } else if(disponibilidade == "horario indisponivel") {
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
            },
            headers
        )
    }

}