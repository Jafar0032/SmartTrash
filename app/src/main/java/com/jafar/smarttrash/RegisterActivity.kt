package com.jafar.smarttrash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
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
            val nis = binding.etRegisterNis.text.toString()
            val nama = binding.etRegisterNama.text.toString()
            val password = binding.etRegisterPassword.text.toString()
            val konfirmasi_password = binding.etRegisterKonfirmasiPassword.text.toString()

            // ======== Ketentuan NIS =========== /
            // 1. Tidak Boleh Kosong
            if (nis.isEmpty()) {
                binding.etRegisterNis.error = "Masukkan NIS anda"
                return@setOnClickListener
            }

            // 2. Harus 9-10 Angka
            if (nis.length in 9..10) {
                binding.etRegisterNis.error = "Minimal 9 angka dan Maksimal 10 angka"
            }


            if (nama.isEmpty()) {
                binding.etRegisterNama.error = "Masukkan Nama anda"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etRegisterPassword.error = "Masukkan Password anda"
            }

        }
    }
}