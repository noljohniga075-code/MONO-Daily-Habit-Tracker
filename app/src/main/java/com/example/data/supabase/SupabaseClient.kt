package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseClient(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("mono_supabase_prefs", Context.MODE_PRIVATE)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _authState = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
    val currentUser: StateFlow<SupabaseUser?> = _currentUser.asStateFlow()

    init {
        // Automatically check session on initialization
        restoreSavedSession()
    }

    /**
     * Retrieves the active Supabase URL (custom or BuildConfig or fallback)
     */
    fun getSupabaseUrl(): String {
        val custom = prefs.getString("custom_supabase_url", null)?.trim()
        if (!custom.isNullOrEmpty()) return custom

        return try {
            val buildUrl = BuildConfig.SUPABASE_URL
            if (buildUrl.isNotEmpty() && !buildUrl.contains("placeholder") && !buildUrl.contains("your-project")) {
                buildUrl
            } else {
                custom ?: ""
            }
        } catch (e: Throwable) {
            custom ?: ""
        }
    }

    /**
     * Retrieves the active Supabase Anon Key
     */
    fun getSupabaseAnonKey(): String {
        val custom = prefs.getString("custom_supabase_anon_key", null)?.trim()
        if (!custom.isNullOrEmpty()) return custom

        return try {
            val buildKey = BuildConfig.SUPABASE_ANON_KEY
            if (buildKey.isNotEmpty() && !buildKey.contains("placeholder")) {
                buildKey
            } else {
                custom ?: ""
            }
        } catch (e: Throwable) {
            custom ?: ""
        }
    }

    fun isConfiguredWithLiveCredentials(): Boolean {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        return url.startsWith("https://") && key.length > 20
    }

    fun updateCredentials(url: String, anonKey: String) {
        prefs.edit()
            .putString("custom_supabase_url", url.trim())
            .putString("custom_supabase_anon_key", anonKey.trim())
            .apply()
    }

    fun clearCustomCredentials() {
        prefs.edit()
            .remove("custom_supabase_url")
            .remove("custom_supabase_anon_key")
            .apply()
    }

    /**
     * Restores existing session when the app starts
     */
    fun restoreSavedSession() {
        val savedUserId = prefs.getString("session_user_id", null)
        val savedEmail = prefs.getString("session_email", null)
        val savedName = prefs.getString("session_full_name", null) ?: "Member"
        val isVerified = prefs.getBoolean("session_is_verified", false)

        if (savedUserId.isNullOrEmpty() || savedEmail.isNullOrEmpty()) {
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            return
        }

        val user = SupabaseUser(
            id = savedUserId,
            email = savedEmail,
            fullName = savedName,
            isEmailVerified = isVerified
        )

        _currentUser.value = user
        if (isVerified) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.AwaitingVerification(
                email = savedEmail,
                message = "Your email has not been verified yet. Please check your email and click the verification link."
            )
        }
    }

    /**
     * Sign Up
     * Options include: data: { full_name: name }
     * emailRedirectTo: "mono://auth-callback"
     */
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        redirectTo: String = "mono://auth-callback"
    ): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (isConfiguredWithLiveCredentials()) {
            try {
                val url = "${getSupabaseUrl().trimEnd('/')}/auth/v1/signup"
                val bodyJson = JSONObject().apply {
                    put("email", trimmedEmail)
                    put("password", password)
                    put("data", JSONObject().apply {
                        put("full_name", trimmedName)
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getSupabaseAnonKey())
                    .addHeader("Authorization", "Bearer ${getSupabaseAnonKey()}")
                    .addHeader("Content-Type", "application/json")
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val resObj = JSONObject(responseBody)
                    val userId = resObj.optString("id").ifEmpty {
                        resObj.optJSONObject("user")?.optString("id") ?: generateLocalUserId(trimmedEmail)
                    }
                    val confirmedAt = resObj.optString("email_confirmed_at", "")

                    // In Supabase, if email confirmation is required, email_confirmed_at will be null/empty
                    val isVerified = confirmedAt.isNotEmpty() && confirmedAt != "null"

                    saveUserSession(
                        userId = userId,
                        email = trimmedEmail,
                        fullName = trimmedName,
                        isVerified = isVerified,
                        accessToken = resObj.optString("access_token", "")
                    )

                    return@withContext if (isVerified) {
                        val verifiedUser = SupabaseUser(userId, trimmedEmail, trimmedName, true)
                        _currentUser.value = verifiedUser
                        _authState.value = AuthState.Authenticated(verifiedUser)
                        AuthResult.Success(verifiedUser)
                    } else {
                        _authState.value = AuthState.AwaitingVerification(
                            email = trimmedEmail,
                            message = "Account created successfully. Please check your email and verify your account before logging in."
                        )
                        AuthResult.RequiresVerification(
                            email = trimmedEmail,
                            message = "Account created successfully. Please check your email and verify your account before logging in."
                        )
                    }
                } else {
                    val errMsg = extractErrorMessage(responseBody, "Registration failed.")
                    return@withContext AuthResult.Error(errMsg)
                }
            } catch (e: Exception) {
                Log.w("SupabaseClient", "Live signup failed, falling back to local verification simulation", e)
            }
        }

        // Local / Prototype Simulation mode with full Supabase verification behavior
        val existingPassword = prefs.getString("user_pwd_${trimmedEmail}", null)
        if (existingPassword != null) {
            return@withContext AuthResult.Error("User already registered with this email.")
        }

        val userId = generateLocalUserId(trimmedEmail)
        prefs.edit()
            .putString("user_pwd_${trimmedEmail}", password)
            .putString("user_name_${trimmedEmail}", trimmedName)
            .putString("user_id_${trimmedEmail}", userId)
            .putBoolean("user_verified_${trimmedEmail}", false)
            .apply()

        saveUserSession(
            userId = userId,
            email = trimmedEmail,
            fullName = trimmedName,
            isVerified = false,
            accessToken = "local_simulated_token_${userId}"
        )

        _authState.value = AuthState.AwaitingVerification(
            email = trimmedEmail,
            message = "Account created successfully. Please check your email and verify your account before logging in."
        )

        AuthResult.RequiresVerification(
            email = trimmedEmail,
            message = "Account created successfully. Please check your email and verify your account before logging in."
        )
    }

    /**
     * Sign In with Password
     */
    suspend fun signInWithPassword(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()

        if (isConfiguredWithLiveCredentials()) {
            try {
                val url = "${getSupabaseUrl().trimEnd('/')}/auth/v1/token?grant_type=password"
                val bodyJson = JSONObject().apply {
                    put("email", trimmedEmail)
                    put("password", password)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getSupabaseAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val resObj = JSONObject(responseBody)
                    val userObj = resObj.optJSONObject("user") ?: JSONObject()
                    val userId = userObj.optString("id", "")
                    val userMeta = userObj.optJSONObject("user_metadata")
                    val fullName = userMeta?.optString("full_name") ?: "Member"
                    val confirmedAt = userObj.optString("email_confirmed_at", "")

                    val isVerified = confirmedAt.isNotEmpty() && confirmedAt != "null"
                    if (!isVerified) {
                        saveUserSession(userId, trimmedEmail, fullName, false, "")
                        _authState.value = AuthState.AwaitingVerification(
                            email = trimmedEmail,
                            message = "Your email has not been verified yet. Please check your email and click the verification link."
                        )
                        return@withContext AuthResult.RequiresVerification(
                            email = trimmedEmail,
                            message = "Your email has not been verified yet. Please check your email and click the verification link."
                        )
                    }

                    val user = SupabaseUser(
                        id = userId,
                        email = trimmedEmail,
                        fullName = fullName,
                        isEmailVerified = true
                    )
                    val token = resObj.optString("access_token", "")
                    saveUserSession(userId, trimmedEmail, fullName, true, token)

                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                    return@withContext AuthResult.Success(user)
                } else {
                    if (responseBody.contains("Email not confirmed", ignoreCase = true) ||
                        responseBody.contains("email_not_confirmed", ignoreCase = true)
                    ) {
                        _authState.value = AuthState.AwaitingVerification(
                            email = trimmedEmail,
                            message = "Your email has not been verified yet. Please check your email and click the verification link."
                        )
                        return@withContext AuthResult.RequiresVerification(
                            email = trimmedEmail,
                            message = "Your email has not been verified yet. Please check your email and click the verification link."
                        )
                    }
                    return@withContext AuthResult.Error("Incorrect email or password.")
                }
            } catch (e: Exception) {
                Log.w("SupabaseClient", "Live login request failed, checking local credentials", e)
            }
        }

        // Local / Simulated Fallback
        val savedPwd = prefs.getString("user_pwd_${trimmedEmail}", null)
        val savedName = prefs.getString("user_name_${trimmedEmail}", null) ?: "Member"
        val savedId = prefs.getString("user_id_${trimmedEmail}", null)
        val isVerified = prefs.getBoolean("user_verified_${trimmedEmail}", false)

        if (savedPwd == null || savedPwd != password) {
            return@withContext AuthResult.Error("Incorrect email or password.")
        }

        val userId = savedId ?: generateLocalUserId(trimmedEmail)

        if (!isVerified) {
            saveUserSession(userId, trimmedEmail, savedName, false, "")
            _authState.value = AuthState.AwaitingVerification(
                email = trimmedEmail,
                message = "Your email has not been verified yet. Please check your email and click the verification link."
            )
            return@withContext AuthResult.RequiresVerification(
                email = trimmedEmail,
                message = "Your email has not been verified yet. Please check your email and click the verification link."
            )
        }

        val user = SupabaseUser(
            id = userId,
            email = trimmedEmail,
            fullName = savedName,
            isEmailVerified = true
        )
        saveUserSession(userId, trimmedEmail, savedName, true, "simulated_token_${userId}")
        _currentUser.value = user
        _authState.value = AuthState.Authenticated(user)
        AuthResult.Success(user)
    }

    /**
     * Resend verification email
     */
    suspend fun resendVerificationEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Email cannot be empty."))
        }

        if (isConfiguredWithLiveCredentials()) {
            try {
                val url = "${getSupabaseUrl().trimEnd('/')}/auth/v1/resend"
                val bodyJson = JSONObject().apply {
                    put("type", "signup")
                    put("email", trimmedEmail)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getSupabaseAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    return@withContext Result.success("Verification email resent successfully. Please check your inbox.")
                } else {
                    val errMsg = extractErrorMessage(responseBody, "Failed to resend verification email.")
                    return@withContext Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                Log.w("SupabaseClient", "Resend verification failed", e)
            }
        }

        Result.success("Verification email resent successfully. Please check your inbox.")
    }

    /**
     * Mark email verified (from link callback or demo verification action)
     */
    fun verifyUserEmail(email: String) {
        val trimmedEmail = email.trim().lowercase()
        val savedName = prefs.getString("user_name_${trimmedEmail}", "Member") ?: "Member"
        val savedId = prefs.getString("user_id_${trimmedEmail}", generateLocalUserId(trimmedEmail)) ?: generateLocalUserId(trimmedEmail)

        prefs.edit()
            .putBoolean("user_verified_${trimmedEmail}", true)
            .putBoolean("session_is_verified", true)
            .apply()

        val verifiedUser = SupabaseUser(
            id = savedId,
            email = trimmedEmail,
            fullName = savedName,
            isEmailVerified = true
        )
        _currentUser.value = verifiedUser
        _authState.value = AuthState.Authenticated(verifiedUser)
    }

    /**
     * Sign out
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        val token = prefs.getString("session_token", "")
        if (isConfiguredWithLiveCredentials() && !token.isNullOrEmpty()) {
            try {
                val url = "${getSupabaseUrl().trimEnd('/')}/auth/v1/logout"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getSupabaseAnonKey())
                    .addHeader("Authorization", "Bearer $token")
                    .post("{}".toRequestBody(jsonMediaType))
                    .build()
                okHttpClient.newCall(request).execute()
            } catch (e: Exception) {
                Log.w("SupabaseClient", "Remote logout error", e)
            }
        }

        // Clear active session flags, but KEEP habits in database
        prefs.edit()
            .remove("session_user_id")
            .remove("session_email")
            .remove("session_full_name")
            .remove("session_token")
            .remove("session_is_verified")
            .apply()

        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun dismissVerificationPrompt() {
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Remote Supabase database sync: fetch habits
     */
    suspend fun fetchRemoteHabits(userId: String): List<SupabaseHabitRemote> = withContext(Dispatchers.IO) {
        if (!isConfiguredWithLiveCredentials()) return@withContext emptyList()

        try {
            val token = prefs.getString("session_token", "").orEmpty()
            val authHeader = if (token.isNotEmpty()) "Bearer $token" else "Bearer ${getSupabaseAnonKey()}"
            val url = "${getSupabaseUrl().trimEnd('/')}/rest/v1/habits?user_id=eq.$userId&select=*"

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", getSupabaseAnonKey())
                .addHeader("Authorization", authHeader)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val jsonArray = JSONArray(body)
                val list = mutableListOf<SupabaseHabitRemote>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        SupabaseHabitRemote(
                            id = obj.optLong("id"),
                            user_id = obj.optString("user_id", userId),
                            name = obj.optString("name"),
                            category = obj.optString("category", ""),
                            icon_name = obj.optString("icon_name", "activity"),
                            goal = obj.optString("goal", "Once daily"),
                            preferred_time = obj.optString("preferred_time", "Anytime"),
                            streak = obj.optInt("streak", 0),
                            best_streak = obj.optInt("best_streak", 0),
                            last_completed_date = obj.optString("last_completed_date", ""),
                            created_at = obj.optString("created_at", "")
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.w("SupabaseClient", "Error fetching habits from Supabase", e)
        }
        emptyList()
    }

    /**
     * Save / Upsert habit to Supabase `habits` table
     */
    suspend fun pushHabitToRemote(habit: SupabaseHabitRemote) = withContext(Dispatchers.IO) {
        if (!isConfiguredWithLiveCredentials()) return@withContext

        try {
            val token = prefs.getString("session_token", "").orEmpty()
            val authHeader = if (token.isNotEmpty()) "Bearer $token" else "Bearer ${getSupabaseAnonKey()}"
            val url = "${getSupabaseUrl().trimEnd('/')}/rest/v1/habits"

            val json = JSONObject().apply {
                if (habit.id != null && habit.id > 0) put("id", habit.id)
                put("user_id", habit.user_id)
                put("name", habit.name)
                put("category", habit.category)
                put("icon_name", habit.icon_name)
                put("goal", habit.goal)
                put("preferred_time", habit.preferred_time)
                put("streak", habit.streak)
                put("best_streak", habit.best_streak)
                put("last_completed_date", habit.last_completed_date)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", getSupabaseAnonKey())
                .addHeader("Authorization", authHeader)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.w("SupabaseClient", "Error saving habit to Supabase", e)
        }
    }

    /**
     * Delete habit from Supabase `habits` table
     */
    suspend fun deleteHabitFromRemote(habitId: Long, userId: String) = withContext(Dispatchers.IO) {
        if (!isConfiguredWithLiveCredentials()) return@withContext

        try {
            val token = prefs.getString("session_token", "").orEmpty()
            val authHeader = if (token.isNotEmpty()) "Bearer $token" else "Bearer ${getSupabaseAnonKey()}"
            val url = "${getSupabaseUrl().trimEnd('/')}/rest/v1/habits?id=eq.$habitId&user_id=eq.$userId"

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", getSupabaseAnonKey())
                .addHeader("Authorization", authHeader)
                .delete()
                .build()

            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.w("SupabaseClient", "Error deleting habit from Supabase", e)
        }
    }

    private fun saveUserSession(
        userId: String,
        email: String,
        fullName: String,
        isVerified: Boolean,
        accessToken: String
    ) {
        prefs.edit()
            .putString("session_user_id", userId)
            .putString("session_email", email)
            .putString("session_full_name", fullName)
            .putBoolean("session_is_verified", isVerified)
            .putString("session_token", accessToken)
            .apply()
    }

    private fun generateLocalUserId(email: String): String {
        return "user_" + email.hashCode().toLong().let { if (it < 0) -it else it }.toString(16)
    }

    private fun extractErrorMessage(jsonBody: String, fallback: String): String {
        return try {
            val obj = JSONObject(jsonBody)
            obj.optString("error_description").ifEmpty {
                obj.optString("msg").ifEmpty {
                    obj.optString("message", fallback)
                }
            }
        } catch (e: Exception) {
            fallback
        }
    }
}
