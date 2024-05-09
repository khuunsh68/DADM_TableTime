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
                // Criação da conta
                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()
                // Adicione a lógica para ir para a próxima atividade após criar a conta
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
}
