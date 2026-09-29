package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DayOverviewItem
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardBorderActive
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary

@Composable
fun StatsOverviewCard(
    completionRate: Float,
    currentStreak: Int,
    bestStreak: Int,
    totalCompleted: Int,
    weeklyRate: Int,
    weeklyOverview: List<DayOverviewItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MonoCardSurface)
            .border(1.dp, MonoCardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
            .testTag("stats_overview_card")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PERFORMANCE OVERVIEW",
                color = MonoTextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "7-Day Pulse",
                color = MonoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 4 Key Stats (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                label = "CURRENT STREAK",
                value = "$currentStreak Days",
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "BEST STREAK",
                value = "$bestStreak Days",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                label = "TOTAL LOGS",
                value = "$totalCompleted",
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "WEEKLY RATE",
                value = "$weeklyRate%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7-Day Minimalist Weekly Overview
        Text(
            text = "THIS WEEK",
            color = MonoTextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MonoDarkBackground)
                .border(1.dp, MonoCardBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weeklyOverview.forEach { day ->
                DayIndicator(day = day)
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MonoDarkBackground)
            .border(1.dp, MonoCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = label,
                color = MonoTextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = MonoTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
private fun DayIndicator(day: DayOverviewItem) {
    val isFullyCompleted = day.completionRate >= 1.0f && day.completionRate > 0f
    val isPartiallyCompleted = day.completionRate > 0f && day.completionRate < 1.0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Circular Day node
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isFullyCompleted -> MonoSilver
                        day.isToday -> MonoCardSurfaceElevated
                        else -> MonoCardSurface
                    }
                )
                .border(
                    width = if (day.isToday) 1.5.dp else 1.dp,
                    color = when {
                        day.isToday -> MonoTextPrimary
                        isFullyCompleted -> MonoSilver
                        isPartiallyCompleted -> MonoSilver
                        else -> MonoCardBorder
                    },
                    shape = CircleShape
                )
        ) {
            if (isFullyCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Day Completed",
                    tint = MonoDarkBackground,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isPartiallyCompleted) {
                // Subtle partial ring/dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MonoSilver)
                )
            } else {
                Text(
                    text = day.dayNumber,
                    color = if (day.isToday) MonoTextPrimary else MonoTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Day Name Label (Mon, Tue, etc.)
        Text(
            text = day.dayLabel,
            color = if (day.isToday) MonoTextPrimary else MonoTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
