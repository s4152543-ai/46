package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SpinGemsRepository(private val dao: SpinGemsDao) {
    val userFlow: Flow<UserEntity?> = dao.getUser()
    val spinHistoryFlow: Flow<List<SpinHistoryEntity>> = dao.getSpinHistory()
    val rewardsFlow: Flow<List<RewardEntity>> = dao.getAllRewards()
    val withdrawalHistoryFlow: Flow<List<WithdrawalHistoryEntity>> = dao.getWithdrawalHistory()

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun addSpinHistory(history: SpinHistoryEntity) {
        dao.insertSpinHistory(history)
    }

    suspend fun updateReward(reward: RewardEntity) {
        dao.updateReward(reward)
    }

    suspend fun addWithdrawalHistory(withdrawal: WithdrawalHistoryEntity) {
        dao.insertWithdrawalHistory(withdrawal)
    }
}
