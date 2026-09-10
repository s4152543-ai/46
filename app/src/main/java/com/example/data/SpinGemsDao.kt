package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpinGemsDao {
    @Query("SELECT * FROM user_table WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM spin_history ORDER BY timestamp DESC")
    fun getSpinHistory(): Flow<List<SpinHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpinHistory(history: SpinHistoryEntity)

    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<RewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)

    @Update
    suspend fun updateReward(reward: RewardEntity)

    @Query("SELECT * FROM withdrawal_history ORDER BY timestamp DESC")
    fun getWithdrawalHistory(): Flow<List<WithdrawalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawalHistory(withdrawal: WithdrawalHistoryEntity)
}
