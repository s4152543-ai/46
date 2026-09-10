package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, SpinHistoryEntity::class, RewardEntity::class, WithdrawalHistoryEntity::class], version = 2, exportSchema = false)
abstract class SpinGemsDatabase : RoomDatabase() {
    abstract fun spinGemsDao(): SpinGemsDao

    companion object {
        @Volatile
        private var INSTANCE: SpinGemsDatabase? = null

        fun getDatabase(context: Context): SpinGemsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpinGemsDatabase::class.java,
                    "spin_gems_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.spinGemsDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: SpinGemsDao) {
                dao.insertUser(UserEntity(id = 1, balance = 1500.00, gems = 350, vipLevel = 1, totalWins = 6200.00, referralCode = "GEMSPIN777"))
                
                dao.insertReward(RewardEntity(1, "Daily Login Bonus", 50, 100.0, false, "DAILY"))
                dao.insertReward(RewardEntity(2, "Spin 5 Times", 100, 250.0, false, "QUEST"))
                dao.insertReward(RewardEntity(3, "Win a Jackpot", 500, 1000.0, false, "QUEST"))
                dao.insertReward(RewardEntity(4, "VIP Tier 2 Unlock", 200, 500.0, false, "QUEST"))
            }
        }
    }
}
