package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE (:userId = '' OR userId = :userId) ORDER BY id ASC")
    fun getHabitsForUser(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id AND (:userId = '' OR userId = :userId) LIMIT 1")
    suspend fun getHabitByIdAndUser(id: Long, userId: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Query("SELECT COUNT(*) FROM habits WHERE userId = :userId")
    suspend fun countHabitsForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id AND (:userId = '' OR userId = :userId)")
    suspend fun deleteHabitById(id: Long, userId: String = "")

    @Query("SELECT * FROM daily_completions WHERE (:userId = '' OR userId = :userId)")
    fun getCompletionsForUser(userId: String): Flow<List<DailyCompletionEntity>>

    @Query("SELECT * FROM daily_completions")
    fun getAllCompletions(): Flow<List<DailyCompletionEntity>>

    @Query("SELECT * FROM daily_completions WHERE date = :date AND (:userId = '' OR userId = :userId)")
    fun getCompletionsForDate(date: String, userId: String = ""): Flow<List<DailyCompletionEntity>>

    @Query("SELECT * FROM daily_completions WHERE date BETWEEN :startDate AND :endDate AND (:userId = '' OR userId = :userId)")
    fun getCompletionsBetweenDates(startDate: String, endDate: String, userId: String = ""): Flow<List<DailyCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: DailyCompletionEntity)

    @Query("DELETE FROM daily_completions WHERE habitId = :habitId AND date = :date AND (:userId = '' OR userId = :userId)")
    suspend fun deleteCompletion(habitId: Long, date: String, userId: String = "")

    @Query("DELETE FROM daily_completions WHERE date = :date AND (:userId = '' OR userId = :userId)")
    suspend fun deleteCompletionsForDate(date: String, userId: String = "")

    @Query("DELETE FROM daily_completions WHERE habitId = :habitId AND (:userId = '' OR userId = :userId)")
    suspend fun deleteCompletionsForHabit(habitId: Long, userId: String = "")
}
