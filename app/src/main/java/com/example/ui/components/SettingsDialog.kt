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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.supabase.SupabaseUser
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary

@Composable
fun SettingsDialog(
    user: SupabaseUser?,
    isLiveSupabase: Boolean,
    onConfigureSupabase: () -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MonoCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MonoDarkBackground)
                                .border(1.dp, MonoCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WorkspacePremium,
                                contentDescription = "MONO",
                                tint = MonoSilver,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "MONO SETTINGS",
                            color = MonoTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
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

                // User Profile Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MonoDarkBackground)
                        .border(1.dp, MonoCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUTHENTICATED USER",
                                color = MonoTextTertiary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4ADE80))
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = "Verified",
                                    color = Color(0xFF4ADE80),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = user?.fullName.orEmpty().ifEmpty { "Member" },
                            color = MonoTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = user?.email.orEmpty(),
                            color = MonoTextSecondary,
                            fontSize = 12.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Supabase Storage & Sync Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingRow(
                        label = "Database",
                        value = "Supabase 'habits' table"
                    )
                    SettingRow(
                        label = "Cloud Sync",
                        value = if (isLiveSupabase) "Active (Supabase REST)" else "Offline-First Local"
                    )
                    SettingRow(
                        label = "Data Isolation",
                        value = "User ID RLS"
                    )
                    SettingRow(
                        label = "Design",
                        value = "Horology Monochrome"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Sync now & Configure Supabase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSyncNow,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MonoSilver
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Sync Cloud", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onConfigureSupabase,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MonoSilver
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Supabase URL", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secure Logout Button
                Button(
                    onClick = onLogout,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonoDarkBackground,
                        contentColor = MonoTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.dp, MonoCardBorder, RoundedCornerShape(12.dp))
                        .testTag("dialog_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MonoSilver
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Sign Out of MONO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Habits are permanently stored in Supabase and restored when you log back in.",
                    color = MonoTextTertiary,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MonoCardSurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MonoTextTertiary, fontSize = 12.sp)
        Text(text = value, color = MonoTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
