package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spin_history")
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameName: String,
    val betAmount: Double,
    val winAmount: Double,
    val resultText: String,
    val timestamp: Long = System.currentTimeMillis()
)
