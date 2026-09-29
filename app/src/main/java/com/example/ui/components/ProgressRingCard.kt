package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonoCardBorder
import com.example.ui.theme.MonoCardBorderActive
import com.example.ui.theme.MonoCardSurface
import com.example.ui.theme.MonoCardSurfaceElevated
import com.example.ui.theme.MonoDarkBackground
import com.example.ui.theme.MonoSilver
import com.example.ui.theme.MonoSilverMuted
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoTextTertiary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressRingCard(
    completionRate: Float,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_ring_animation"
    )

    val percentageInt = (animatedProgress * 100).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MonoCardSurfaceElevated,
                        MonoCardSurface
                    )
                )
            )
            .border(1.dp, MonoCardBorder, RoundedCornerShape(20.dp))
            .padding(24.dp)
            .testTag("daily_progress_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Section Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY FOCUS",
                    color = MonoTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MonoDarkBackground)
                        .border(1.dp, MonoCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (totalCount > 0 && completedCount == totalCount) "ALL DONE" else "IN PROGRESS",
                        color = if (totalCount > 0 && completedCount == totalCount) MonoSilver else MonoTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Luxury Chronometer Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(190.dp)
                    .testTag("progress_ring_gauge")
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = arcSize / 2f

                    // Subtle dial tick marks (luxury watch chronometer motif)
                    val tickCount = 60
                    for (i in 0 until tickCount) {
                        val angle = (i * (360f / tickCount)) * (Math.PI / 180f).toFloat()
                        val isMajor = i % 5 == 0
                        val outerRadius = radius + 8.dp.toPx()
                        val innerRadius = outerRadius - (if (isMajor) 6.dp.toPx() else 3.dp.toPx())
                        val tickColor = if (isMajor) Color(0x33FFFFFF) else Color(0x1AFFFFFF)

                        val startX = center.x + innerRadius * cos(angle)
                        val startY = center.y + innerRadius * sin(angle)
                        val endX = center.x + outerRadius * cos(angle)
                        val endY = center.y + outerRadius * sin(angle)

                        drawLine(
                            color = tickColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // Background Track
                    drawArc(
                        color = MonoSilverMuted,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Progress Stroke with Silver/White gradient
                    if (animatedProgress > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFB0B4C0),
                                    Color(0xFFE5E7EB),
                                    Color(0xFFFFFFFF),
                                    Color(0xFFE5E7EB)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Centered Information
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$percentageInt%",
                        color = MonoTextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Complete",
                        color = MonoTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle status: "4 of 6 habits completed"
            Text(
                text = if (totalCount == 0) "No habits yet" else "$completedCount of $totalCount habits completed",
                color = MonoTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
        }
    }
}
