package com.jafar.smarttrash

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jafar.smarttrash.databinding.ActivityLeaderboardBinding

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private var userListLeaderboard: ArrayList<User>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()

        userListLeaderboard = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(EXTRA_USER_LIST, User::class.java)
        } else {
            intent.getParcelableArrayListExtra(EXTRA_USER_LIST)
        }
        Log.d("data user", userListLeaderboard.toString())

        setUpJuara123()

        userListLeaderboard?.let {
            setLeaderboardData(it)
        }

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setUpRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.layoutManager = linearLayoutManager
        val itemDecoration = DividerItemDecoration(this, linearLayoutManager.orientation)
        binding.rvLeaderboard.addItemDecoration(itemDecoration)
    }

    private fun setLeaderboardData(users: List<User>) {
        val adapter = LeaderboardAdapter(users)
        adapter.submitList(users)
        binding.rvLeaderboard.adapter = adapter
    }

    private fun setUpJuara123() {
        binding.tvNamaJuara1.text = userListLeaderboard?.getOrNull(0)?.nama ?: "Null"
        val poinJuara1 = userListLeaderboard?.getOrNull(0)?.score ?: 0
        binding.tvPoinJuara1.text = "${poinJuara1}pt"

        binding.tvNamaJuara2.text = userListLeaderboard?.getOrNull(1)?.nama ?: "Null"
        val poinJuara2 = userListLeaderboard?.getOrNull(1)?.score ?: 0
        binding.tvPoinJuara2.text = "${poinJuara2}pt"

        binding.tvNamaJuara3.text = userListLeaderboard?.getOrNull(2)?.nama ?: "Null"
        val poinJuara3 = userListLeaderboard?.getOrNull(2)?.score ?: 0
        binding.tvPoinJuara3.text = "${poinJuara3}pt"
    }

    companion object {
        const val EXTRA_USER_LIST = "extra_user_list"
    }
}