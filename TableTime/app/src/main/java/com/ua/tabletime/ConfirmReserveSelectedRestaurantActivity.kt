package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConfirmReserveSelectedRestaurantActivity : AppCompatActivity() {

    lateinit var txtViewInfoNumeroPessoas: TextView
    lateinit var txtViewInfoData: TextView
    lateinit var txtViewInfoHorario: TextView
    lateinit var btnConfirmarReserva: Button
    lateinit var btnVoltar: Button

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


        btnConfirmarReserva.setOnClickListener {
            //MANDAR PEDIDO À API
            //DEVERÍAMOS MANDAR UM VERIFICAR DISPONIBILIDADE NOVAMENTE ANTES DE FAZER UM ADD RESERVA



            //CASO DÊ TUDO CERTO VAI PARA ESTA PÁGINA
            //startActivity(Intent(this, ReservaConfirmadaActivity::class.java))
        }

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, SelectedRestaurantActivity::class.java))
        }
    }
}