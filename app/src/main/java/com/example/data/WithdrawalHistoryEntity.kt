package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawal_history")
data class WithdrawalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val accountHolderName: String,
    val status: String, // "SUCCESS" or "PENDING"
    val timestamp: Long = System.currentTimeMillis()
)
