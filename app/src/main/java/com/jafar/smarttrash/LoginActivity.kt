package com.jafar.smarttrash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.jafar.smarttrash.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etLoginPassword.transformationMethod = MyPasswordTransformationMethod()

        binding.btnLogin.setOnClickListener {
            val nis = binding.etLoginNis.text.trim().toString()
            val password = binding.etLoginPassword.text.trim().toString()

            if (nis.isEmpty()) {
                binding.etLoginNis.error = "Masukkan NIS"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etLoginPassword.error = "Masukkan password"
                return@setOnClickListener
            }
            validateUserLogin(nis, password)
        }

        binding.tvLoginKeRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateUserLogin(nis: String, password: String) {
        mAuth = FirebaseAuth.getInstance()
        val email = "$nis@gmail.com"

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                Toast.makeText(this, "Login Gagal. Cek lagi NIS dan Password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}