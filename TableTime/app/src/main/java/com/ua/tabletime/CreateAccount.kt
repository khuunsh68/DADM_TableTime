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
import com.android.volley.Request
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

        // Inicialize as views
        inputNome = findViewById(R.id.editTextName)
        inputEmail = findViewById(R.id.editTextEmail2)
        inputPassword = findViewById(R.id.editTextPassword)
        inputRepeatPassword = findViewById(R.id.editTextRepeatPassword)
        buttonCriarConta = findViewById(R.id.buttonCriarConta)
        buttonLogin = findViewById(R.id.buttonLogin)
        msgErro = findViewById(R.id.textViewErro)

        // Configuração de onClickListener para o botão de criar conta
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
                buttonCriarConta.isEnabled = false // Desativa o botão para evitar múltiplas requisições
                criarConta(nome, email, password)
            }
        }

        // Configuração de onClickListener para o botão de login
        buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finalize esta atividade para evitar voltar para ela pressionando o botão Voltar
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

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("nome", nome)
            jsonObject.put("email", email)
            jsonObject.put("password", password)

            Log.d("CreateAccount", "JSON to be sent: $jsonObject")  // Adiciona log do JSON

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, jsonObject,
                { response ->
                    buttonCriarConta.isEnabled = true // Reativa o botão
                    Log.d("CreateAccount", "Response: $response")  // Adiciona log da resposta
                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, HomepageActivity::class.java))
                    finish() // Finaliza a atividade para evitar voltar a ela com o botão Voltar
                },
                { error ->
                    buttonCriarConta.isEnabled = true // Reativa o botão
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

            requestQueue.add(jsonObjectRequest)
        } catch (e: Exception) {
            buttonCriarConta.isEnabled = true
            Log.e("CreateAccount", "Error creating JSON object: ${e.message}", e)
            Toast.makeText(this, "Erro ao criar JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}