package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.data.*
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import kotlin.math.sin

// --- Shifting Gradient Background Composable ---
@Composable
fun ShiftingGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Shift coordinate center with a circular motion
            val shiftX = (width / 4) * sin(phase)
            val shiftY = (height / 4) * sin(phase + Math.PI / 2).toFloat()

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1F123C),
                        Color(0xFF0F0829),
                        DarkNavy
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        width / 2 + shiftX,
                        height / 2 + shiftY
                    ),
                    radius = width * 1.2f
                )
            )
        }
        content()
    }
}

// --- Navigation Bar / Safe Areas Wrapper ---
@Composable
fun AuraMathApp(viewModel: MathViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentScreen = viewModel.currentScreen

    ShiftingGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Main Top Navigation Bar (Only on secondary screens)
            if (currentScreen != MathViewModel.Screen.DASHBOARD && currentScreen != MathViewModel.Screen.SPLIT_DUEL) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(MathViewModel.Screen.DASHBOARD) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = when (currentScreen) {
                            MathViewModel.Screen.TRICKS_LIBRARY -> "Tricks Library"
                            MathViewModel.Screen.GAME_ARENA -> "Math Arena"
                            MathViewModel.Screen.REPORT_CARD -> "AI Report Card"
                            MathViewModel.Screen.LEADERBOARD -> "Global Ranks"
                            MathViewModel.Screen.GHOST_MATCH -> "Ghost Duel"
                            MathViewModel.Screen.SETTINGS -> "Preferences"
                            else -> "AuraMath"
                        },
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        MathViewModel.Screen.DASHBOARD -> DashboardScreen(viewModel)
                        MathViewModel.Screen.TRICKS_LIBRARY -> TricksLibraryScreen(viewModel)
                        MathViewModel.Screen.GAME_ARENA -> GameArenaScreen(viewModel)
                        MathViewModel.Screen.REPORT_CARD -> ReportCardScreen(viewModel)
                        MathViewModel.Screen.LEADERBOARD -> LeaderboardScreen(viewModel)
                        MathViewModel.Screen.GHOST_MATCH -> GhostMatchScreen(viewModel)
                        MathViewModel.Screen.SPLIT_DUEL -> SplitDuelScreen(viewModel)
                        MathViewModel.Screen.SETTINGS -> SettingsScreen(viewModel)
                    }
                }
            }

            // Bottom Navigation Bar (Dashboard levels)
            if (currentScreen == MathViewModel.Screen.DASHBOARD ||
                currentScreen == MathViewModel.Screen.TRICKS_LIBRARY ||
                currentScreen == MathViewModel.Screen.REPORT_CARD ||
                currentScreen == MathViewModel.Screen.LEADERBOARD
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color(0x0A000000))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isActive = currentScreen == MathViewModel.Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(MathViewModel.Screen.DASHBOARD) }
                    )
                    BottomNavItem(
                        icon = Icons.Default.Book,
                        label = "Tricks",
                        isActive = currentScreen == MathViewModel.Screen.TRICKS_LIBRARY,
                        onClick = { viewModel.navigateTo(MathViewModel.Screen.TRICKS_LIBRARY) }
                    )
                    BottomNavItem(
                        icon = Icons.Default.BarChart,
                        label = "Insights",
                        isActive = currentScreen == MathViewModel.Screen.REPORT_CARD,
                        onClick = {
                            viewModel.navigateTo(MathViewModel.Screen.REPORT_CARD)
                            val prof = userProfile ?: UserProfile()
                            viewModel.loadAiReportCard(prof.username, prof.xp, prof.level, prof.dailyStreak)
                        }
                    )
                    BottomNavItem(
                        icon = Icons.Default.EmojiEvents,
                        label = "Ranks",
                        isActive = currentScreen == MathViewModel.Screen.LEADERBOARD,
                        onClick = { viewModel.navigateTo(MathViewModel.Screen.LEADERBOARD) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) NeonMint else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) NeonMint else Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// --- Screen 1: Dashboard (Home) ---
@Composable
fun DashboardScreen(viewModel: MathViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val prof = profile ?: UserProfile()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()

    val currentLevelXp = prof.xp % 1000
    val levelProgress = currentLevelXp.toFloat() / 1000f

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming & Streak Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AURA MATH",
                    color = NeonMint,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "Hey, ${prof.username}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Streak Badges
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE25822).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFE25822).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Streak",
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${prof.dailyStreak} Days",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Level & XP glass progress bar
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level ${prof.level}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${prof.xp} XP",
                        color = NeonMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = NeonMint,
                    trackColor = Color(0x22FFFFFF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${1000 - currentLevelXp} XP to next level",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Animated Glass Cup Widget & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brain Power Cup (Animated Liquid Cup)
            // Goal is say, 500 XP per day, let's represent that progress
            val dailyXpGoal = 500f
            val todayXp = sessions
                .filter {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    sdf.format(java.util.Date(it.timestamp)) == sdf.format(java.util.Date())
                }
                .sumOf { it.score * 10 } // Estimated XP
            val cupProgress = minOf(1.0f, todayXp.toFloat() / dailyXpGoal)

            BrainPowerCup(
                progress = if (cupProgress == 0f) 0.05f else cupProgress, // min height to see liquid
                modifier = Modifier.weight(1f)
            )

            // Direct play cards
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Play Time Attack
                Button(
                    onClick = {
                        viewModel.startArenaGame(MathViewModel.GameMode.SPEED_RUN)
                        viewModel.navigateTo(MathViewModel.Screen.GAME_ARENA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FlashOn, "Speed Run", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Speed Run", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Daily workout
                Button(
                    onClick = {
                        viewModel.startArenaGame(MathViewModel.GameMode.DAILY_WORKOUT)
                        viewModel.navigateTo(MathViewModel.Screen.GAME_ARENA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FitnessCenter, "Workout", tint = DarkNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Daily workout", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Custom Practice Arena Creator
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Custom Arena Setup",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Select Categories
                Text(
                    text = "Select Operations:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                val allCats = listOf("Addition", "Subtraction", "Multiplication", "Division", "Percentages")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allCats.forEach { cat ->
                        val isSelected = viewModel.customCategories.contains(cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonBlue.copy(alpha = 0.3f) else GlassBg)
                                .border(1.dp, if (isSelected) NeonBlue else GlassBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isSelected) viewModel.customCategories.remove(cat)
                                    else viewModel.customCategories.add(cat)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, color = if (isSelected) NeonBlue else Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Select Difficulty
                Text(
                    text = "Difficulty:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MathDifficulty.values().forEach { diff ->
                        val isSelected = viewModel.customDifficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonPink.copy(alpha = 0.3f) else GlassBg)
                                .border(1.dp, if (isSelected) NeonPink else GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.customDifficulty = diff }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff.name,
                                color = if (isSelected) NeonPink else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Limit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Questions: ${viewModel.customQuestionLimit}", color = Color.White, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 15, 25).forEach { lim ->
                            val isSelected = viewModel.customQuestionLimit == lim
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else GlassBg)
                                    .border(1.dp, GlassBorder, CircleShape)
                                    .clickable { viewModel.customQuestionLimit = lim }
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lim.toString(),
                                    color = if (isSelected) DarkNavy else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.startArenaGame(MathViewModel.GameMode.CUSTOM_PRACTICE)
                        viewModel.navigateTo(MathViewModel.Screen.GAME_ARENA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Generate Practice Arena", color = DarkNavy, fontWeight = FontWeight.Black)
                }
            }
        }

        // Competitions & Multiplayers
        Text(
            text = "Competitive Duels",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simulated Ghost Duel
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(MathViewModel.Screen.GHOST_MATCH) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Ghost Race",
                        tint = NeonPink,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ghost Match", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Race online ghosts",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Split Screen Duel
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(MathViewModel.Screen.SPLIT_DUEL) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Local Split Duel",
                        tint = NeonMint,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1v1 Split Screen", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Dual on same screen",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Preferences Button Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(MathViewModel.Screen.SETTINGS) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Privacy & Settings",
                        tint = NeonMint
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Security & Settings", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Privacy policy & local secure DB", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Go",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- Screen 2: Interactive Tricks & Shortcuts Library ---
@Composable
fun TricksLibraryScreen(viewModel: MathViewModel) {
    val progressList by viewModel.trickProgress.collectAsStateWithLifecycle()
    val activeTrick = viewModel.selectedTrick

    if (activeTrick != null) {
        // Active Practising Trick layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trick details
            IconButton(
                onClick = { viewModel.selectedTrick = null },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassBg)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to tricks list", tint = Color.White)
            }

            Text(
                text = activeTrick.title,
                color = Color(activeTrick.colorAccent),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            // Step 1: Explanation
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "The Mental Formula:",
                        color = Color(activeTrick.colorAccent),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeTrick.conceptExplanation,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            if (!viewModel.isTrickWalkthroughCompleted) {
                // Walkthrough Steps
                Text(
                    text = "Walkthrough: Solve ${activeTrick.walkthroughProblem}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val currentStep = activeTrick.walkthroughSteps.getOrNull(viewModel.trickWalkthroughStepIndex)
                if (currentStep != null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = currentStep.instruction,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = viewModel.trickWalkthroughInput,
                                onValueChange = { viewModel.trickWalkthroughInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Your answer") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(activeTrick.colorAccent),
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            val isError = remember { mutableStateOf(false) }

                            if (isError.value) {
                                Text("Incorrect intermediate value! Try again.", color = NeonPink, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (viewModel.trickWalkthroughInput == currentStep.expectedInput) {
                                        isError.value = false
                                        viewModel.trickWalkthroughInput = ""
                                        if (viewModel.trickWalkthroughStepIndex + 1 < activeTrick.walkthroughSteps.size) {
                                            viewModel.trickWalkthroughStepIndex++
                                        } else {
                                            viewModel.isTrickWalkthroughCompleted = true
                                        }
                                    } else {
                                        isError.value = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(activeTrick.colorAccent))
                            ) {
                                Text("Next Step", color = DarkNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (!viewModel.isTrickSandboxCompleted) {
                // Sandbox Practice Mode (5 targeted questions)
                Text(
                    text = "Practice Sandbox: Q ${viewModel.activeSandboxIndex + 1} of 5",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val activeQ = viewModel.sandboxQuestions.getOrNull(viewModel.activeSandboxIndex)
                if (activeQ != null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = activeQ.expression,
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )

                            OutlinedTextField(
                                value = viewModel.trickWalkthroughInput,
                                onValueChange = { viewModel.trickWalkthroughInput = it },
                                modifier = Modifier.width(180.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonMint,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val isCorrect = viewModel.trickWalkthroughInput == activeQ.correctAnswer
                                    if (isCorrect) viewModel.trickSandboxScore++
                                    viewModel.trickWalkthroughInput = ""

                                    if (viewModel.activeSandboxIndex + 1 < 5) {
                                        viewModel.activeSandboxIndex++
                                    } else {
                                        viewModel.isTrickSandboxCompleted = true
                                        // Save progress
                                        val stars = when (viewModel.trickSandboxScore) {
                                            5 -> 3
                                            4 -> 2
                                            3 -> 1
                                            else -> 0
                                        }
                                        viewModel.saveTrickProgress(
                                            trickId = activeTrick.id,
                                            completed = true,
                                            stars = stars,
                                            score = viewModel.trickSandboxScore
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMint)
                            ) {
                                Text("Submit", color = DarkNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Mastery Result Board
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sandbox Finished!",
                            color = NeonMint,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${viewModel.trickSandboxScore} / 5 Correct Answers",
                            color = Color.White,
                            fontSize = 18.sp
                        )

                        // Stars display
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val starsCount = when (viewModel.trickSandboxScore) {
                                5 -> 3
                                4 -> 2
                                3 -> 1
                                else -> 0
                            }
                            repeat(3) { index ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = if (index < starsCount) Color(0xFFFFCC33) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = "You earned +${viewModel.trickSandboxScore * 20 + 150} XP for trick mastery expansion!",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { viewModel.selectedTrick = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(activeTrick.colorAccent))
                        ) {
                            Text("Back to Library", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // Organised Grid list of Tricks
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Interactive Shortcuts Library",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Learn powerful formulas and shortcuts. Master each sandbox to gain maximum stars and level expansions.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            items(TricksManager.tricks) { trick ->
                val progress = progressList.find { it.trickId == trick.id }
                val stars = progress?.stars ?: 0

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startTrickFlow(trick) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(trick.colorAccent).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = trick.category.uppercase(),
                                    color = Color(trick.colorAccent),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = trick.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = trick.summary,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        // Stars indicator
                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = if (index < stars) Color(0xFFFFCC33) else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Screen 3: Active Gameplay Arena (Time Attack/Custom) ---
@Composable
fun GameArenaScreen(viewModel: MathViewModel) {
    if (viewModel.isGameRunning) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Combo count flashing
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Whatshot, "Combo", tint = NeonPink, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "COMBO: ${viewModel.comboCount}",
                        color = if (viewModel.comboCount >= 5) NeonPink else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Timer badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (viewModel.timeLeftSeconds <= 10) NeonPink.copy(alpha = 0.2f) else GlassBg)
                        .border(1.dp, if (viewModel.timeLeftSeconds <= 10) NeonPink else GlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TIME: ${viewModel.timeLeftSeconds}s",
                        color = if (viewModel.timeLeftSeconds <= 10) NeonPink else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top-bar Wave timer progression (represented visually)
            val initialSeconds = if (viewModel.activeGameMode == MathViewModel.GameMode.SPEED_RUN) 60f else 180f
            val progressVal = (viewModel.timeLeftSeconds.toFloat() / initialSeconds).coerceIn(0f, 1f)
            WaveProgressBar(progress = progressVal, waveColor = NeonMint)

            // Center large expression card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.activeEquation.expression,
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Typed answer input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x22000000))
                            .border(2.dp, NeonMint, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.currentAnswerInput.ifEmpty { "Type Answer..." },
                            color = if (viewModel.currentAnswerInput.isEmpty()) Color.White.copy(alpha = 0.4f) else Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Custom numerical keypads
            CustomGlassKeyboard(
                onKeyClick = { key ->
                    viewModel.handleNumKeypadInput(key)
                }
            )

            Button(
                onClick = { viewModel.submitAnswer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("SUBMIT", color = DarkNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else if (viewModel.isGameOver) {
        // Result summary board
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Workout Finished!",
                color = NeonMint,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Pair("Correct", viewModel.correctCount.toString()),
                    Pair("Total Q", viewModel.totalQuestionsAttempted.toString()),
                    Pair(
                        "Accuracy",
                        if (viewModel.totalQuestionsAttempted > 0)
                            "${((viewModel.correctCount.toFloat() / viewModel.totalQuestionsAttempted) * 100).toInt()}%"
                        else "0%"
                    )
                ).forEach { item ->
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = item.first, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.second, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // XP Added
            val xpGain = viewModel.correctCount * 10 + 20
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, "XP Gain", tint = NeonMint, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Rank Expansion", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("XP gained in local synchronization", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "+$xpGain XP",
                        color = NeonMint,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Detail Equations Checked List
            Text(
                text = "Performance Diagnostics:",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.solvedEquationsList.forEach { pair ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = pair.first.expression, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(text = "Category: ${pair.first.category}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (pair.second) "Answer: ${pair.first.answer}" else "Expected: ${pair.first.answer}",
                                    color = if (pair.second) NeonMint else NeonPink,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Icon(
                                    imageVector = if (pair.second) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = if (pair.second) "Correct" else "Incorrect",
                                    tint = if (pair.second) NeonMint else NeonPink
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.navigateTo(MathViewModel.Screen.DASHBOARD) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dashboard", color = DarkNavy, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 4: Dynamic AI Report Card (Analytics) ---
@Composable
fun ReportCardScreen(viewModel: MathViewModel) {
    val stats = viewModel.operationStatsMap
    val aiReview = viewModel.aiReportReview
    val isAiLoading = viewModel.isAiReportLoading

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main charts
        Text(
            text = "Speed vs Accuracy Analysis",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        // Custom drawn Canvas Curve Chart
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Drawing background grids
                    drawRect(Color.White.copy(alpha = 0.05f))

                    // Mock coordinate curves plotting actual math improvement
                    val path = Path()
                    path.moveTo(0f, h * 0.8f)
                    path.cubicTo(w * 0.25f, h * 0.75f, w * 0.5f, h * 0.45f, w * 0.75f, h * 0.35f)
                    path.lineTo(w, h * 0.2f)

                    drawPath(
                        path = path,
                        color = NeonMint,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Dots
                    drawCircle(NeonMint, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f))
                    drawCircle(NeonMint, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w, h * 0.2f))
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Session 1", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text("Practice Peak", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Today", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        }

        // Diagnostics breakdown per category
        Text(
            text = "Strength Diagnostic Matrix",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        if (stats.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QueryStats, "No stats", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No diagnostics calculated yet.", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Solve practice and speed run tests to establish your performance baseline.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stats.forEach { (cat, info) ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = cat, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(text = "${info.totalSessions} sessions completed", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Accuracy", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    Text("${info.averageAccuracy.toInt()}%", color = NeonMint, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Speed", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    Text("${String.format("%.2f", info.averageTimeMs / 1000f)}s", color = NeonBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic AI report card encouragement
        Text(
            text = "AuraMath AI Assessment",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Coach",
                        tint = NeonMint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Elite Coach Feedback", color = NeonMint, fontWeight = FontWeight.Black)
                }

                if (isAiLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = NeonMint)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("AuraMath AI analyzing metrics...", color = Color.White)
                    }
                } else {
                    Text(
                        text = aiReview,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

// --- Screen 5: Leaderboards ---
@Composable
fun LeaderboardScreen(viewModel: MathViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val prof = userProfile ?: UserProfile()
    val fullList = viewModel.getDynamicLeaderboard(prof.xp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AuraMath Global Standings",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Top 3 Podium Displays
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val silver = fullList.getOrNull(1)
            val gold = fullList.getOrNull(0)
            val bronze = fullList.getOrNull(2)

            // 2nd Place (Silver)
            if (silver != null) {
                PodiumColumn(
                    entry = silver,
                    height = 110.dp,
                    colorAccent = Color(0xFFC0C0C0),
                    medal = "🥈",
                    modifier = Modifier.weight(1f)
                )
            }

            // 1st Place (Gold)
            if (gold != null) {
                PodiumColumn(
                    entry = gold,
                    height = 140.dp,
                    colorAccent = Color(0xFFFFD700),
                    medal = "🥇",
                    modifier = Modifier.weight(1.1f)
                )
            }

            // 3rd Place (Bronze)
            if (bronze != null) {
                PodiumColumn(
                    entry = bronze,
                    height = 90.dp,
                    colorAccent = Color(0xFFCD7F32),
                    medal = "🥉",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Leaderboard Lists
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val remainders = fullList.drop(3)
            items(remainders) { entry ->
                val isSelf = entry.isUser
                val border = if (isSelf) BorderStroke(1.dp, NeonMint) else BorderStroke(1.dp, GlassBorder)
                val bg = if (isSelf) NeonMint.copy(alpha = 0.1f) else GlassBg

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = border
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${entry.rank}",
                                color = if (isSelf) NeonMint else Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(text = entry.country, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = entry.username,
                                color = if (isSelf) NeonMint else Color.White,
                                fontWeight = if (isSelf) FontWeight.Black else FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${entry.xp} XP",
                            color = if (isSelf) NeonMint else Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(
    entry: MathViewModel.LeaderboardEntry,
    height: Dp,
    colorAccent: Color,
    medal: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = medal, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.username.substringBefore(" "),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = "${entry.xp} XP",
            color = colorAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(GlassBg)
                .border(
                    BorderStroke(1.dp, colorAccent.copy(alpha = 0.6f)),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.country,
                fontSize = 32.sp
            )
        }
    }
}

// --- Screen 6: Ghost Matches ---
@Composable
fun GhostMatchScreen(viewModel: MathViewModel) {
    if (viewModel.isGameRunning) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Race Indicators
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = viewModel.ghostStatusMsg,
                        color = NeonMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // Player progress bar
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("You", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${(viewModel.playerProgress * 100).toInt()}%", color = NeonMint, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.playerProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonMint,
                            trackColor = Color(0x22FFFFFF)
                        )
                    }

                    // Ghost progress bar
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.selectedGhost.name, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("${(viewModel.ghostProgress * 100).toInt()}%", color = NeonPink, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.ghostProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonPink,
                            trackColor = Color(0x22FFFFFF)
                        )
                    }
                }
            }

            // Math expressions
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.activeEquation.expression,
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x22000000))
                            .border(2.dp, NeonMint, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.currentAnswerInput,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            CustomGlassKeyboard(
                onKeyClick = { key ->
                    viewModel.handleNumKeypadInput(key)
                }
            )

            Button(
                onClick = { viewModel.submitGhostAnswer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("SUBMIT SPEED", color = DarkNavy, fontWeight = FontWeight.Bold)
            }
        }
    } else if (viewModel.isGameOver) {
        // Post ghost duel display
        val playerWinner = viewModel.playerProgress >= 1.0f && viewModel.playerProgress > viewModel.ghostProgress
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (playerWinner) "VICTORY!" else "DEFEAT...",
                color = if (playerWinner) NeonMint else NeonPink,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = if (playerWinner) "You out-paced ${viewModel.selectedGhost.name} with extreme mathematical agility!"
                else "${viewModel.selectedGhost.name} calculated faster this time! Practice shortcuts to expand your speed limits.",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.navigateTo(MathViewModel.Screen.DASHBOARD) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Dashboard", color = DarkNavy, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Ghost profiles selection screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Select Ghost Competitor",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Race against historical records of math elite champions. Complete 15 correct equations first to claim victory.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            items(viewModel.ghosts) { ghost ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.startGhostMatch(ghost)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = ghost.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonPink.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = ghost.rank, color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Solve Delay: ${String.format("%.1f", ghost.speedMs / 1000f)}s",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.startGhostMatch(ghost) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint)
                        ) {
                            Text("Race", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- Screen 7: 1v1 Split-Screen Duel (Local Match) ---
@Composable
fun SplitDuelScreen(viewModel: MathViewModel) {
    if (viewModel.isDuelRunning) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Player 1 Area (Top, rotated 180 degrees)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .rotate(180f)
                    .background(Color(0xFF2E1705))
                    .border(2.dp, Color(0xFFFF6600).copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header P1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PLAYER 1 (ORANGE)", color = Color(0xFFFF6600), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("SCORE: ${viewModel.player1Score}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Equation
                    Text(
                        text = viewModel.player1ActiveEquation.expression,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Input
                    OutlinedTextField(
                        value = viewModel.player1AnswerInput,
                        onValueChange = {
                            viewModel.player1AnswerInput = it
                            viewModel.handlePlayer1Answer(it)
                        },
                        modifier = Modifier.width(160.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 22.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6600),
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Divider bar with Timer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TIME REMAINING: ${viewModel.duelTimeLeft}s",
                    color = NeonPink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player 2 Area (Bottom, Standard rotation)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF031E2C))
                    .border(2.dp, NeonBlue.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Input P2
                    OutlinedTextField(
                        value = viewModel.player2AnswerInput,
                        onValueChange = {
                            viewModel.player2AnswerInput = it
                            viewModel.handlePlayer2Answer(it)
                        },
                        modifier = Modifier.width(160.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 22.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Equation
                    Text(
                        text = viewModel.player2ActiveEquation.expression,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Header P2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PLAYER 2 (TEAL)", color = NeonBlue, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("SCORE: ${viewModel.player2Score}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    } else if (viewModel.isDuelOver) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "DUEL OVER!", color = NeonPink, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Winner: ${viewModel.duelWinner}", color = NeonMint, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "P1 Score: ${viewModel.player1Score} | P2 Score: ${viewModel.player2Score}",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = { viewModel.navigateTo(MathViewModel.Screen.DASHBOARD) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dashboard", color = DarkNavy, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Duel Welcome Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.People, "Duel", tint = NeonMint, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "1v1 Local Split Screen Duel", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Place the device flat on a table between two players. Player 1 (Orange, Top) sits opposite Player 2 (Teal, Bottom). Type answers directly into input fields to submit and clear them! The fastest to calculate correct expressions wins.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = { viewModel.startSplitDuel() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Duel Match", color = DarkNavy, fontWeight = FontWeight.Black)
            }
        }
    }
}

// --- Screen 8: Settings, Privacy, Security ---
@Composable
fun SettingsScreen(viewModel: MathViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val prof = profile ?: UserProfile()

    var usernameInput by remember(prof.username) { mutableStateOf(prof.username) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Player Configuration", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        // Name editor
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Profile Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMint,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Button(
                    onClick = {
                        viewModel.updateUsername(usernameInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Name", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(text = "Privacy & Security Framework", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        // Privacy detailed glass block
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, "Verified Privacy", tint = NeonMint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy & Local Sandboxing", color = NeonMint, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "AuraMath is engineered prioritizing user telemetry privacy and absolute local sandboxing. All game sessions, formulas performance, active stats, and level progress are written strictly inside secure local SQLite database files (via Room). No profiles are broadcast or sold to secondary aggregators.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = GlassBorder)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BugReport, "Security Warnings", tint = NeonPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("API Security Caution", color = NeonPink, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Security Warning: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
