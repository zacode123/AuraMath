package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Data Classes for Gemini API ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

// --- Retrofit API Service ---

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiService::class.java)
    }

    suspend fun getReportCardReview(
        username: String,
        xp: Int,
        level: Int,
        streak: Int,
        weakSpots: String,
        accuracy: Float,
        avgTimeSec: Float
    ): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return "To get a fully personalized AI Math Report Card, please configure your GEMINI_API_KEY in the AI Studio Secrets Panel! Here is your algorithmic insight: Keep focusing on $weakSpots, your overall math speed averages ${String.format("%.2f", avgTimeSec)}s with ${String.format("%.1f", accuracy)}% accuracy."
        }

        val prompt = """
            You are AuraMath AI, an elite mathematical personal coach.
            Analyze this user's math training performance data:
            - User: $username
            - Current XP: $xp
            - Level: $level
            - Active Streak: $streak days
            - Average Performance: ${String.format("%.1f", accuracy)}% accuracy, averaging ${String.format("%.2f", avgTimeSec)} seconds per calculation.
            - Diagnosed Weak Spots (operation and delay ratios): $weakSpots
            
            Provide a concise, motivating, and highly professional report card assessment. Mention their streak and exact tips/tricks they can practice to conquer their weaknesses.
            Format the response in brief paragraphs or bullet points. Avoid markdown blocks, keep it conversational and direct (max 150 words).
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

        return try {
            val response = service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Excellent progress! Continue practicing your daily mental workouts to increase your agility."
        } catch (e: Exception) {
            "Algorithmic diagnostic: You're showing consistent progress with ${String.format("%.1f", accuracy)}% accuracy. Focus heavily on $weakSpots to break through your speed ceilings."
        }
    }
}
