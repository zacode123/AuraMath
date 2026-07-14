package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

// --- Frosted Glass Colors ---
val GlassBg = Color(0x11FFFFFF)
val GlassBorder = Color(0x22FFFFFF)
val GlassBorderStrong = Color(0x44FFFFFF)

// Neon Theme Accents
val NeonMint = Color(0xFF00FFCC)
val NeonPurple = Color(0xFF8833FF)
val NeonPink = Color(0xFFFF3399)
val NeonBlue = Color(0xFF33CCFF)
val DarkNavy = Color(0xFF0A091A)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke = BorderStroke(1.dp, GlassBorder),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        border = borderStroke,
        content = content
    )
}

@Composable
fun BrainPowerCup(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    waveColor: Color = NeonMint
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val targetProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Box(
        modifier = modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(GlassBg)
            .border(2.dp, GlassBorderStrong, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Clip drawing inside a circle to simulate a cup
            val circlePath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(0f, 0f, width, height))
            }

            clipPath(circlePath) {
                val waveHeight = 8.dp.toPx()
                val waterLevelY = height - (animatedProgress * height)

                // Back Wave
                val backPath = Path()
                backPath.moveTo(0f, height)
                for (x in 0..width.toInt() step 5) {
                    val angle = (x / width) * (2 * Math.PI) + phase
                    val y = waterLevelY + waveHeight * sin(angle).toFloat()
                    backPath.lineTo(x.toFloat(), y)
                }
                backPath.lineTo(width, height)
                backPath.close()
                drawPath(backPath, color = waveColor.copy(alpha = 0.5f))

                // Front Wave
                val frontPath = Path()
                frontPath.moveTo(0f, height)
                for (x in 0..width.toInt() step 5) {
                    // Shift the angle phase slightly for depth
                    val angle = (x / width) * (2 * Math.PI) - phase + Math.PI / 2
                    val y = waterLevelY + waveHeight * sin(angle).toFloat()
                    frontPath.lineTo(x.toFloat(), y)
                }
                frontPath.lineTo(width, height)
                frontPath.close()
                drawPath(frontPath, color = waveColor)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Brain Cup",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WaveProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    waveColor: Color = NeonBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progressBar")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val clipPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        0f, 0f, width, height,
                        androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
                    )
                )
            }

            clipPath(clipPath) {
                val waveHeight = 4.dp.toPx()
                val targetX = progress * width
                val waterLevelY = height / 2

                val wavePath = Path()
                wavePath.moveTo(0f, height)
                for (x in 0..targetX.toInt() step 5) {
                    val angle = (x / width) * (4 * Math.PI) + phase
                    val y = waterLevelY + waveHeight * sin(angle).toFloat()
                    wavePath.lineTo(x.toFloat(), y)
                }
                wavePath.lineTo(targetX, height)
                wavePath.close()

                drawPath(wavePath, color = waveColor)
            }
        }
    }
}

@Composable
fun CustomGlassKeyboard(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("⌫", "0", "C")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in keys) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    val isSpecial = key == "⌫" || key == "C"
                    val itemBg = if (isSpecial) Color(0x22FFFFFF) else GlassBg
                    val itemBorder = if (isSpecial) GlassBorderStrong else GlassBorder

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(itemBg)
                            .border(1.dp, itemBorder, RoundedCornerShape(16.dp))
                            .clickable { onKeyClick(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = if (isSpecial) NeonPink else Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
