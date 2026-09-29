package com.example.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyCompletionEntity
import com.example.data.HabitEntity
import com.example.data.HabitRepository
import com.example.data.supabase.AuthResult
import com.example.data.supabase.AuthState
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HabitItemUi(
    val id: Long,
    val name: String,
    val category: String,
    val iconName: String,
    val goal: String,
    val preferredTime: String,
    val streak: Int,
    val bestStreak: Int,
    val isCompletedToday: Boolean
)

data class DayOverviewItem(
    val dateString: String,
    val dayLabel: String,       // e.g. "Mon"
    val dayNumber: String,      // e.g. "18"
    val completionRate: Float,  // 0.0f to 1.0f
    val isToday: Boolean,
    val isPast: Boolean
)

data class HabitUiState(
    val habits: List<HabitItemUi> = emptyList(),
    val todayFormattedDate: String = "",
    val todayDateString: String = "",
    val completionRate: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletionsAllTime: Int = 0,
    val weeklyOverview: List<DayOverviewItem> = emptyList(),
    val weeklyAverageRate: Int = 0,
    val isLoading: Boolean = false,
    val userEmail: String = "",
    val userName: String = ""
)

class HabitViewModel(
    private val repository: HabitRepository,
    val supabaseClient: SupabaseClient
) : ViewModel() {

    private val todayDateString = getTodayDate()
    private val todayFormatted = getHeaderDate()

    val authState: StateFlow<AuthState> = supabaseClient.authState
    val currentUser: StateFlow<SupabaseUser?> = supabaseClient.currentUser

    private val _authActionLoading = MutableStateFlow(false)
    val authActionLoading: StateFlow<Boolean> = _authActionLoading.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HabitUiState> = currentUser.flatMapLatest { user ->
        val userId = user?.id.orEmpty()
        val userName = user?.fullName.orEmpty()
        val userEmail = user?.email.orEmpty()

        if (userId.isNotEmpty()) {
            // Guarantee habits are initialized for this user & sync
            viewModelScope.launch {
                repository.ensureInitialHabitsForUser(userId)
                repository.syncWithSupabase(userId)
            }
            combine(
                repository.getHabitsFlow(userId),
                repository.getCompletionsFlow(userId)
            ) { habits, completions ->
                val todayCompletions = completions.filter { it.date == todayDateString }
                val completedHabitIds = todayCompletions.map { it.habitId }.toSet()

                val habitItems = habits.map { entity ->
                    HabitItemUi(
                        id = entity.id,
                        name = entity.name,
                        category = entity.category,
                        iconName = entity.iconName,
                        goal = entity.goal,
                        preferredTime = entity.preferredTime,
                        streak = entity.streak,
                        bestStreak = entity.bestStreak,
                        isCompletedToday = completedHabitIds.contains(entity.id)
                    )
                }

                val totalCount = habitItems.size
                val completedCount = habitItems.count { it.isCompletedToday }
                val rate = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

                val maxBest = habits.maxOfOrNull { it.bestStreak } ?: 0
                val currentStreak = habits.filter { it.streak > 0 }.maxOfOrNull { it.streak } ?: 0
                val totalCompletionsAllTime = completions.size

                val weeklyOverview = buildWeeklyOverview(completions, totalCount, todayDateString)
                val weeklyAverageRate = if (weeklyOverview.isNotEmpty()) {
                    val pastAndToday = weeklyOverview.filter { it.isPast || it.isToday }
                    if (pastAndToday.isNotEmpty()) {
                        (pastAndToday.map { it.completionRate }.average() * 100).toInt()
                    } else 0
                } else 0

                HabitUiState(
                    habits = habitItems,
                    todayFormattedDate = todayFormatted,
                    todayDateString = todayDateString,
                    completionRate = rate,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    currentStreak = currentStreak,
                    bestStreak = maxBest,
                    totalCompletionsAllTime = totalCompletionsAllTime,
                    weeklyOverview = weeklyOverview,
                    weeklyAverageRate = weeklyAverageRate,
                    isLoading = false,
                    userEmail = userEmail,
                    userName = userName
                )
            }
        } else {
            flowOf(
                HabitUiState(
                    todayFormattedDate = todayFormatted,
                    todayDateString = todayDateString,
                    isLoading = false
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState(
            todayFormattedDate = todayFormatted,
            todayDateString = todayDateString,
            isLoading = true
        )
    )

    // ==========================================
    // AUTH ACTIONS
    // ==========================================

    fun signUp(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        onResult: (AuthResult) -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isEmpty() || trimmedEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            onResult(AuthResult.Error("Please complete all fields."))
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            onResult(AuthResult.Error("Please enter a valid email address."))
            return
        }

        if (password != confirmPassword) {
            onResult(AuthResult.Error("Password and Confirm Password do not match."))
            return
        }

        if (password.length < 6) {
            onResult(AuthResult.Error("Password must be at least 6 characters."))
            return
        }

        viewModelScope.launch {
            _authActionLoading.value = true
            try {
                val result = supabaseClient.signUp(
                    name = trimmedName,
                    email = trimmedEmail,
                    password = password,
                    redirectTo = "mono://auth-callback"
                )
                onResult(result)
            } finally {
                _authActionLoading.value = false
            }
        }
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            onResult(AuthResult.Error("Please enter your email and password."))
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            onResult(AuthResult.Error("Please enter a valid email address."))
            return
        }

        viewModelScope.launch {
            _authActionLoading.value = true
            try {
                val result = supabaseClient.signInWithPassword(trimmedEmail, password)
                onResult(result)
            } finally {
                _authActionLoading.value = false
            }
        }
    }

    fun resendVerification(email: String, onResult: (Result<String>) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            onResult(Result.failure(IllegalArgumentException("Please enter your email.")))
            return
        }

        viewModelScope.launch {
            _authActionLoading.value = true
            try {
                val result = supabaseClient.resendVerificationEmail(trimmedEmail)
                onResult(result)
            } finally {
                _authActionLoading.value = false
            }
        }
    }

    fun verifyEmailSimulated(email: String) {
        supabaseClient.verifyUserEmail(email)
    }

    fun dismissVerificationNotice() {
        supabaseClient.dismissVerificationPrompt()
    }

    fun signOut() {
        viewModelScope.launch {
            supabaseClient.signOut()
        }
    }

    // ==========================================
    // HABIT ACTIONS
    // ==========================================

    private fun getCurrentUserId(): String = currentUser.value?.id.orEmpty()

    fun toggleHabit(habitId: Long) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return
        val currentHabit = uiState.value.habits.find { it.id == habitId } ?: return

        viewModelScope.launch {
            repository.toggleHabitCompletion(
                habitId = habitId,
                date = todayDateString,
                isCurrentlyCompleted = currentHabit.isCompletedToday,
                userId = userId
            )
        }
    }

    fun addHabit(
        name: String,
        category: String,
        iconName: String,
        goal: String,
        preferredTime: String
    ) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return

        viewModelScope.launch {
            repository.addHabit(
                userId = userId,
                name = name,
                category = category,
                iconName = iconName,
                goal = goal,
                preferredTime = preferredTime
            )
        }
    }

    fun updateHabit(
        id: Long,
        name: String,
        category: String,
        iconName: String,
        goal: String,
        preferredTime: String
    ) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return

        viewModelScope.launch {
            val existing = uiState.value.habits.find { it.id == id }
            val updated = HabitEntity(
                id = id,
                userId = userId,
                name = name,
                category = category,
                iconName = iconName,
                goal = goal,
                preferredTime = preferredTime,
                streak = existing?.streak ?: 0,
                bestStreak = existing?.bestStreak ?: 0,
                lastCompletedDate = if (existing?.isCompletedToday == true) todayDateString else ""
            )
            repository.updateHabit(updated)
        }
    }

    fun deleteHabit(id: Long) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return

        viewModelScope.launch {
            repository.deleteHabit(id, userId)
        }
    }

    fun resetTodayProgress() {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return

        viewModelScope.launch {
            val completedTodayIds = uiState.value.habits
                .filter { it.isCompletedToday }
                .map { it.id }
            repository.resetTodayProgress(todayDateString, completedTodayIds, userId)
        }
    }

    fun syncWithSupabaseNow() {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                repository.syncWithSupabase(userId)
            }
        }
    }

    companion object {
        fun getTodayDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getHeaderDate(): String {
            val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            return sdf.format(Date())
        }

        private fun buildWeeklyOverview(
            completions: List<DailyCompletionEntity>,
            currentTotalHabits: Int,
            todayDate: String
        ): List<DayOverviewItem> {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfDayLabel = SimpleDateFormat("EEE", Locale.getDefault())
            val sdfDayNumber = SimpleDateFormat("d", Locale.getDefault())

            val items = mutableListOf<DayOverviewItem>()

            for (i in 0 until 7) {
                val dayDate = calendar.time
                val key = sdfKey.format(dayDate)
                val dayLabel = sdfDayLabel.format(dayDate)
                val dayNumber = sdfDayNumber.format(dayDate)

                val dayCompletions = completions.filter { it.date == key }
                val rate = if (currentTotalHabits > 0) {
                    (dayCompletions.size.toFloat() / currentTotalHabits.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val isToday = key == todayDate
                val isPast = key < todayDate

                items.add(
                    DayOverviewItem(
                        dateString = key,
                        dayLabel = dayLabel,
                        dayNumber = dayNumber,
                        completionRate = rate,
                        isToday = isToday,
                        isPast = isPast
                    )
                )

                calendar.add(Calendar.DAY_OF_WEEK, 1)
            }

            return items
        }
    }
}

class HabitViewModelFactory(
    private val repository: HabitRepository,
    private val supabaseClient: SupabaseClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            return HabitViewModel(repository, supabaseClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
