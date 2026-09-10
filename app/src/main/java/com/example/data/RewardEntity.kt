package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val rewardGems: Int,
    val rewardCoins: Double,
    val isClaimed: Boolean = false,
    val category: String // "DAILY", "QUEST"
)
