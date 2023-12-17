package com.jafar.smarttrash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jafar.smarttrash.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mRoot: DatabaseReference
    private lateinit var mRef: DatabaseReference

    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        userId = mAuth.currentUser?.uid.toString()
        mRoot = FirebaseDatabase.getInstance().reference
        mRef = mRoot.child("users").child(userId)

        mRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val nama = user?.nama.toString()
                val score = user?.score
                getRankingCurrentUser(user) { rank ->
                    val textRank = "#${rank.toString()}"
                    binding.tvRanking.text = textRank
                }
                binding.tvNama.text = nama
                binding.tvJumlahSampah.text = score.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Gagal membaca data dari Firebase", Toast.LENGTH_SHORT).show()
            }

        })

        binding.viewRanking.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

    }

    fun getRankingCurrentUser(currentUser: User?, callback: (Int) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userScores = mutableListOf<User>()

                for (userSnapshot in snapshot.children) {
                    val nis = userSnapshot.child("nis").getValue(String::class.java).toString()
                    val nama = userSnapshot.child("nama").getValue(String::class.java)
                    val score = userSnapshot.child("score").getValue(Int::class.java)

                    if (nama != null && score != null) {
                        userScores.add(User(nis, nama, score))
                    }
                }

                val sortedUserScores = userScores.sortedByDescending { it.score }
                Log.d(TAG, sortedUserScores.toString())
                for ((index, userScore) in sortedUserScores.withIndex()) {
                    if (currentUser?.nis == userScore.nis) {
                        callback(index + 1)
                        return
                    }
                }
                callback(0)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Gagal Menampilkan Ranking", Toast.LENGTH_SHORT).show()
                callback(0)
            }

        })
    }

    companion object {
        private val TAG = HomeActivity::class.java.simpleName
    }
}