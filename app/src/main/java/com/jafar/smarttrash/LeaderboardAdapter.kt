package com.jafar.smarttrash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jafar.smarttrash.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(private val data: List<User>) : ListAdapter<User, LeaderboardAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val leaderboardUser = getItem(position)
        holder.bind(leaderboardUser, position + 1)
    }

    class ViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, ranking: Int) {
            binding.tvRanking.text = ranking.toString() 
            binding.tvNama.text = user.nama
            val textScore = "${user.score.toString()}pt"
            binding.tvScore.text = textScore
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(
                oldItem: User,
                newItem: User
            ): Boolean {
                return oldItem.nis == newItem.nis
            }

            override fun areContentsTheSame(
                oldItem: User,
                newItem: User
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}