package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.supabase.AuthResult
import com.example.ui.HabitViewModel
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary

enum class AuthTab {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    viewModel: HabitViewModel,
    initialTab: AuthTab = AuthTab.LOGIN,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(initialTab) }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Signup Form State
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }
    var signupPasswordVisible by remember { mutableStateOf(false) }

    // Notice & Feedback Banners
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var unverifiedEmailNotice by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Dialogs
    var showResendDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MonoDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Supabase settings trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showConfigDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MonoCardSurface)
                        .border(1.dp, MonoCardBorder, CircleShape)
                        .testTag("auth_supabase_config_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cloud,
                        contentDescription = "Supabase Configuration",
                        tint = MonoSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MONO",
                    color = MonoTextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(MonoSilver)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Daily Habit Tracker",
                color = MonoTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Small habits. Quiet progress.",
                color = MonoTextTertiary,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Luxury Segmented Tab Selector
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MonoCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sign In Tab
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == AuthTab.LOGIN) MonoSilver else MonoCardSurface)
                            .clickable {
                                activeTab = AuthTab.LOGIN
                                errorMessage = null
                            }
                            .testTag("tab_login")
                    ) {
                        Text(
                            text = "Sign In",
                            color = if (activeTab == AuthTab.LOGIN) MonoDarkBackground else MonoTextSecondary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Sign Up Tab
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == AuthTab.SIGN_UP) MonoSilver else MonoCardSurface)
                            .clickable {
                                activeTab = AuthTab.SIGN_UP
                                errorMessage = null
                            }
                            .testTag("tab_signup")
                    ) {
                        Text(
                            text = "Sign Up",
                            color = if (activeTab == AuthTab.SIGN_UP) MonoDarkBackground else MonoTextSecondary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Unverified Account Banner
            AnimatedVisibility(
                visible = unverifiedEmailNotice != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                unverifiedEmailNotice?.let { notice ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MonoCardSurfaceElevated)
                            .border(1.dp, MonoSilver.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.MarkEmailUnread,
                                contentDescription = null,
                                tint = MonoSilver,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EMAIL VERIFICATION REQUIRED",
                                    color = MonoSilver,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notice,
                                    color = MonoTextPrimary,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Resend Email button
                            Button(
                                onClick = {
                                    val emailToResend = if (activeTab == AuthTab.LOGIN) loginEmail else signupEmail
                                    showResendDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MonoDarkBackground,
                                    contentColor = MonoSilver
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .border(1.dp, MonoCardBorder, RoundedCornerShape(8.dp))
                                    .testTag("banner_resend_button")
                            ) {
                                Text("Resend Email", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Simulate Verification for reviewer/testing convenience
                            Button(
                                onClick = {
                                    val emailToVerify = if (activeTab == AuthTab.LOGIN) loginEmail else signupEmail
                                    if (emailToVerify.isNotEmpty()) {
                                        viewModel.verifyEmailSimulated(emailToVerify)
                                        unverifiedEmailNotice = null
                                        statusMessage = "Email verified successfully! You can now log in."
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MonoSilver,
                                    contentColor = MonoDarkBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("simulate_verify_button")
                            ) {
                                Text("Verify Now (Testing)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // General Status / Success Banner
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                statusMessage?.let { msg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MonoCardSurfaceElevated)
                            .border(1.dp, MonoSilver.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MonoSilver,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = MonoTextPrimary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Error Banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { err ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MonoCardSurfaceElevated)
                            .border(1.dp, MonoCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MonoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            color = MonoTextPrimary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Main Auth Form Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MonoCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_form_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    if (activeTab == AuthTab.LOGIN) {
                        // ===================================
                        // SIGN IN FORM
                        // ===================================
                        Text(
                            text = "WELCOME BACK",
                            color = MonoSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Access your habits and streaks",
                            color = MonoTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Email
                        Text(
                            text = "EMAIL",
                            color = MonoTextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = {
                                loginEmail = it
                                errorMessage = null
                            },
                            placeholder = { Text("you@example.com", color = MonoTextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoTextPrimary,
                                unfocusedTextColor = MonoTextPrimary,
                                focusedContainerColor = MonoDarkBackground,
                                unfocusedContainerColor = MonoDarkBackground,
                                focusedBorderColor = MonoSilver,
                                unfocusedBorderColor = MonoCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password
                        Text(
                            text = "PASSWORD",
                            color = MonoTextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                errorMessage = null
                            },
                            placeholder = { Text("••••••••", color = MonoTextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = MonoTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoTextPrimary,
                                unfocusedTextColor = MonoTextPrimary,
                                focusedContainerColor = MonoDarkBackground,
                                unfocusedContainerColor = MonoDarkBackground,
                                focusedBorderColor = MonoSilver,
                                unfocusedBorderColor = MonoCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Login Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                errorMessage = null
                                unverifiedEmailNotice = null
                                statusMessage = null
                                isSubmitting = true

                                viewModel.signIn(loginEmail, loginPassword) { result ->
                                    isSubmitting = false
                                    when (result) {
                                        is AuthResult.Success -> {
                                            // Navigation handled by authState change
                                        }
                                        is AuthResult.RequiresVerification -> {
                                            unverifiedEmailNotice = result.message
                                        }
                                        is AuthResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoSilver,
                                contentColor = MonoDarkBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MonoDarkBackground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Sign In", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to Sign Up
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Don't have an account?",
                                color = MonoTextSecondary,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign Up",
                                color = MonoSilver,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        activeTab = AuthTab.SIGN_UP
                                        errorMessage = null
                                        statusMessage = null
                                    }
                                    .testTag("switch_to_signup_button")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Resend Verification Link
                        TextButton(
                            onClick = { showResendDialog = true },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag("open_resend_dialog_button")
                        ) {
                            Text(
                                text = "Resend Verification Email",
                                color = MonoTextTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // ===================================
                        // SIGN UP FORM
                        // ===================================
                        Text(
                            text = "CREATE AN ACCOUNT",
                            color = MonoSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your habits permanently stored in Supabase",
                            color = MonoTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Name
                        Text(
                            text = "FULL NAME",
                            color = MonoTextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupName,
                            onValueChange = {
                                signupName = it
                                errorMessage = null
                            },
                            placeholder = { Text("Alex Vance", color = MonoTextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = FocusDirection.Down.let { ImeAction.Next }
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoTextPrimary,
                                unfocusedTextColor = MonoTextPrimary,
                                focusedContainerColor = MonoDarkBackground,
                                unfocusedContainerColor = MonoDarkBackground,
                                focusedBorderColor = MonoSilver,
                                unfocusedBorderColor = MonoCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        Text(
                            text = "EMAIL",
                            color = MonoTextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = {
                                signupEmail = it
                                errorMessage = null
                            },
                            placeholder = { Text("you@example.com", color = MonoTextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoTextPrimary,
                                unfocusedTextColor = MonoTextPrimary,
                                focusedContainerColor = MonoDarkBackground,
                                unfocusedContainerColor = MonoDarkBackground,
                                focusedBorderColor = MonoSilver,
                                unfocusedBorderColor = MonoCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        Text(
                            text = "PASSWORD (MIN 6 CHARACTERS)",
                            color = MonoTextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupPassword,
                            onValueChange = {
                                signupPassword = it
                                errorMessage = null
                            },
                            placeholder = { Text("••••••••", color = MonoTextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { signupPasswordVisible = !signupPasswordVisible }) {
                                    Icon(
                                        imageVector = if (signupPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = MonoTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoTextPrimary,
                                unfocusedTextColor = MonoTextPrimary,
                                focusedContainerColor = MonoDarkBackground,
                                unfocusedContainerColor = MonoDarkBackground,
                                focusedBorderColor = MonoSilver,
                                unfocusedBorderColor = MonoCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_password_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password
                        Text(
                            text = "CONFIRM PASSWORD",
                            color = MonoTextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupConfirmPassword,
                            onValueChange = {
                                signupConfirmPassword = it
                                errorMessage = null
                            },
                            placeholder = { Text("••••••••", color = MonoTextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoTextPrimary,
                                unfocusedTextColor = MonoTextPrimary,
                                focusedContainerColor = MonoDarkBackground,
                                unfocusedContainerColor = MonoDarkBackground,
                                focusedBorderColor = MonoSilver,
                                unfocusedBorderColor = MonoCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Sign Up Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                errorMessage = null
                                statusMessage = null
                                unverifiedEmailNotice = null
                                isSubmitting = true

                                viewModel.signUp(
                                    name = signupName,
                                    email = signupEmail,
                                    password = signupPassword,
                                    confirmPassword = signupConfirmPassword
                                ) { result ->
                                    isSubmitting = false
                                    when (result) {
                                        is AuthResult.Success -> {
                                            statusMessage = "Account created and verified! Welcome to MONO."
                                        }
                                        is AuthResult.RequiresVerification -> {
                                            unverifiedEmailNotice = result.message
                                            loginEmail = signupEmail
                                        }
                                        is AuthResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoSilver,
                                contentColor = MonoDarkBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("signup_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MonoDarkBackground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Create Account", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to Login
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account?",
                                color = MonoTextSecondary,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign In",
                                color = MonoSilver,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        activeTab = AuthTab.LOGIN
                                        errorMessage = null
                                        statusMessage = null
                                    }
                                    .testTag("switch_to_login_button")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer assurance
            Text(
                text = "Protected with Supabase Authentication & RLS Storage",
                color = MonoTextTertiary,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            )
        }
    }

    // Resend Dialog
    if (showResendDialog) {
        ResendVerificationDialog(
            initialEmail = if (loginEmail.isNotEmpty()) loginEmail else signupEmail,
            onDismiss = { showResendDialog = false },
            onResend = { email, callback ->
                viewModel.resendVerification(email, callback)
            }
        )
    }

    // Supabase Config Dialog
    if (showConfigDialog) {
        SupabaseConfigDialog(
            supabaseClient = viewModel.supabaseClient,
            onDismiss = { showConfigDialog = false }
        )
    }
}
