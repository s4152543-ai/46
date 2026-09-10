package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 1250.70,
    val gems: Int = 250,
    val vipLevel: Int = 1,
    val totalWins: Double = 5400.00,
    val referralCode: String = "GEMSPIN777"
)
