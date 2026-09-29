package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.HabitRepository
import com.example.data.supabase.AuthState
import com.example.data.supabase.SupabaseClient
import com.example.ui.HabitViewModel
import com.example.ui.HabitViewModelFactory
import com.example.ui.MonoDashboardScreen
import com.example.ui.auth.AuthLoadingScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthTab
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var supabaseClient: SupabaseClient

    private val habitViewModel: HabitViewModel by viewModels {
        supabaseClient = SupabaseClient(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = HabitRepository(database.habitDao(), supabaseClient)
        HabitViewModelFactory(repository, supabaseClient)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLink(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MonoDarkBackground
                ) {
                    val authState by habitViewModel.authState.collectAsStateWithLifecycle()

                    when (val state = authState) {
                        is AuthState.CheckingSession -> {
                            AuthLoadingScreen()
                        }
                        is AuthState.Authenticated -> {
                            MonoDashboardScreen(viewModel = habitViewModel)
                        }
                        is AuthState.AwaitingVerification -> {
                            AuthScreen(
                                viewModel = habitViewModel,
                                initialTab = AuthTab.LOGIN
                            )
                        }
                        is AuthState.Unauthenticated -> {
                            AuthScreen(
                                viewModel = habitViewModel,
                                initialTab = AuthTab.LOGIN
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "mono" && uri.host == "auth-callback") {
            val email = uri.getQueryParameter("email")
            if (!email.isNullOrEmpty()) {
                habitViewModel.verifyEmailSimulated(email)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "MONO: $name", modifier = modifier)
}

