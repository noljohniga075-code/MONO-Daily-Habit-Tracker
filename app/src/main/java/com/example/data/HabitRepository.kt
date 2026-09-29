package com.example.data

import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabaseHabitRemote
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale

class HabitRepository(
    private val habitDao: HabitDao,
    private val supabaseClient: SupabaseClient? = null
) {

    fun getHabitsFlow(userId: String): Flow<List<HabitEntity>> {
        return habitDao.getHabitsForUser(userId)
    }

    fun getCompletionsFlow(userId: String): Flow<List<DailyCompletionEntity>> {
        return habitDao.getCompletionsForUser(userId)
    }

    // Default backward compatible flows
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<DailyCompletionEntity>> = habitDao.getAllCompletions()

    suspend fun ensureInitialHabitsForUser(userId: String) {
        val count = habitDao.countHabitsForUser(userId)
        if (count == 0) {
            val starterHabits = listOf(
                HabitEntity(
                    userId = userId,
                    name = "Morning Exercise",
                    category = "Health",
                    iconName = "activity",
                    goal = "30 mins",
                    preferredTime = "Morning",
                    streak = 3,
                    bestStreak = 5,
                    lastCompletedDate = ""
                ),
                HabitEntity(
                    userId = userId,
                    name = "Read 20 Minutes",
                    category = "Mind",
                    iconName = "book",
                    goal = "20 mins",
                    preferredTime = "Evening",
                    streak = 7,
                    bestStreak = 12,
                    lastCompletedDate = ""
                ),
                HabitEntity(
                    userId = userId,
                    name = "Drink Water",
                    category = "Health",
                    iconName = "water",
                    goal = "2.5 Liters",
                    preferredTime = "Anytime",
                    streak = 14,
                    bestStreak = 14,
                    lastCompletedDate = ""
                ),
                HabitEntity(
                    userId = userId,
                    name = "Study Electronics",
                    category = "Learning",
                    iconName = "study",
                    goal = "45 mins",
                    preferredTime = "Afternoon",
                    streak = 2,
                    bestStreak = 4,
                    lastCompletedDate = ""
                ),
                HabitEntity(
                    userId = userId,
                    name = "Practice Guitar",
                    category = "Hobby",
                    iconName = "guitar",
                    goal = "25 mins",
                    preferredTime = "Evening",
                    streak = 5,
                    bestStreak = 9,
                    lastCompletedDate = ""
                )
            )
            habitDao.insertHabits(starterHabits)

            // Push to Supabase if configured
            supabaseClient?.let { client ->
                if (client.isConfiguredWithLiveCredentials()) {
                    starterHabits.forEach { habit ->
                        client.pushHabitToRemote(
                            SupabaseHabitRemote(
                                user_id = userId,
                                name = habit.name,
                                category = habit.category,
                                icon_name = habit.iconName,
                                goal = habit.goal,
                                preferred_time = habit.preferredTime,
                                streak = habit.streak,
                                best_streak = habit.bestStreak,
                                last_completed_date = habit.lastCompletedDate
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun syncWithSupabase(userId: String) {
        supabaseClient?.let { client ->
            if (client.isConfiguredWithLiveCredentials() && userId.isNotEmpty()) {
                val remoteHabits = client.fetchRemoteHabits(userId)
                if (remoteHabits.isNotEmpty()) {
                    val entities = remoteHabits.map { remote ->
                        HabitEntity(
                            id = remote.id ?: 0,
                            userId = userId,
                            name = remote.name,
                            category = remote.category,
                            iconName = remote.icon_name,
                            goal = remote.goal,
                            preferredTime = remote.preferred_time,
                            streak = remote.streak,
                            bestStreak = remote.best_streak,
                            lastCompletedDate = remote.last_completed_date
                        )
                    }
                    habitDao.insertHabits(entities)
                }
            }
        }
    }

    suspend fun addHabit(
        userId: String,
        name: String,
        category: String,
        iconName: String,
        goal: String,
        preferredTime: String
    ): Long {
        val habit = HabitEntity(
            userId = userId,
            name = name,
            category = category,
            iconName = iconName,
            goal = goal,
            preferredTime = preferredTime,
            streak = 0,
            bestStreak = 0,
            lastCompletedDate = ""
        )
        val generatedId = habitDao.insertHabit(habit)

        supabaseClient?.pushHabitToRemote(
            SupabaseHabitRemote(
                id = generatedId,
                user_id = userId,
                name = name,
                category = category,
                icon_name = iconName,
                goal = goal,
                preferred_time = preferredTime,
                streak = 0,
                best_streak = 0,
                last_completed_date = ""
            )
        )

        return generatedId
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)

        supabaseClient?.pushHabitToRemote(
            SupabaseHabitRemote(
                id = habit.id,
                user_id = habit.userId,
                name = habit.name,
                category = habit.category,
                icon_name = habit.iconName,
                goal = habit.goal,
                preferred_time = habit.preferredTime,
                streak = habit.streak,
                best_streak = habit.bestStreak,
                last_completed_date = habit.lastCompletedDate
            )
        )
    }

    suspend fun deleteHabit(habitId: Long, userId: String) {
        habitDao.deleteCompletionsForHabit(habitId, userId)
        habitDao.deleteHabitById(habitId, userId)

        supabaseClient?.deleteHabitFromRemote(habitId, userId)
    }

    suspend fun toggleHabitCompletion(
        habitId: Long,
        date: String,
        isCurrentlyCompleted: Boolean,
        userId: String
    ) {
        val habit = habitDao.getHabitByIdAndUser(habitId, userId) ?: habitDao.getHabitById(habitId) ?: return

        if (isCurrentlyCompleted) {
            // Unmark completion
            habitDao.deleteCompletion(habitId, date, userId)
            val newStreak = (habit.streak - 1).coerceAtLeast(0)
            val updated = habit.copy(streak = newStreak)
            habitDao.updateHabit(updated)
            supabaseClient?.pushHabitToRemote(
                SupabaseHabitRemote(
                    id = updated.id,
                    user_id = userId,
                    name = updated.name,
                    category = updated.category,
                    icon_name = updated.iconName,
                    goal = updated.goal,
                    preferred_time = updated.preferredTime,
                    streak = updated.streak,
                    best_streak = updated.bestStreak,
                    last_completed_date = updated.lastCompletedDate
                )
            )
        } else {
            // Mark as completed
            habitDao.insertCompletion(
                DailyCompletionEntity(
                    userId = userId,
                    habitId = habitId,
                    date = date
                )
            )

            // Streak calculation
            val newStreak = if (habit.lastCompletedDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val last = sdf.parse(habit.lastCompletedDate)
                    val current = sdf.parse(date)
                    val diff = if (last != null && current != null) {
                        (current.time - last.time) / (1000 * 60 * 60 * 24)
                    } else 0
                    if (diff <= 1) habit.streak + 1 else 1
                } catch (e: Exception) {
                    habit.streak + 1
                }
            } else {
                habit.streak + 1
            }

            val newBestStreak = maxOf(habit.bestStreak, newStreak)
            val updated = habit.copy(
                streak = newStreak,
                bestStreak = newBestStreak,
                lastCompletedDate = date
            )
            habitDao.updateHabit(updated)
            supabaseClient?.pushHabitToRemote(
                SupabaseHabitRemote(
                    id = updated.id,
                    user_id = userId,
                    name = updated.name,
                    category = updated.category,
                    icon_name = updated.iconName,
                    goal = updated.goal,
                    preferred_time = updated.preferredTime,
                    streak = updated.streak,
                    best_streak = updated.bestStreak,
                    last_completed_date = updated.lastCompletedDate
                )
            )
        }
    }

    suspend fun resetTodayProgress(date: String, completedHabitIds: List<Long>, userId: String) {
        for (id in completedHabitIds) {
            val habit = habitDao.getHabitByIdAndUser(id, userId) ?: habitDao.getHabitById(id)
            if (habit != null) {
                val newStreak = (habit.streak - 1).coerceAtLeast(0)
                val updated = habit.copy(streak = newStreak)
                habitDao.updateHabit(updated)
                supabaseClient?.pushHabitToRemote(
                    SupabaseHabitRemote(
                        id = updated.id,
                        user_id = userId,
                        name = updated.name,
                        category = updated.category,
                        icon_name = updated.iconName,
                        goal = updated.goal,
                        preferred_time = updated.preferredTime,
                        streak = updated.streak,
                        best_streak = updated.bestStreak,
                        last_completed_date = updated.lastCompletedDate
                    )
                )
            }
        }
        habitDao.deleteCompletionsForDate(date, userId)
    }
}
