package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val name: String,
    val category: String = "",
    val iconName: String = "activity",
    val goal: String = "Once daily",
    val preferredTime: String = "Anytime",
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
