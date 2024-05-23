package com.ua.tabletime

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    lateinit var buttonLogin: Button
    lateinit var buttonCriarConta: Button
    lateinit var inputEmail: EditText
    lateinit var inputPassword: EditText
    lateinit var msgErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializando as views
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonCriarConta = findViewById(R.id.buttonCriarConta)
        inputEmail = findViewById(R.id.editTextEmail)
        inputPassword = findViewById(R.id.editTextPassword)
        msgErro = findViewById(R.id.textViewErro)

        // Configurando o OnClickListener para o botão de login
        buttonLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

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
                buttonLogin.isEnabled = false // Desativa o botão para evitar múltiplas requisições
                fazerLogin(email, password)
            }
        }

        buttonCriarConta.setOnClickListener {
            startActivity(Intent(this, CreateAccount::class.java))
        }
    }

    // Funções auxiliares para validar email e senha
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun fazerLogin(email: String, password: String) {
        val url = "https://dadm-api.vercel.app/login"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                buttonLogin.isEnabled = true // Reativa o botão
                Toast.makeText(this, "Login feito com sucesso!", Toast.LENGTH_LONG).show()

                val token = response.getString("token")
                val userId = response.getInt("id")
                val userName = response.getString("nome")

                val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("jwt_token", token)
                editor.putInt("user_id", userId)
                editor.putString("userName", userName)
                editor.apply()

                startActivity(Intent(this, HomepageActivity::class.java))
                finish() // Finaliza a atividade para evitar voltar a ela com o botão Voltar
            },
            { error ->
                buttonLogin.isEnabled = true // Reativa o botão
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Erro ao fazer login: parâmetros inválidos"
                    404 -> "Erro ao fazer login: verifique suas credenciais"
                    else -> "Erro ao fazer login: ${error.message}"
                }
                msgErro.text = errorMsg
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}