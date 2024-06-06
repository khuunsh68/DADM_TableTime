package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CreateAccount : AppCompatActivity() {

    lateinit var inputNome: EditText
    lateinit var buttonCriarConta: Button
    lateinit var inputEmail: EditText
    lateinit var inputPassword: EditText
    lateinit var inputRepeatPassword: EditText
    lateinit var buttonLogin: Button
    lateinit var msgErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)

        inputNome = findViewById(R.id.editTextName)
        inputEmail = findViewById(R.id.editTextEmail2)
        inputPassword = findViewById(R.id.editTextPassword)
        inputRepeatPassword = findViewById(R.id.editTextRepeatPassword)
        buttonCriarConta = findViewById(R.id.buttonCriarConta)
        buttonLogin = findViewById(R.id.buttonLogin)
        msgErro = findViewById(R.id.textViewErro)

        buttonCriarConta.setOnClickListener {
            val nome = inputNome.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val repeatPassword = inputRepeatPassword.text.toString()

            if (nome.isBlank() || email.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
                msgErro.text = "Um ou mais campos vazios!"
                Toast.makeText(this, "Um ou mais campos vazios!", Toast.LENGTH_LONG).show()
            } else if (!isValidEmail(email)) {
                msgErro.text = "Formato de email inválido!"
                Toast.makeText(this, "Formato de email inválido!", Toast.LENGTH_LONG).show()
            } else if (!isValidPassword(password)) {
                msgErro.text = "A senha deve ter pelo menos 8 caracteres!"
                Toast.makeText(this, "A senha deve ter pelo menos 8 caracteres!", Toast.LENGTH_LONG).show()
            } else if (password != repeatPassword) {
                msgErro.text = "As senhas não coincidem!"
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
            } else {
                buttonCriarConta.isEnabled = false
                criarConta(nome, email, password)
            }
        }

        buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun criarConta(nome: String, email: String, password: String) {
        val url = "https://dadm-api.vercel.app/register"
        val jsonObject = JSONObject().apply {
            put("nome", nome)
            put("email", email)
            put("password", password)
        }

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST, url, jsonObject,
            { response ->
                buttonCriarConta.isEnabled = true
                Log.d("CreateAccount", "Response: $response")
                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            },
            { error ->
                buttonCriarConta.isEnabled = true
                Log.e("CreateAccount", "Error: ${error.message}", error)
                val errorMsg = if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> "Erro ao criar conta: parâmetros inválidos ou usuário já existe"
                        else -> "Erro ao criar conta: ${error.message}"
                    }
                } else {
                    "Erro ao criar conta: ${error.message}"
                }
                msgErro.text = errorMsg
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        )
    }
}