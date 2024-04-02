package com.ua.tabletime

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    lateinit var buttonLogin: Button
    lateinit var buttonCriarConta: Button
    lateinit var inputEmail: EditText
    lateinit var inputPassword: EditText
    lateinit var msgErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        buttonLogin = findViewById(R.id.buttonCriarConta)
        buttonCriarConta = findViewById(R.id.buttonLogin)
        inputEmail = findViewById(R.id.editTextEmail)
        inputPassword = findViewById(R.id.editTextPassword)
        msgErro = findViewById(R.id.textViewErro)

        buttonLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            println(email)
            println(password)

            if (email.isBlank()) {
                msgErro.text = "Campo de email vazio!"
                Toast.makeText(applicationContext, "Campo de email vazio!", Toast.LENGTH_LONG).show()
            } else if (password.isBlank()) {
                msgErro.text = "Campo de password vazio!"
                Toast.makeText(applicationContext, "Campo de password vazio!", Toast.LENGTH_LONG).show()
            } else if (!isValidEmail(email)) {
                msgErro.text = "Formato de email inválido!"
                Toast.makeText(applicationContext, "Formato de email inválido!", Toast.LENGTH_LONG).show()
            } else if (!isValidPassword(password)) {
                msgErro.text = "A senha deve ter pelo menos 8 caracteres!"
                Toast.makeText(applicationContext, "A senha deve ter pelo menos 8 caracteres!", Toast.LENGTH_LONG).show()
            } else {
                //LOGIN OK, ir para a página inicial
                startActivity(Intent(this, HomepageActivity::class.java))
            }
        }

        buttonLogin.setOnClickListener {
            val fragment = CreateAccountFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(android.R.id.content, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}