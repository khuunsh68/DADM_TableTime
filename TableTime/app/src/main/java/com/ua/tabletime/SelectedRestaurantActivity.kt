package com.ua.tabletime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar

class SelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtNomeRestauranteSelecionado: TextView
    lateinit var txtAvaliacaoRestauranteSelecionado: TextView
    lateinit var txtTipoCozinhaRestauranteSelecionado: TextView
    lateinit var buttonVerificarDisponibilidade: Button
    lateinit var editTextNumeroPessoas: EditText
    lateinit var editTextDataHora: EditText

    private var selectedDate: String? = null
    private var selectedTime: String? = null

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
        buttonVerificarDisponibilidade = findViewById(R.id.buttonVerificarDisponibilidade)
        editTextNumeroPessoas = findViewById(R.id.editTextNumeroPessoas)
        editTextDataHora = findViewById(R.id.editTextDataHora)

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
                                "Número de pessoas: $numeroPessoas\nData selecionada: $selectedDate\nHora selecionada: $selectedTime"
                            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            // ENVIAR OS DADOS PARA A API PARA VERIFICAR DISPONIBILIDADE

                            fetchVerificarDisponibilidadeFromApi(1, numeroPessoas);
                            //trocar 1 por o id_restaurante do restaurante selecionado

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

                selectedDate = "${dayOfMonth.toString().padStart(2, '0')}/${
                    (month + 1).toString().padStart(2, '0')
                }/$year"

                TimePickerDialog(this, { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    selectedTime = "${hourOfDay.toString().padStart(2, '0')}:${
                        minute.toString().padStart(2, '0')
                    }"

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
        val url = "https://dadm-api.vercel.app/reserva/verificarDisponibilidade"
        val requestQueue = Volley.newRequestQueue(this)

        val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "")

        val jsonObject = JSONObject()
        jsonObject.put("id_restaurante", id_restaurante)
        jsonObject.put("data_reserva", selectedDate)
        jsonObject.put("horario", selectedTime)
        jsonObject.put("quantidade", quantidade)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Toast.makeText(this, response.toString(), Toast.LENGTH_LONG).show()
            },
            { error ->
                val errorMsg = when (error.networkResponse?.statusCode) {
                    401 -> "${error.message}"
                    else -> "Erro: ${error.message}"
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        )

        val jsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                for (i in 0 until response.length()) {
                    val restaurantJson = response.getJSONObject(i)
                    Log.d("disponibilidade", restaurantJson.getString("disponibilidade"))
                }
            },
            { error ->
                // Handle error
                Toast.makeText(
                    this,
                    "Failed to fetch verificar disponibilidade: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            // Adicionar o token ao cabeçalho da solicitação
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(jsonArrayRequest)
    }

}