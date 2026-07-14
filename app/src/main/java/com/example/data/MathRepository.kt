package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class MathRepository(private val mathDao: MathDao) {

    val allSessions: Flow<List<GameSession>> = mathDao.getAllSessions()
    val trickProgress: Flow<List<TrickProgress>> = mathDao.getAllTrickProgress()
    val userProfile: Flow<UserProfile?> = mathDao.getUserProfileFlow()

    suspend fun insertSession(session: GameSession) {
        mathDao.insertSession(session)
        // Earn XP: score * 10 XP, plus accuracy bonus
        val xpEarned = session.score * 10 + (session.accuracy * 1.5f).toInt()
        updateUserProfileWithXp(xpEarned)
    }

    suspend fun saveTrickProgress(trickId: String, completed: Boolean, stars: Int, score: Int) {
        val existing = mathDao.getTrickProgress(trickId)
        val newStars = if (existing != null && existing.stars > stars) existing.stars else stars
        val progress = TrickProgress(
            trickId = trickId,
            completed = completed,
            stars = newStars,
            lastAttemptScore = score,
            lastAttemptTimestamp = System.currentTimeMillis()
        )
        mathDao.insertTrickProgress(progress)
        if (completed) {
            updateUserProfileWithXp(150 + stars * 50) // Bonus for mastering tricks
        }
    }

    private suspend fun updateUserProfileWithXp(xpEarned: Int) {
        val existing = mathDao.getUserProfile() ?: UserProfile()
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = format.format(calendar.time)

        // Calculate Streak
        var newStreak = existing.dailyStreak
        var newLongestStreak = existing.longestStreak

        if (existing.lastActiveDate.isEmpty()) {
            // First time active
            newStreak = 1
        } else {
            try {
                val lastDate = format.parse(existing.lastActiveDate)
                val diffMs = calendar.time.time - lastDate.time
                val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

                if (diffDays == 1) {
                    newStreak += 1
                } else if (diffDays > 1) {
                    newStreak = 1 // reset
                }
                // If diffDays == 0 (same day), streak remains unchanged
            } catch (e: Exception) {
                newStreak = 1
            }
        }

        if (newStreak > newLongestStreak) {
            newLongestStreak = newStreak
        }

        val newXp = existing.xp + xpEarned
        val newLevel = (newXp / 1000) + 1

        val updatedProfile = existing.copy(
            xp = newXp,
            level = newLevel,
            dailyStreak = newStreak,
            longestStreak = newLongestStreak,
            lastActiveDate = todayStr
        )
        mathDao.insertUserProfile(updatedProfile)
    }

    suspend fun initializeUserProfile(username: String) {
        val existing = mathDao.getUserProfile()
        if (existing == null) {
            mathDao.insertUserProfile(UserProfile(username = username))
        } else {
            mathDao.insertUserProfile(existing.copy(username = username))
        }
    }

    // Weak Spot Diagnosis
    suspend fun getWeakSpotAnalysis(): Map<String, OperationStats> {
        val sessions = mathDao.getAllSessions().firstOrNull() ?: emptyList()
        if (sessions.isEmpty()) return emptyMap()

        val statsMap = mutableMapOf<String, MutableList<Pair<Float, Long>>>() // category -> list of (accuracy, avgTime)

        for (session in sessions) {
            val categories = session.subDetails.split(", ")
            for (cat in categories) {
                if (cat.isNotBlank()) {
                    val list = statsMap.getOrPut(cat) { mutableListOf() }
                    list.add(Pair(session.accuracy, session.averageTimePerQuestionMs))
                }
            }
        }

        return statsMap.mapValues { (_, dataList) ->
            val avgAccuracy = dataList.map { it.first }.average().toFloat()
            val avgTime = dataList.map { it.second }.average().toLong()
            OperationStats(avgAccuracy, avgTime, dataList.size)
        }
    }
}

data class OperationStats(
    val averageAccuracy: Float,
    val averageTimeMs: Long,
    val totalSessions: Int
)
