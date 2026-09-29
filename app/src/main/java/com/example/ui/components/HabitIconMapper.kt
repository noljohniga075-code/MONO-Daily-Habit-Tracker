package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

object HabitIconMapper {
    val availableIcons = listOf(
        HabitIconOption("activity", "Exercise", Icons.Outlined.DirectionsRun),
        HabitIconOption("fitness", "Gym", Icons.Outlined.FitnessCenter),
        HabitIconOption("book", "Read", Icons.Outlined.MenuBook),
        HabitIconOption("water", "Water", Icons.Outlined.WaterDrop),
        HabitIconOption("study", "Study", Icons.Outlined.Memory),
        HabitIconOption("mind", "Mind", Icons.Outlined.Psychology),
        HabitIconOption("meditation", "Zen", Icons.Outlined.SelfImprovement),
        HabitIconOption("guitar", "Music", Icons.Outlined.MusicNote),
        HabitIconOption("sun", "Morning", Icons.Outlined.WbSunny),
        HabitIconOption("moon", "Evening", Icons.Outlined.Nightlight),
        HabitIconOption("coffee", "Focus", Icons.Outlined.Coffee),
        HabitIconOption("health", "Health", Icons.Outlined.FavoriteBorder),
        HabitIconOption("energy", "Energy", Icons.Outlined.Bolt)
    )

    fun getIcon(name: String): ImageVector {
        return availableIcons.firstOrNull { it.id.equals(name, ignoreCase = true) }?.icon
            ?: Icons.Outlined.DirectionsRun
    }
}
