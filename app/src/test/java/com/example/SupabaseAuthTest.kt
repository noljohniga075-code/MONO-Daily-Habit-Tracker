package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.supabase.AuthResult
import com.example.data.supabase.AuthState
import com.example.data.supabase.SupabaseClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SupabaseAuthTest {

    private lateinit var context: Context
    private lateinit var client: SupabaseClient

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        client = SupabaseClient(context)
        runBlocking {
            client.signOut()
        }
    }

    @Test
    fun `signup requires email verification and blocks access`() = runBlocking {
        val email = "alex.mono.test@example.com"
        val name = "Alex Vance"
        val password = "secretPassword123"

        val result = client.signUp(name, email, password)

        assertTrue(result is AuthResult.RequiresVerification)
        val verification = result as AuthResult.RequiresVerification
        assertEquals(
            "Account created successfully. Please check your email and verify your account before logging in.",
            verification.message
        )

        // Verifies that unverified user cannot log in
        val loginResult = client.signInWithPassword(email, password)
        assertTrue(loginResult is AuthResult.RequiresVerification)
        val loginVerification = loginResult as AuthResult.RequiresVerification
        assertEquals(
            "Your email has not been verified yet. Please check your email and click the verification link.",
            loginVerification.message
        )
    }

    @Test
    fun `incorrect login shows incorrect email or password`() = runBlocking {
        val result = client.signInWithPassword("unknown@example.com", "wrongPassword")
        assertTrue(result is AuthResult.Error)
        assertEquals("Incorrect email or password.", (result as AuthResult.Error).message)
    }

    @Test
    fun `resend verification email succeeds`() = runBlocking {
        val email = "alex.mono.test2@example.com"
        val res = client.resendVerificationEmail(email)
        assertTrue(res.isSuccess)
        assertEquals(
            "Verification email resent successfully. Please check your inbox.",
            res.getOrNull()
        )
    }

    @Test
    fun `verified user can login and session is authenticated`() = runBlocking {
        val email = "verified.user@example.com"
        val name = "Sarah Connor"
        val password = "password123"

        client.signUp(name, email, password)

        // Simulate user clicking email verification link
        client.verifyUserEmail(email)

        val loginResult = client.signInWithPassword(email, password)
        assertTrue(loginResult is AuthResult.Success)
        val success = loginResult as AuthResult.Success
        assertEquals(email, success.user.email)
        assertEquals(name, success.user.fullName)
        assertTrue(success.user.isEmailVerified)
        assertTrue(client.authState.value is AuthState.Authenticated)

        // Sign out
        client.signOut()
        assertTrue(client.authState.value is AuthState.Unauthenticated)
    }
}
