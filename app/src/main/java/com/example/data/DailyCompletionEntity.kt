package com.example.data

import androidx.room.Entity

@Entity(
    tableName = "daily_completions",
    primaryKeys = ["userId", "habitId", "date"]
)
data class DailyCompletionEntity(
    val userId: String = "",
    val habitId: Long,
    val date: String, // Format: YYYY-MM-DD
    val completedAt: Long = System.currentTimeMillis()
)
