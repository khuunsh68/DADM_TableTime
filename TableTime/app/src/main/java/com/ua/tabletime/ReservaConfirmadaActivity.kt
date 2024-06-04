package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReservaConfirmadaActivity : AppCompatActivity() {

    lateinit var btnVoltarHomepage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reserva_confirmada)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        btnVoltarHomepage = findViewById(R.id.buttonVoltarHomepage)

        btnVoltarHomepage.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
            //TEM QUE SE DAR finish() AQUI?
        }

    }
}