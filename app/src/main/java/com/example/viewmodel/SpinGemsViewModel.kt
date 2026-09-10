package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SpinGemsDatabase
import com.example.data.SpinGemsRepository
import com.example.data.SpinHistoryEntity
import com.example.data.UserEntity
import com.example.data.RewardEntity
import com.example.data.WithdrawalHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpinGemsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SpinGemsRepository

    val user: StateFlow<UserEntity?>
    val spinHistory: StateFlow<List<SpinHistoryEntity>>
    val rewards: StateFlow<List<RewardEntity>>
    val withdrawalHistory: StateFlow<List<WithdrawalHistoryEntity>>

    init {
        val dao = SpinGemsDatabase.getDatabase(application).spinGemsDao()
        repository = SpinGemsRepository(dao)

        user = repository.userFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserEntity()
        )

        spinHistory = repository.spinHistoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        rewards = repository.rewardsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        withdrawalHistory = repository.withdrawalHistoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun spinGame(gameName: String, betAmount: Double, isGemBet: Boolean, onResult: (Boolean, Double, String) -> Unit) {
        viewModelScope.launch {
            val currentUser = user.value ?: UserEntity()
            if (isGemBet) {
                if (currentUser.gems < betAmount.toInt()) {
                    onResult(false, 0.0, "Not enough gems!")
                    return@launch
                }
            } else {
                if (currentUser.balance < betAmount) {
                    onResult(false, 0.0, "Not enough balance!")
                    return@launch
                }
            }

            // Deduct bet
            val updatedUser = if (isGemBet) {
                currentUser.copy(gems = currentUser.gems - betAmount.toInt())
            } else {
                currentUser.copy(balance = currentUser.balance - betAmount)
            }
            repository.updateUser(updatedUser)

            // Calculate win (random multiplier: 0x, 1x, 2x, 5x, 10x, 50x Jackpot)
            val multipliers = listOf(0.0, 0.5, 1.0, 2.0, 5.0, 10.0, 25.0, 50.0)
            val weights = listOf(35, 20, 20, 15, 6, 2, 1, 1) // probability distribution
            
            val totalWeight = weights.sum()
            var randomVal = (0 until totalWeight).random()
            var selectedMultiplier = 0.0
            for (i in multipliers.indices) {
                randomVal -= weights[i]
                if (randomVal < 0) {
                    selectedMultiplier = multipliers[i]
                    break
                }
            }

            val winAmount = betAmount * selectedMultiplier
            val isWin = winAmount > 0
            val resultText = if (isWin) "Won ${String.format("%.2f", winAmount)} (${selectedMultiplier}x)!" else "No win, try again!"

            val finalUser = if (isWin) {
                if (isGemBet) {
                    updatedUser.copy(
                        gems = updatedUser.gems + winAmount.toInt(),
                        totalWins = updatedUser.totalWins + winAmount
                    )
                } else {
                    updatedUser.copy(
                        balance = updatedUser.balance + winAmount,
                        totalWins = updatedUser.totalWins + winAmount
                    )
                }
            } else {
                updatedUser
            }
            repository.updateUser(finalUser)

            repository.addSpinHistory(
                SpinHistoryEntity(
                    gameName = gameName,
                    betAmount = betAmount,
                    winAmount = winAmount,
                    resultText = resultText
                )
            )

            onResult(isWin, winAmount, resultText)
        }
    }

    fun deposit(amount: Double) {
        viewModelScope.launch {
            val currentUser = user.value ?: UserEntity()
            val updated = currentUser.copy(balance = currentUser.balance + amount)
            repository.updateUser(updated)
        }
    }

    fun withdraw(amount: Double, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentUser = user.value ?: UserEntity()
            if (currentUser.balance >= amount) {
                val updated = currentUser.copy(balance = currentUser.balance - amount)
                repository.updateUser(updated)
                onComplete(true, "Successfully withdrew ${String.format("%.2f", amount)}")
            } else {
                onComplete(false, "Insufficient balance for withdrawal.")
            }
        }
    }

    fun withdrawToBank(bankName: String, accountNumber: String, ifscCode: String, accountHolderName: String, amount: Double, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentUser = user.value ?: UserEntity()
            if (bankName.isBlank() || accountNumber.isBlank() || ifscCode.isBlank() || accountHolderName.isBlank()) {
                onComplete(false, "Please fill in all bank account details.")
                return@launch
            }
            if (amount <= 0) {
                onComplete(false, "Please enter a valid withdrawal amount.")
                return@launch
            }
            if (currentUser.balance >= amount) {
                val updated = currentUser.copy(balance = currentUser.balance - amount)
                repository.updateUser(updated)
                repository.addWithdrawalHistory(
                    WithdrawalHistoryEntity(
                        amount = amount,
                        bankName = bankName,
                        accountNumber = accountNumber,
                        ifscCode = ifscCode,
                        accountHolderName = accountHolderName,
                        status = "SUCCESS"
                    )
                )
                onComplete(true, "Successfully withdrew ₹${String.format("%.2f", amount)} to $bankName!")
            } else {
                onComplete(false, "Insufficient balance for withdrawal.")
            }
        }
    }

    fun claimReward(reward: RewardEntity) {
        viewModelScope.launch {
            val currentUser = user.value ?: UserEntity()
            val updatedUser = currentUser.copy(
                gems = currentUser.gems + reward.rewardGems,
                balance = currentUser.balance + reward.rewardCoins
            )
            repository.updateUser(updatedUser)
            repository.updateReward(reward.copy(isClaimed = true))
        }
    }
}
