package com.ua.tabletime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateAccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var inputNome: EditText
    lateinit var buttonCriarConta: Button
    lateinit var inputEmail: EditText
    lateinit var inputPassword: EditText
    lateinit var inputRepeatPassword: EditText
    lateinit var buttonLogin: Button
    lateinit var msgErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_account, container, false)

        buttonCriarConta = view.findViewById(R.id.buttonCriarConta)
        inputNome = view.findViewById(R.id.editTextName)
        inputEmail = view.findViewById(R.id.editTextEmail2)
        inputPassword = view.findViewById(R.id.editTextPassword)
        inputRepeatPassword = view.findViewById(R.id.editTextRepeatPassword)
        msgErro = view.findViewById(R.id.textViewErro)

        buttonCriarConta.setOnClickListener {
            val nome = inputNome.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val repeatPassword = inputRepeatPassword.text.toString()

            Log.d("CreateAccountFragment", "Nome: $nome")
            Log.d("CreateAccountFragment", "Email: $email")
            Log.d("CreateAccountFragment", "Password: $password")
            Log.d("CreateAccountFragment", "RepeatPassword: $repeatPassword")

            if (nome.isBlank() || email.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
                msgErro.text = "Um ou mais campos vazios!"
                Toast.makeText(requireContext(), "Um ou mais campos vazios!", Toast.LENGTH_LONG)
                    .show()
            } else if (!isValidEmail(email)) {
                msgErro.text = "Formato de email inválido!"
                Toast.makeText(requireContext(), "Formato de email inválido!", Toast.LENGTH_LONG)
                    .show()
            } else if (!isValidPassword(password)) {
                msgErro.text = "A password deve ter pelo menos 8 caracteres!"
                Toast.makeText(
                    requireContext(),
                    "A password deve ter pelo menos 8 caracteres!",
                    Toast.LENGTH_LONG
                ).show()
            } else if (password != repeatPassword) {
                msgErro.text = "Passwords diferentes!"
                Toast.makeText(requireContext(), "Passwords diferentes!", Toast.LENGTH_LONG).show()
            } else {
                // LOGIN OK, ir para a página inicial
            }
        }

        buttonLogin.setOnClickListener {
            //IR PARA PÁGINA DE LOGIN
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateAccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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