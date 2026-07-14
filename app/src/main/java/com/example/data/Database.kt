package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String, // SPEED_RUN, DAILY_WORKOUT, TRICK_PRACTICE, CUSTOM_TEST, GHOST_DUEL, SPLIT_DUEL
    val score: Int,
    val totalQuestions: Int,
    val correctQuestions: Int,
    val accuracy: Float, // percentage
    val averageTimePerQuestionMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val subDetails: String // operations used, e.g. "Multiplication, Division"
)

@Entity(tableName = "trick_progress")
data class TrickProgress(
    @PrimaryKey val trickId: String,
    val completed: Boolean,
    val stars: Int,
    val lastAttemptScore: Int,
    val lastAttemptTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "AuraMath Elite",
    val xp: Int = 0,
    val level: Int = 1,
    val dailyStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "" // YYYY-MM-DD
)

// --- DAO ---

@Dao
interface MathDao {
    // Game Sessions
    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE mode = :mode ORDER BY timestamp DESC")
    fun getSessionsByMode(mode: String): Flow<List<GameSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSession)

    // Trick Progress
    @Query("SELECT * FROM trick_progress")
    fun getAllTrickProgress(): Flow<List<TrickProgress>>

    @Query("SELECT * FROM trick_progress WHERE trickId = :trickId LIMIT 1")
    suspend fun getTrickProgress(trickId: String): TrickProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrickProgress(progress: TrickProgress)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

// --- Database Class ---

@Database(
    entities = [GameSession::class, TrickProgress::class, UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mathDao(): MathDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auramath_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
