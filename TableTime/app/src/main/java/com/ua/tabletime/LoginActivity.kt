package com.ua.tabletime

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
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

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonCriarConta = findViewById(R.id.buttonCriarConta)
        inputEmail = findViewById(R.id.editTextEmail)
        inputPassword = findViewById(R.id.editTextPassword)
        msgErro = findViewById(R.id.textViewErro)

        buttonLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            if (email.isBlank()) {
                showErrorDialog("Campo de email vazio!")
            } else if (password.isBlank()) {
                showErrorDialog("Campo de password vazio!")
            } else if (!isValidEmail(email)) {
                showErrorDialog("Formato de email inválido!")
            } else if (!isValidPassword(password)) {
                showErrorDialog("A senha deve ter pelo menos 8 caracteres!")
            } else {
                fazerLogin(email, password)
            }
        }

        buttonCriarConta.setOnClickListener {
            startActivity(Intent(this, CreateAccount::class.java))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun fazerLogin(email: String, password: String) {
        buttonLogin.isEnabled = false

        val url = "https://dadm-api.vercel.app/login"
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        NetworkUtils.sendJsonObjectRequest(
            this,
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                buttonLogin.isEnabled = true
                Toast.makeText(this, "Login feito com sucesso!", Toast.LENGTH_LONG).show()

                val token = response.getString("token")
                val userId = response.getInt("id")
                val name = response.getString("nome")

                Log.d("nome", name)

                val sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("jwt_token", token)
                editor.putInt("user_id", userId)
                editor.putString("name", name)
                editor.apply()

                startActivity(Intent(this, HomepageActivity::class.java))
                finish()
            },
            { error ->
                buttonLogin.isEnabled = true
                val errorMsg = when (error.networkResponse?.statusCode) {
                    400 -> "Erro ao fazer login: parâmetros inválidos"
                    401 -> "Erro ao fazer login: credenciais incorretas"
                    404 -> "Erro ao fazer login: verifique suas credenciais"
                    else -> "Erro ao fazer login: ${error.message}"
                }
                showErrorDialog(errorMsg)
                msgErro.text = errorMsg
            }
        )
    }

    private fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Erro de Login")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}