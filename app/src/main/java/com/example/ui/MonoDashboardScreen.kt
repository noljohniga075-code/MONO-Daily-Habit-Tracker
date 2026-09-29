package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.auth.SupabaseConfigDialog
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HabitCard
import com.example.ui.components.MonoHeader
import com.example.ui.components.ProgressRingCard
import com.example.ui.components.ResetConfirmDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StatsOverviewCard
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary
import androidx.compose.ui.window.Dialog

@Composable
fun MonoDashboardScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitItemUi?>(null) }
    var habitToDelete by remember { mutableStateOf<HabitItemUi?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showSupabaseConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MonoDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MonoDarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    habitToEdit = null
                    showAddDialog = true
                },
                containerColor = MonoSilver,
                contentColor = MonoDarkBackground,
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .testTag("floating_add_habit_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Habit",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    MonoHeader(
                        currentDate = uiState.todayFormattedDate,
                        userName = uiState.userName,
                        userEmail = uiState.userEmail,
                        onResetTodayClicked = { showResetConfirmDialog = true },
                        onSettingsClicked = { showSettingsDialog = true },
                        onLogoutClicked = { showLogoutConfirmDialog = true }
                    )
                }

                // Daily Progress Ring
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        ProgressRingCard(
                            completionRate = uiState.completionRate,
                            completedCount = uiState.completedCount,
                            totalCount = uiState.totalCount
                        )
                    }
                }

                // Statistics & 7-Day Overview
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        StatsOverviewCard(
                            completionRate = uiState.completionRate,
                            currentStreak = uiState.currentStreak,
                            bestStreak = uiState.bestStreak,
                            totalCompleted = uiState.totalCompletionsAllTime,
                            weeklyRate = uiState.weeklyAverageRate,
                            weeklyOverview = uiState.weeklyOverview
                        )
                    }
                }

                // Habit List Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY HABITS",
                                color = MonoTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "${uiState.completedCount} of ${uiState.totalCount} Done",
                                color = MonoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                habitToEdit = null
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoSilver,
                                contentColor = MonoDarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("section_add_habit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Add Habit",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Empty State or List of Habit Cards
                if (uiState.habits.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            EmptyStateView(
                                onAddHabitClicked = {
                                    habitToEdit = null
                                    showAddDialog = true
                                }
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.habits,
                        key = { it.id }
                    ) { habit ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            HabitCard(
                                habit = habit,
                                onToggle = { viewModel.toggleHabit(habit.id) },
                                onEdit = { habitToEdit = habit },
                                onDelete = { habitToDelete = habit }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Modal Dialog
    if (showAddDialog || habitToEdit != null) {
        AddEditHabitDialog(
            initialHabit = habitToEdit,
            onDismiss = {
                showAddDialog = false
                habitToEdit = null
            },
            onSave = { name, category, icon, goal, reminder ->
                val currentEditing = habitToEdit
                if (currentEditing != null) {
                    viewModel.updateHabit(
                        id = currentEditing.id,
                        name = name,
                        category = category,
                        iconName = icon,
                        goal = goal,
                        preferredTime = reminder
                    )
                } else {
                    viewModel.addHabit(
                        name = name,
                        category = category,
                        iconName = icon,
                        goal = goal,
                        preferredTime = reminder
                    )
                }
                showAddDialog = false
                habitToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    habitToDelete?.let { habit ->
        DeleteConfirmDialog(
            habitName = habit.name,
            onConfirm = {
                viewModel.deleteHabit(habit.id)
                habitToDelete = null
            },
            onDismiss = { habitToDelete = null }
        )
    }

    // Reset Today Confirmation Dialog
    if (showResetConfirmDialog) {
        ResetConfirmDialog(
            onConfirm = {
                viewModel.resetTodayProgress()
                showResetConfirmDialog = false
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }

    // Settings / About Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            user = currentUser,
            isLiveSupabase = viewModel.supabaseClient.isConfiguredWithLiveCredentials(),
            onConfigureSupabase = {
                showSettingsDialog = false
                showSupabaseConfigDialog = true
            },
            onSyncNow = {
                viewModel.syncWithSupabaseNow()
            },
            onLogout = {
                showSettingsDialog = false
                showLogoutConfirmDialog = true
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        Dialog(onDismissRequest = { showLogoutConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MonoCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .testTag("logout_confirm_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "SIGN OUT",
                        color = MonoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Are you sure you want to sign out? Your habits and streaks are permanently preserved in Supabase and will be restored when you log back in.",
                        color = MonoTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showLogoutConfirmDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoDarkBackground,
                                contentColor = MonoTextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MonoCardBorder, RoundedCornerShape(10.dp))
                        ) {
                            Text("Cancel", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showLogoutConfirmDialog = false
                                viewModel.signOut()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoSilver,
                                contentColor = MonoDarkBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_logout_button")
                        ) {
                            Text("Sign Out", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Supabase Configuration Dialog
    if (showSupabaseConfigDialog) {
        SupabaseConfigDialog(
            supabaseClient = viewModel.supabaseClient,
            onDismiss = { showSupabaseConfigDialog = false }
        )
    }
}

