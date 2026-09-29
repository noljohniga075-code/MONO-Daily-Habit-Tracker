package com.example.ui.auth

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
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.supabase.SupabaseClient
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary

@Composable
fun SupabaseConfigDialog(
    supabaseClient: SupabaseClient,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(supabaseClient.getSupabaseUrl()) }
    var anonKeyText by remember { mutableStateOf(supabaseClient.getSupabaseAnonKey()) }
    var showSqlSchema by remember { mutableStateOf(false) }
    var savedNotice by remember { mutableStateOf(false) }

    val isLive = supabaseClient.isConfiguredWithLiveCredentials()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MonoCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("supabase_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MonoDarkBackground)
                                .border(1.dp, MonoCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Cloud,
                                contentDescription = null,
                                tint = MonoSilver,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "SUPABASE SETUP",
                            color = MonoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = MonoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MonoCardSurfaceElevated)
                        .border(1.dp, MonoCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isLive) Color(0xFF4ADE80) else Color(0xFFFBBF24))
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text(
                            text = if (isLive) "Connected to Supabase" else "Local / Prototype Mode",
                            color = MonoTextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isLive) "Auth & storage syncing live with Supabase" else "Credentials not set; using local auth & storage",
                            color = MonoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Project URL Field
                Text(
                    text = "SUPABASE PROJECT URL",
                    color = MonoTextTertiary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        savedNotice = false
                    },
                    placeholder = { Text("https://your-project.supabase.co", color = MonoTextTertiary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedContainerColor = MonoDarkBackground,
                        unfocusedContainerColor = MonoDarkBackground,
                        focusedBorderColor = MonoSilver,
                        unfocusedBorderColor = MonoCardBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_url_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Anon Key Field
                Text(
                    text = "SUPABASE ANON PUBLIC KEY",
                    color = MonoTextTertiary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = anonKeyText,
                    onValueChange = {
                        anonKeyText = it
                        savedNotice = false
                    },
                    placeholder = { Text("eyJhbGciOi...", color = MonoTextTertiary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedContainerColor = MonoDarkBackground,
                        unfocusedContainerColor = MonoDarkBackground,
                        focusedBorderColor = MonoSilver,
                        unfocusedBorderColor = MonoCardBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_anon_key_input")
                )

                if (savedNotice) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MonoSilver,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Supabase configuration updated successfully.",
                            color = MonoSilver,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle SQL Schema View
                TextButton(
                    onClick = { showSqlSchema = !showSqlSchema },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showSqlSchema) "Hide SQL Table Schema" else "View Supabase SQL Table Schema",
                        color = MonoSilver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (showSqlSchema) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonoDarkBackground)
                            .border(1.dp, MonoCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = """
-- Supabase habits table:
create table habits (
  id bigint generated by default as identity primary key,
  user_id text not null,
  name text not null,
  category text default '',
  icon_name text default 'activity',
  goal text default 'Once daily',
  preferred_time text default 'Anytime',
  streak integer default 0,
  best_streak integer default 0,
  last_completed_date text default '',
  created_at timestamp with time zone default timezone('utc'::text, now())
);

-- Row Level Security
alter table habits enable row level security;
create policy "Users manage their own habits" on habits
  for all using (auth.uid()::text = user_id);
                            """.trimIndent(),
                            color = MonoTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            supabaseClient.clearCustomCredentials()
                            urlText = supabaseClient.getSupabaseUrl()
                            anonKeyText = supabaseClient.getSupabaseAnonKey()
                            savedNotice = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset", color = MonoTextTertiary, fontSize = 12.5.sp)
                    }

                    Button(
                        onClick = {
                            supabaseClient.updateCredentials(urlText, anonKeyText)
                            savedNotice = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonoSilver,
                            contentColor = MonoDarkBackground
                        ),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text("Save Config", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
