package com.jafar.smarttrash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.jafar.smarttrash.databinding.ActivityLeaderboardBinding

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setUpRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.layoutManager = linearLayoutManager
        val itemDecoration = DividerItemDecoration(this, linearLayoutManager.orientation)
        binding.rvLeaderboard.addItemDecoration(itemDecoration)
    }

    private fun setLeaderboardData(users: List<User>) {
        val adapter = LeaderboardAdapter(users)
        binding.rvLeaderboard.adapter = adapter
    }
}