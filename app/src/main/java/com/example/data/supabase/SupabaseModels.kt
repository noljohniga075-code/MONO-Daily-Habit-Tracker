package com.example.data.supabase

/**
 * Represents a Supabase Authenticated User.
 */
data class SupabaseUser(
    val id: String,
    val email: String,
    val fullName: String,
    val isEmailVerified: Boolean,
    val createdAt: String? = null
)

/**
 * Represents an active Supabase Session with access & refresh tokens.
 */
data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String,
    val user: SupabaseUser,
    val expiresAt: Long = System.currentTimeMillis() + (3600 * 1000)
)

/**
 * High-level state of authentication across the application.
 */
sealed class AuthState {
    object CheckingSession : AuthState()
    object Unauthenticated : AuthState()
    data class AwaitingVerification(
        val email: String,
        val message: String = "Your email has not been verified yet. Please check your email and click the verification link."
    ) : AuthState()
    data class Authenticated(val user: SupabaseUser) : AuthState()
}

/**
 * Result returned by Auth operations (signUp, signIn, resend).
 */
sealed class AuthResult {
    data class Success(val user: SupabaseUser, val message: String? = null) : AuthResult()
    data class RequiresVerification(
        val email: String,
        val message: String
    ) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * DTO matching Supabase `habits` table columns:
 * id, user_id, name, category, icon_name, goal, preferred_time, streak, best_streak, last_completed_date, created_at
 */
data class SupabaseHabitRemote(
    val id: Long? = null,
    val user_id: String,
    val name: String,
    val category: String = "",
    val icon_name: String = "activity",
    val goal: String = "Once daily",
    val preferred_time: String = "Anytime",
    val streak: Int = 0,
    val best_streak: Int = 0,
    val last_completed_date: String = "",
    val created_at: String? = null
)

/**
 * DTO matching Supabase `daily_completions` table columns
 */
data class SupabaseCompletionRemote(
    val user_id: String,
    val habit_id: Long,
    val date: String,
    val completed_at: Long = System.currentTimeMillis()
)
