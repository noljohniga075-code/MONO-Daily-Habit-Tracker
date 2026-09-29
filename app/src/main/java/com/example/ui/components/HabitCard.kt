package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HabitItemUi
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardBorderActive
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoCompletedBg
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary

@Composable
fun HabitCard(
    habit: HabitItemUi,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = habit.isCompletedToday

    val cardBgColor by animateColorAsState(
        targetValue = if (isCompleted) MonoCompletedBg else MonoCardSurface,
        label = "card_bg_color"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = if (isCompleted) MonoCardBorder else MonoCardBorderActive,
        label = "card_border_color"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "check_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
            .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(14.dp)
            .testTag("habit_card_${habit.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checkbox & Habit details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Minimalist Luxury Checkbox
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCompleted) MonoSilver else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (isCompleted) MonoSilver else MonoCardBorderActive,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggle() }
                        .testTag("habit_checkbox_${habit.id}")
                ) {
                    if (isCompleted || checkScale > 0.05f) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = MonoDarkBackground,
                            modifier = Modifier
                                .size(16.dp)
                                .scale(checkScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Line Icon Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MonoDarkBackground)
                        .border(1.dp, MonoCardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = HabitIconMapper.getIcon(habit.iconName),
                        contentDescription = habit.name,
                        tint = if (isCompleted) MonoTextSecondary else MonoSilver,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Details Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        color = if (isCompleted) MonoTextSecondary else MonoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (habit.category.isNotBlank()) {
                            Text(
                                text = habit.category,
                                color = MonoTextTertiary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "•",
                                color = MonoTextTertiary,
                                fontSize = 10.sp
                            )
                        }

                        // Streak Indicator Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = if (habit.streak > 0) MonoSilver else MonoTextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${habit.streak}d",
                                color = if (habit.streak > 0) MonoTextPrimary else MonoTextTertiary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (habit.goal.isNotBlank()) {
                            Text(
                                text = "•",
                                color = MonoTextTertiary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = habit.goal,
                                color = MonoTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Actions: Edit & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("habit_edit_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Habit",
                        tint = MonoTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("habit_delete_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete Habit",
                        tint = MonoTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
