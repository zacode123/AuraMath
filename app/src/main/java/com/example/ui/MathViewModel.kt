package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MathViewModel(application: Application) : AndroidViewModel(application) {

    private val mathDao = AppDatabase.getDatabase(application).mathDao()
    private val repository = MathRepository(mathDao)

    // --- Core Flows ---
    val allSessions: StateFlow<List<GameSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trickProgress: StateFlow<List<TrickProgress>> = repository.trickProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Screen Navigation ---
    enum class Screen {
        DASHBOARD,
        TRICKS_LIBRARY,
        GAME_ARENA,
        REPORT_CARD,
        LEADERBOARD,
        GHOST_MATCH,
        SPLIT_DUEL,
        SETTINGS
    }

    var currentScreen by mutableStateOf(Screen.DASHBOARD)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        // Reset state when navigating
        stopArenaGame()
    }

    // --- active trick sandbox state ---
    var selectedTrick by mutableStateOf<MathTrick?>(null)
    var trickWalkthroughStepIndex by mutableStateOf(0)
    var trickSandboxAnswers = mutableStateListOf<String>()
    var sandboxQuestions = mutableListOf<SandboxQuestion>()
    var activeSandboxIndex by mutableStateOf(0)
    var trickWalkthroughInput by mutableStateOf("")
    var isTrickWalkthroughCompleted by mutableStateOf(false)
    var isTrickSandboxCompleted by mutableStateOf(false)
    var trickSandboxScore by mutableStateOf(0)

    fun startTrickFlow(trick: MathTrick) {
        selectedTrick = trick
        trickWalkthroughStepIndex = 0
        trickWalkthroughInput = ""
        isTrickWalkthroughCompleted = false
        isTrickSandboxCompleted = false
        trickSandboxScore = 0
        activeSandboxIndex = 0

        // Prepare 5 sandbox questions
        sandboxQuestions.clear()
        repeat(5) {
            sandboxQuestions.add(trick.generateSandboxQuestion())
        }
        trickSandboxAnswers.clear()
        repeat(5) { trickSandboxAnswers.add("") }
        navigateTo(Screen.TRICKS_LIBRARY)
    }

    fun saveTrickProgress(trickId: String, completed: Boolean, stars: Int, score: Int) {
        viewModelScope.launch {
            repository.saveTrickProgress(trickId, completed, stars, score)
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            repository.initializeUserProfile(newName)
        }
    }

    // --- Arena Gameplay States ---
    enum class GameMode {
        SPEED_RUN,
        DAILY_WORKOUT,
        CUSTOM_PRACTICE,
        GHOST_DUEL,
        SPLIT_DUEL
    }

    var activeGameMode by mutableStateOf(GameMode.SPEED_RUN)
    var activeEquation by mutableStateOf(GeneratedEquation("", "", ""))
    var currentAnswerInput by mutableStateOf("")
    var timeLeftSeconds by mutableStateOf(60)
    var comboCount by mutableStateOf(0)
    var correctCount by mutableStateOf(0)
    var totalQuestionsAttempted by mutableStateOf(0)
    var isGameRunning by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)
    var solvedEquationsList = mutableListOf<Pair<GeneratedEquation, Boolean>>() // Eq, wasCorrect
    var totalXPAdded by mutableStateOf(0)

    // Custom configuration parameters
    var customCategories = mutableStateListOf("Addition", "Subtraction")
    var customDifficulty by mutableStateOf(MathDifficulty.BEGINNER)
    var customQuestionLimit by mutableStateOf(15)

    // Statistics and Timestamps
    private var questionStartTime = 0L
    private var totalSolvingTimeMs = 0L

    private var gameTimerJob: Job? = null

    fun startArenaGame(mode: GameMode) {
        activeGameMode = mode
        currentAnswerInput = ""
        comboCount = 0
        correctCount = 0
        totalQuestionsAttempted = 0
        isGameRunning = true
        isGameOver = false
        solvedEquationsList.clear()
        totalSolvingTimeMs = 0L
        timeLeftSeconds = if (mode == GameMode.SPEED_RUN) 60 else if (mode == GameMode.DAILY_WORKOUT) 180 else 120

        generateNextArenaQuestion()
        startTimer()
    }

    fun handleNumKeypadInput(char: String) {
        if (!isGameRunning) return
        if (char == "C") {
            currentAnswerInput = ""
        } else if (char == "⌫") {
            if (currentAnswerInput.isNotEmpty()) {
                currentAnswerInput = currentAnswerInput.dropLast(1)
            }
        } else {
            if (currentAnswerInput.length < 8) {
                currentAnswerInput += char
            }
        }
    }

    fun submitAnswer() {
        if (!isGameRunning) return
        val elapsed = System.currentTimeMillis() - questionStartTime
        totalSolvingTimeMs += elapsed

        val isCorrect = currentAnswerInput == activeEquation.answer
        solvedEquationsList.add(Pair(activeEquation, isCorrect))
        totalQuestionsAttempted++

        if (isCorrect) {
            correctCount++
            comboCount++
            // Add time for Speed Run mode
            if (activeGameMode == GameMode.SPEED_RUN) {
                timeLeftSeconds += 2
                if (timeLeftSeconds > 99) timeLeftSeconds = 99
            }
        } else {
            comboCount = 0
            if (activeGameMode == GameMode.SPEED_RUN) {
                timeLeftSeconds = maxOf(0, timeLeftSeconds - 5)
                if (timeLeftSeconds <= 0) {
                    endArenaGame()
                    return
                }
            }
        }

        currentAnswerInput = ""
        generateNextArenaQuestion()
    }

    private fun generateNextArenaQuestion() {
        questionStartTime = System.currentTimeMillis()

        // Adaptive Difficulty calculation based on speed and combo
        val effectiveDifficulty = if (activeGameMode == GameMode.SPEED_RUN) {
            when {
                comboCount >= 8 -> MathDifficulty.MASTER
                comboCount >= 4 -> MathDifficulty.INTERMEDIATE
                else -> MathDifficulty.BEGINNER
            }
        } else if (activeGameMode == GameMode.CUSTOM_PRACTICE) {
            customDifficulty
        } else {
            MathDifficulty.INTERMEDIATE
        }

        // Selected operation types
        val ops = if (activeGameMode == GameMode.CUSTOM_PRACTICE) {
            val list = customCategories.map {
                when (it) {
                    "Addition" -> OperationType.ADDITION
                    "Subtraction" -> OperationType.SUBTRACTION
                    "Multiplication" -> OperationType.MULTIPLICATION
                    "Division" -> OperationType.DIVISION
                    "Percentages" -> OperationType.PERCENTAGE
                    else -> OperationType.MIXED
                }
            }
            if (list.isEmpty()) OperationType.MIXED else list.random()
        } else {
            OperationType.MIXED
        }

        activeEquation = MathGenerator.generate(effectiveDifficulty, ops)

        // Stop custom tests when reaching the question limit
        if (activeGameMode == GameMode.CUSTOM_PRACTICE && totalQuestionsAttempted >= customQuestionLimit) {
            endArenaGame()
        }
    }

    private fun startTimer() {
        gameTimerJob?.cancel()
        gameTimerJob = viewModelScope.launch {
            while (timeLeftSeconds > 0 && isGameRunning) {
                delay(1000)
                timeLeftSeconds--
            }
            if (isGameRunning) {
                endArenaGame()
            }
        }
    }

    private fun endArenaGame() {
        isGameRunning = false
        isGameOver = true
        gameTimerJob?.cancel()

        // Persist game session to database
        val accuracy = if (totalQuestionsAttempted > 0) {
            (correctCount.toFloat() / totalQuestionsAttempted) * 100f
        } else {
            0f
        }

        val avgTime = if (totalQuestionsAttempted > 0) {
            totalSolvingTimeMs / totalQuestionsAttempted
        } else {
            0L
        }

        // Categories solved
        val subDetailsString = solvedEquationsList.map { it.first.category }.distinct().joinToString(", ")

        // Calculate direct score based on correctness
        val finalScore = correctCount * 10

        viewModelScope.launch {
            val session = GameSession(
                mode = activeGameMode.name,
                score = finalScore,
                totalQuestions = totalQuestionsAttempted,
                correctQuestions = correctCount,
                accuracy = accuracy,
                averageTimePerQuestionMs = avgTime,
                subDetails = subDetailsString
            )
            repository.insertSession(session)
            // Trigger UI report card refresh
            fetchWeakSpotDiagnosis()
        }
    }

    fun stopArenaGame() {
        isGameRunning = false
        isGameOver = false
        gameTimerJob?.cancel()
    }

    // --- Simulated Ghost Match / Multiplayer ---
    data class GhostOpponent(
        val name: String,
        val rank: String,
        val speedMs: Long,
        val accuracy: Float,
        val avatarUrl: String? = null
    )

    val ghosts = listOf(
        GhostOpponent("Hypotenuse_Hank", "#12", 2100, 95f),
        GhostOpponent("Mathemagician", "#5", 1400, 98f),
        GhostOpponent("Fibonacci_Force", "#42", 2800, 90f),
        GhostOpponent("Cosine_Cowboy", "#28", 2500, 88f)
    )

    var selectedGhost by mutableStateOf(ghosts[0])
    var playerProgress by mutableStateOf(0f) // 0.0 to 1.0
    var ghostProgress by mutableStateOf(0f)
    var ghostStatusMsg by mutableStateOf("")

    private var ghostJob: Job? = null

    fun startGhostMatch(ghost: GhostOpponent) {
        selectedGhost = ghost
        playerProgress = 0f
        ghostProgress = 0f
        ghostStatusMsg = "The race is on!"
        timeLeftSeconds = 90
        isGameRunning = true
        isGameOver = false
        correctCount = 0
        totalQuestionsAttempted = 0
        solvedEquationsList.clear()

        generateNextArenaQuestion()

        // Op timer
        gameTimerJob?.cancel()
        gameTimerJob = viewModelScope.launch {
            while (timeLeftSeconds > 0 && isGameRunning) {
                delay(1000)
                timeLeftSeconds--
            }
            if (isGameRunning) {
                ghostStatusMsg = "Time's up!"
                endGhostMatch()
            }
        }

        // Ghost progress simulation
        ghostJob?.cancel()
        ghostJob = viewModelScope.launch {
            val totalQuestions = 15
            while (ghostProgress < 1.0f && isGameRunning) {
                // Ghost solves questions at its pace
                val solveDelay = ghost.speedMs + Random.nextLong(-300, 400)
                delay(solveDelay)
                val isCorrect = Random.nextFloat() * 100f <= ghost.accuracy
                if (isCorrect) {
                    ghostProgress += 1.0f / totalQuestions
                    ghostStatusMsg = "${ghost.name} just solved another equation!"
                } else {
                    ghostStatusMsg = "${ghost.name} made an error and is stalled!"
                    delay(1500) // penalty
                }
                if (ghostProgress >= 1.0f) {
                    ghostProgress = 1.0f
                    ghostStatusMsg = "${ghost.name} crossed the finish line!"
                    endGhostMatch()
                }
            }
        }
    }

    fun submitGhostAnswer() {
        val isCorrect = currentAnswerInput == activeEquation.answer
        totalQuestionsAttempted++
        if (isCorrect) {
            correctCount++
            playerProgress += 1.0f / 15
            if (playerProgress >= 1.0f) {
                playerProgress = 1.0f
                ghostStatusMsg = "Victory! You crossed the finish line first!"
                endGhostMatch()
                return
            }
        } else {
            // stalled penalty
            ghostStatusMsg = "Mistake! Locked for 1.5 seconds!"
            currentAnswerInput = ""
            return
        }
        currentAnswerInput = ""
        generateNextArenaQuestion()
    }

    private fun endGhostMatch() {
        isGameRunning = false
        isGameOver = true
        ghostJob?.cancel()
        gameTimerJob?.cancel()

        val isPlayerWinner = playerProgress >= 1.0f && playerProgress > ghostProgress
        val score = if (isPlayerWinner) 150 else (playerProgress * 100).toInt()

        viewModelScope.launch {
            val session = GameSession(
                mode = "GHOST_DUEL",
                score = score,
                totalQuestions = totalQuestionsAttempted,
                correctQuestions = correctCount,
                accuracy = if (totalQuestionsAttempted > 0) (correctCount.toFloat() / totalQuestionsAttempted) * 100f else 0f,
                averageTimePerQuestionMs = 2500L,
                subDetails = "Ghost Rivalry against ${selectedGhost.name}"
            )
            repository.insertSession(session)
            fetchWeakSpotDiagnosis()
        }
    }

    // --- Split-Screen Local Duel ---
    var player1ActiveEquation by mutableStateOf(GeneratedEquation("", "", ""))
    var player2ActiveEquation by mutableStateOf(GeneratedEquation("", "", ""))
    var player1AnswerInput by mutableStateOf("")
    var player2AnswerInput by mutableStateOf("")
    var player1Score by mutableStateOf(0)
    var player2Score by mutableStateOf(0)
    var duelTimeLeft by mutableStateOf(60)
    var isDuelRunning by mutableStateOf(false)
    var isDuelOver by mutableStateOf(false)
    var duelWinner by mutableStateOf("")

    private var duelTimerJob: Job? = null

    fun startSplitDuel() {
        player1AnswerInput = ""
        player2AnswerInput = ""
        player1Score = 0
        player2Score = 0
        duelTimeLeft = 60
        isDuelRunning = true
        isDuelOver = false
        duelWinner = ""

        generatePlayer1Question()
        generatePlayer2Question()

        duelTimerJob?.cancel()
        duelTimerJob = viewModelScope.launch {
            while (duelTimeLeft > 0 && isDuelRunning) {
                delay(1000)
                duelTimeLeft--
            }
            if (isDuelRunning) {
                endSplitDuel()
            }
        }
    }

    fun generatePlayer1Question() {
        player1ActiveEquation = MathGenerator.generate(MathDifficulty.INTERMEDIATE, OperationType.MIXED)
    }

    fun generatePlayer2Question() {
        player2ActiveEquation = MathGenerator.generate(MathDifficulty.INTERMEDIATE, OperationType.MIXED)
    }

    fun handlePlayer1Answer(input: String) {
        if (input == player1ActiveEquation.answer) {
            player1Score += 10
            player1AnswerInput = ""
            generatePlayer1Question()
        } else {
            player1AnswerInput = "" // clear on error
        }
    }

    fun handlePlayer2Answer(input: String) {
        if (input == player2ActiveEquation.answer) {
            player2Score += 10
            player2AnswerInput = ""
            generatePlayer2Question()
        } else {
            player2AnswerInput = ""
        }
    }

    private fun endSplitDuel() {
        isDuelRunning = false
        isDuelOver = true
        duelTimerJob?.cancel()

        duelWinner = when {
            player1Score > player2Score -> "Player 1 (Top/Orange)"
            player2Score > player1Score -> "Player 2 (Bottom/Teal)"
            else -> "It's a tie!"
        }
    }

    // --- Report Card & Weak Spot Analysis via Gemini API ---
    var aiReportReview by mutableStateOf("Initializing AuraMath AI Coach...")
        private set
    var isAiReportLoading by mutableStateOf(false)
        private set

    var operationStatsMap by mutableStateOf<Map<String, OperationStats>>(emptyMap())
        private set

    fun fetchWeakSpotDiagnosis() {
        viewModelScope.launch {
            operationStatsMap = repository.getWeakSpotAnalysis()
        }
    }

    fun loadAiReportCard(username: String, xp: Int, level: Int, streak: Int) {
        isAiReportLoading = true
        viewModelScope.launch {
            val stats = repository.getWeakSpotAnalysis()
            val totalQuestions = stats.values.sumOf { it.totalSessions }
            val averageAccuracy = if (stats.isNotEmpty()) stats.values.map { it.averageAccuracy }.average().toFloat() else 90f
            val averageTimeSec = if (stats.isNotEmpty()) (stats.values.map { it.averageTimeMs }.average() / 1000f).toFloat() else 2.2f

            val weakSpotsText = if (stats.isNotEmpty()) {
                stats.entries
                    .sortedBy { it.value.averageAccuracy }
                    .take(2)
                    .joinToString(", ") { "${it.key} (${String.format("%.1f", it.value.averageAccuracy)}% accuracy, ${String.format("%.2f", it.value.averageTimeMs / 1000f)}s avg)" }
            } else {
                "No sessions recorded yet! Play Speed Run or Practice Tricks to let the AI analyze your strengths."
            }

            val review = GeminiApiClient.getReportCardReview(
                username = username,
                xp = xp,
                level = level,
                streak = streak,
                weakSpots = weakSpotsText,
                accuracy = averageAccuracy,
                avgTimeSec = averageTimeSec
            )
            aiReportReview = review
            isAiReportLoading = false
        }
    }

    // --- Global Leaderboard Simulation ---
    data class LeaderboardEntry(
        val rank: Int,
        val username: String,
        val country: String,
        val xp: Int,
        val isUser: Boolean = false
    )

    val leaderboardData = listOf(
        LeaderboardEntry(1, "Euler_Enthusiast", "🇺🇸", 12500),
        LeaderboardEntry(2, "Hypotenuse_Hank", "🇬🇧", 11200),
        LeaderboardEntry(3, "Mathemagician", "🇯🇵", 9800),
        LeaderboardEntry(4, "Fibonacci_Force", "🇨🇦", 8400),
        LeaderboardEntry(5, "Cosine_Cowboy", "🇦🇺", 7900),
        LeaderboardEntry(6, "Matrix_Master", "🇩🇪", 6800),
        LeaderboardEntry(7, "AuraMath Elite (You)", "🇺🇸", 0, true) // Will be updated dynamically with actual user XP
    )

    fun getDynamicLeaderboard(userXp: Int): List<LeaderboardEntry> {
        val userEntry = LeaderboardEntry(7, "AuraMath Elite (You)", "🇺🇸", userXp, true)
        val fullList = (leaderboardData.filter { !it.isUser } + userEntry)
            .sortedByDescending { it.xp }
        
        return fullList.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    init {
        // Initialize Default User Profile if empty
        viewModelScope.launch {
            repository.initializeUserProfile("AuraMath Elite")
            fetchWeakSpotDiagnosis()
        }
    }
}

val MathTrick.colorAccent: Long
    get() = when (category) {
        "Addition" -> 0xFF00FFCC // Neon Mint
        "Subtraction" -> 0xFFFF66CC // Pink
        "Multiplication" -> 0xFF8833FF // Purple
        "Division" -> 0xFF33CCFF // Light Blue
        "Percentages" -> 0xFFFFCC33 // Gold
        else -> 0xFFFFFFFF
    }
