package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.HabitItemUi
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
fun AddEditHabitDialog(
    initialHabit: HabitItemUi? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, icon: String, goal: String, reminder: String) -> Unit
) {
    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var category by remember { mutableStateOf(initialHabit?.category ?: "") }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconName ?: "activity") }
    var goal by remember { mutableStateOf(initialHabit?.goal ?: "Once daily") }
    var preferredTime by remember { mutableStateOf(initialHabit?.preferredTime ?: "Anytime") }
    var nameError by remember { mutableStateOf(false) }

    val categorySuggestions = listOf("Health", "Mind", "Learning", "Routine", "Hobby", "Focus")
    val timeOptions = listOf("Morning", "Afternoon", "Evening", "Anytime")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MonoCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_habit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialHabit == null) "NEW HABIT" else "EDIT HABIT",
                            color = MonoTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Design a quiet daily ritual",
                            color = MonoTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = MonoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Habit Name Field
                Text(
                    text = "HABIT NAME",
                    color = MonoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    placeholder = { Text("e.g. Read 20 Minutes", color = MonoTextTertiary, fontSize = 14.sp) },
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Please enter a habit name", color = MonoSilver, fontSize = 11.sp) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoDarkBackground,
                        unfocusedContainerColor = MonoDarkBackground,
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedBorderColor = MonoSilver,
                        unfocusedBorderColor = MonoCardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Field & Suggestion Chips
                Text(
                    text = "CATEGORY",
                    color = MonoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    placeholder = { Text("e.g. Health, Learning", color = MonoTextTertiary, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoDarkBackground,
                        unfocusedContainerColor = MonoDarkBackground,
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedBorderColor = MonoSilver,
                        unfocusedBorderColor = MonoCardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_category_input")
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Quick chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categorySuggestions) { item ->
                        val isSelected = category.equals(item, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MonoSilver else MonoDarkBackground)
                                .border(1.dp, if (isSelected) MonoSilver else MonoCardBorder, RoundedCornerShape(8.dp))
                                .clickable { category = item }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) MonoDarkBackground else MonoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selector
                Text(
                    text = "ICON",
                    color = MonoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("icon_selector_row")
                ) {
                    items(HabitIconMapper.availableIcons) { iconOption ->
                        val isSelected = selectedIcon.equals(iconOption.id, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MonoSilver else MonoDarkBackground)
                                .border(1.dp, if (isSelected) MonoSilver else MonoCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedIcon = iconOption.id }
                                .testTag("icon_option_${iconOption.id}")
                        ) {
                            Icon(
                                imageVector = iconOption.icon,
                                contentDescription = iconOption.label,
                                tint = if (isSelected) MonoDarkBackground else MonoTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Goal Field
                Text(
                    text = "DAILY GOAL",
                    color = MonoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    placeholder = { Text("e.g. 20 mins, 2 Liters", color = MonoTextTertiary, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoDarkBackground,
                        unfocusedContainerColor = MonoDarkBackground,
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedBorderColor = MonoSilver,
                        unfocusedBorderColor = MonoCardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preferred Time / Reminder
                Text(
                    text = "PREFERRED TIME",
                    color = MonoTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timeOptions.forEach { time ->
                        val isSelected = preferredTime.equals(time, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MonoSilver else MonoDarkBackground)
                                .border(1.dp, if (isSelected) MonoSilver else MonoCardBorder, RoundedCornerShape(10.dp))
                                .clickable { preferredTime = time }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = time,
                                color = if (isSelected) MonoDarkBackground else MonoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_cancel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MonoTextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (name.trim().isBlank()) {
                                nameError = true
                            } else {
                                onSave(
                                    name.trim(),
                                    category.trim(),
                                    selectedIcon,
                                    goal.trim().ifEmpty { "Once daily" },
                                    preferredTime
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonoSilver,
                            contentColor = MonoDarkBackground
                        )
                    ) {
                        Text(
                            text = if (initialHabit == null) "Add Habit" else "Save Changes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
