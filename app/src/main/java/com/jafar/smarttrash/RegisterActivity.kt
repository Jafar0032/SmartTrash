package com.jafar.smarttrash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jafar.smarttrash.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mRoot: DatabaseReference
    private lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mRoot = mDatabase.reference

        binding.btnRegister.setOnClickListener {
            val nis = binding.etRegisterNis.text.trim().toString()
            val email = "$nis@gmail.com"
            val nama = binding.etRegisterNama.text.trim().toString()
            val password = binding.etRegisterPassword.text.trim().toString()
            val konfirmasi_password = binding.etRegisterKonfirmasiPassword.text.trim().toString()

            // ======== Ketentuan NIS =========== //
            if (nis.isEmpty()) {
                binding.etRegisterNis.error = "Masukkan NIS anda"
                return@setOnClickListener
            }

            if (nis.length !in 9..10) {
                binding.etRegisterNis.error = "Minimal 9 angka dan Maksimal 10 angka"
                return@setOnClickListener
            }

            // ======== Ketentuan Nama =========== //
            if (nama.isEmpty()) {
                binding.etRegisterNama.error = "Masukkan Nama anda"
                return@setOnClickListener
            }

            // ======== Ketentuan Password =========== //
            if (password.isEmpty()) {
                binding.etRegisterPassword.error = "Masukkan Password anda"
                return@setOnClickListener
            }

            if (password.length < 8) {
                binding.etRegisterPassword.error = "Minimal 8 huruf"
                return@setOnClickListener
            }

            if (konfirmasi_password != password) {
                binding.etRegisterKonfirmasiPassword.error = "Password tidak sama"
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val mUser = User(nis, nama)
                    val userId: String = task.result.user?.uid ?: nis
                    mRef = mRoot.child("users").child(userId)
                    mRef.setValue(mUser)
                    val intentToHome = Intent(this, LoginActivity::class.java)
                    startActivity(intentToHome)
                } else {
                    Log.w(TAG, "Create User Error", task.exception)
                    Toast.makeText(this, "Register Failed", Toast.LENGTH_SHORT).show()
                }
            })

        }
    }

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }
}