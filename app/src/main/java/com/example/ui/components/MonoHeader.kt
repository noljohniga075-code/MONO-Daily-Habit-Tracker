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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary

@Composable
fun MonoHeader(
    currentDate: String,
    userName: String = "",
    userEmail: String = "",
    onResetTodayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Area
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MONO",
                        color = MonoTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    // Precision watch-like status dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MonoSilver)
                    )
                }

                Text(
                    text = "Daily Habit Tracker",
                    color = MonoTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Small habits. Quiet progress.",
                    color = MonoTextTertiary,
                    fontSize = 11.5.sp,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.3.sp
                )
            }

            // Actions & Account Area
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Reset Today button
                IconButton(
                    onClick = onResetTodayClicked,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MonoCardSurface)
                        .border(1.dp, MonoCardBorder, CircleShape)
                        .testTag("reset_today_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = "Reset Today's Progress",
                        tint = MonoTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Settings / Info / Supabase Status button
                IconButton(
                    onClick = onSettingsClicked,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MonoCardSurface)
                        .border(1.dp, MonoCardBorder, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Settings and Supabase Info",
                        tint = MonoTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Logout button
                IconButton(
                    onClick = onLogoutClicked,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MonoCardSurface)
                        .border(1.dp, MonoCardBorder, CircleShape)
                        .testTag("header_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Logout",
                        tint = MonoTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row with Date Tag & User Profile Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Formatted Date Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MonoCardSurfaceElevated)
                    .border(1.dp, MonoCardBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MonoSilver)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentDate.ifEmpty { "Today" },
                    color = MonoTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            // User / Supabase Account Pill
            if (userName.isNotEmpty() || userEmail.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MonoCardSurfaceElevated)
                        .border(1.dp, MonoCardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MonoSilver,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = userName.ifEmpty { userEmail.substringBefore("@") },
                        color = MonoTextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
