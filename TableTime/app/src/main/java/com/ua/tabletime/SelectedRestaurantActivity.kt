package com.ua.tabletime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                            val message = "Número de pessoas: $numeroPessoas\nData selecionada: $selectedDate\nHora selecionada: $selectedTime"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            // ENVIAR OS DADOS PARA A API PARA VERIFICAR DISPONIBILIDADE
                        } else {
                            Toast.makeText(this, "Por favor, selecione a data e a hora.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "O número mínimo de pessoas é 1.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Número inválido de pessoas.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Por favor, insira a quantidade de pessoas.", Toast.LENGTH_LONG).show()
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

}