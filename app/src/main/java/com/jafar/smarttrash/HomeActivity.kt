package com.jafar.smarttrash

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ResourceCursorAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
    private lateinit var mRoot: DatabaseReference
    private lateinit var mRef: DatabaseReference

    private lateinit var userId: String
    private var scoreAddUp: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        // Jika user belum login, maka langsung diarahkan ke halaman login
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = mAuth.currentUser?.uid.toString()
        mRoot = FirebaseDatabase.getInstance().reference
        mRef = mRoot.child("users").child(userId)

        showProgressBar(true)

        mRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val nama = user?.nama.toString()
                val score = user?.score
                getRankingCurrentUser(user) { rank ->
                    val textRank = "#$rank"
                    binding.tvRanking.text = textRank
                    binding.tvNama.text = nama
                    binding.tvJumlahSampah.text = score.toString()
                    showProgressBar(false)
                    val scoreAddUpString = intent.getStringExtra(PreviewActivity.EXTRA_SAMPAH)
                    if (scoreAddUpString != null) {
                        // Start animation
                        scoreAddUp = scoreAddUpString.toInt()
                        startTeksAnimation(scoreAddUp)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressBar(false)
                Toast.makeText(this@HomeActivity, "Gagal membaca data dari Firebase", Toast.LENGTH_SHORT).show()
            }

        })

        binding.viewRanking.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        binding.ivLogout.setOnClickListener {
            showDialog()
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

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.bts_logout)

        val btnLogout = dialog.findViewById(R.id.btn_logout) as Button
        val btnCancel = dialog.findViewById(R.id.btn_cancel) as Button

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()
    }

    private fun startTeksAnimation(scoreAddUp: Int) {
        val textScore = androidx.appcompat.widget.AppCompatTextView(this)
        val showScore = "+$scoreAddUp"
        textScore.text = showScore
        textScore.textSize = 24f
        textScore.elevation = 4f
        textScore.translationZ = 4f

        val montserrat_bold = ResourcesCompat.getFont(this, R.font.montserrat_bold)
        textScore.typeface = montserrat_bold

        binding.root.addView(textScore)

        val referenceLocation = IntArray(2)
        binding.tvJumlahSampah.getLocationOnScreen(referenceLocation)

        textScore.x = referenceLocation[0].toFloat()
        textScore.y = referenceLocation[1].toFloat() - textScore.height

        val deltaY = textScore.y + textScore.height.toFloat()

        val animation = TranslateAnimation(
            0f, 0f, -150f, -deltaY + 300f
        )
        animation.duration = 1000
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                // Hapus teks setelah animasi selesai
                binding.root.removeView(textScore)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })
        textScore.startAnimation(animation)
    }

    fun showProgressBar(bool: Boolean) {
        if (bool) { // Jika sedang loading
            binding.animProgress.visibility = View.VISIBLE
            binding.frameOverlay.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            binding.frameOverlay.visibility = View.GONE
            binding.animProgress.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    companion object {
        private val TAG = HomeActivity::class.java.simpleName
    }
}