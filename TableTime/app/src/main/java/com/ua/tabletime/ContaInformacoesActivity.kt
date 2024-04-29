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
    lateinit var textTitulo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conta_informacoes)


        setContentView(R.layout.activity_conta_informacoes)

        val containerReservas = findViewById<LinearLayout>(R.id.containerReservas)

        for (i in 1..5) {
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
        textTitulo = findViewById(R.id.textTitulo)
        buttonTerminarSessao.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        textTitulo.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }

    }
}