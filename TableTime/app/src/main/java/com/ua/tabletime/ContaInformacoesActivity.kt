package com.ua.tabletime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class ContaInformacoesActivity : AppCompatActivity() {

    lateinit var buttonTerminarSessao: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conta_informacoes)


        setContentView(R.layout.activity_conta_informacoes)

        // Obtendo referência ao LinearLayout dentro do ScrollView
        val containerReservas = findViewById<LinearLayout>(R.id.containerReservas)

        // Adicionando manualmente os cartões de reserva
        for (i in 1..5) {//5 cartões exemplo
            val cardView = layoutInflater.inflate(R.layout.card_reserva, null)

            val textViewNomeReserva = cardView.findViewById<TextView>(R.id.textViewNomeRestaurante)
            val textViewQuantidade = cardView.findViewById<TextView>(R.id.textViewQuantidade)
            val textViewDataReserva = cardView.findViewById<TextView>(R.id.textViewDataReserva)
            val imageView = cardView.findViewById<ImageView>(R.id.imageView)
            val imageView2 = cardView.findViewById<ImageView>(R.id.imageView2)

            textViewNomeReserva.text = "Nome do Restaurante $i"
            textViewQuantidade.text = "$i"
            textViewDataReserva.text = "0$i/03/2024 - 19h30"

            containerReservas.addView(cardView)
        }

        buttonTerminarSessao = findViewById(R.id.buttonTerminarSessao)
        buttonTerminarSessao.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}