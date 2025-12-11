package com.prody.prashant.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.prody.prashant.data.local.entity.BuddhaMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI Service for Buddha interactions using Google's Gemini API.
 * Provides philosophical mentorship through various personas.
 */
class BuddhaAiService(
    private var apiKey: String?,
    private var modelName: String = "gemini-1.5-flash"
) {

    private var generativeModel: GenerativeModel? = null

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    private val generationConfig = generationConfig {
        temperature = 0.8f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1024
    }

    init {
        initializeModel()
    }

    private fun initializeModel() {
        val key = apiKey
        if (key != null && key.isNotBlank()) {
            generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = key,
                generationConfig = generationConfig,
                safetySettings = safetySettings
            )
        }
    }

    fun updateApiKey(newKey: String?) {
        apiKey = newKey
        initializeModel()
    }

    fun updateModel(newModel: String) {
        modelName = newModel
        initializeModel()
    }

    fun isConfigured(): Boolean = apiKey != null && apiKey!!.isNotBlank()

    // ==================== Core Chat Functions ====================

    /**
     * Sends a message to Buddha and receives a philosophical response.
     */
    suspend fun chat(
        userMessage: String,
        conversationHistory: String = "",
        mode: BuddhaMode = BuddhaMode.STOIC,
        userContext: UserContext? = null
    ): BuddhaResponse = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext BuddhaResponse.Error("API key not configured")

        try {
            val systemPrompt = buildSystemPrompt(mode, userContext)
            val fullPrompt = buildFullPrompt(systemPrompt, conversationHistory, userMessage)

            val startTime = System.currentTimeMillis()
            val response = model.generateContent(content { text(fullPrompt) })
            val endTime = System.currentTimeMillis()

            val responseText = response.text
            if (responseText != null) {
                BuddhaResponse.Success(
                    message = responseText,
                    responseTimeMs = endTime - startTime,
                    tokenCount = responseText.split(Regex("\\s+")).size // Approximate
                )
            } else {
                BuddhaResponse.Error("Empty response from AI")
            }
        } catch (e: Exception) {
            BuddhaResponse.Error(e.message ?: "Unknown error occurred")
        }
    }

    private fun buildSystemPrompt(mode: BuddhaMode, context: UserContext?): String {
        val basePrompt = mode.systemPrompt

        val contextAddition = context?.let {
            buildString {
                append("\n\nUser Context:")
                it.currentStreak?.let { streak -> append("\n- Current streak: $streak days") }
                it.totalWordsLearned?.let { words -> append("\n- Words learned: $words") }
                it.recentMood?.let { mood -> append("\n- Recent mood: $mood") }
                it.currentChallenge?.let { challenge -> append("\n- Current challenge: $challenge") }
                it.recentJournalSummary?.let { summary -> append("\n- Recent thoughts: $summary") }
            }
        } ?: ""

        return basePrompt + contextAddition + """
            |
            |Guidelines:
            |- Keep responses concise but meaningful (2-4 paragraphs max)
            |- Use metaphors and stories when appropriate
            |- Ask thoughtful questions to encourage reflection
            |- Never be preachy or condescending
            |- Acknowledge the user's feelings before offering perspective
            |- Draw from philosophical traditions naturally, without excessive quoting
            |- Be warm and human, not robotic
            |- If the user seems distressed, be especially gentle and supportive
        """.trimMargin()
    }

    private fun buildFullPrompt(systemPrompt: String, history: String, userMessage: String): String {
        return buildString {
            append(systemPrompt)
            append("\n\n")
            if (history.isNotBlank()) {
                append("Previous conversation:\n")
                append(history)
                append("\n\n")
            }
            append("User: $userMessage\n\nBuddha:")
        }
    }

    // ==================== Journal Analysis ====================

    /**
     * Analyzes a journal entry and provides philosophical insights.
     */
    suspend fun analyzeJournal(
        journalContent: String,
        mood: String? = null,
        mode: BuddhaMode = BuddhaMode.STOIC
    ): JournalAnalysis = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext JournalAnalysis.Error("API key not configured")

        try {
            val prompt = buildJournalAnalysisPrompt(journalContent, mood, mode)
            val response = model.generateContent(content { text(prompt) })
            val responseText = response.text ?: return@withContext JournalAnalysis.Error("Empty response")

            parseJournalAnalysis(responseText)
        } catch (e: Exception) {
            JournalAnalysis.Error(e.message ?: "Analysis failed")
        }
    }

    private fun buildJournalAnalysisPrompt(content: String, mood: String?, mode: BuddhaMode): String {
        return """
            |You are Buddha, a ${mode.displayName.lowercase()}, analyzing a personal journal entry.
            |
            |Journal Entry:
            |"$content"
            |${mood?.let { "\nStated mood: $it" } ?: ""}
            |
            |Please provide a thoughtful analysis in this exact format:
            |
            |SUMMARY: (1-2 sentences capturing the essence of their writing)
            |
            |REFLECTION: (2-3 sentences of philosophical reflection on their thoughts)
            |
            |SUGGESTION: (1-2 actionable suggestions or questions for reflection)
            |
            |SENTIMENT: (one word: positive, negative, mixed, or neutral)
            |
            |THEMES: (2-4 key themes, comma-separated)
            |
            |Be compassionate, insightful, and avoid being preachy.
        """.trimMargin()
    }

    private fun parseJournalAnalysis(response: String): JournalAnalysis {
        try {
            val sections = mapOf(
                "summary" to extractSection(response, "SUMMARY:"),
                "reflection" to extractSection(response, "REFLECTION:"),
                "suggestion" to extractSection(response, "SUGGESTION:"),
                "sentiment" to extractSection(response, "SENTIMENT:"),
                "themes" to extractSection(response, "THEMES:")
            )

            return JournalAnalysis.Success(
                summary = sections["summary"] ?: "",
                reflection = sections["reflection"] ?: "",
                suggestion = sections["suggestion"] ?: "",
                sentiment = sections["sentiment"] ?: "neutral",
                keyThemes = sections["themes"] ?: ""
            )
        } catch (e: Exception) {
            // If parsing fails, return the raw response as the reflection
            return JournalAnalysis.Success(
                summary = "",
                reflection = response,
                suggestion = "",
                sentiment = "neutral",
                keyThemes = ""
            )
        }
    }

    private fun extractSection(text: String, marker: String): String? {
        val startIndex = text.indexOf(marker, ignoreCase = true)
        if (startIndex == -1) return null

        val contentStart = startIndex + marker.length
        val nextMarkerIndex = listOf("SUMMARY:", "REFLECTION:", "SUGGESTION:", "SENTIMENT:", "THEMES:")
            .filter { it != marker }
            .mapNotNull { m ->
                val idx = text.indexOf(m, contentStart, ignoreCase = true)
                if (idx == -1) null else idx
            }
            .minOrNull() ?: text.length

        return text.substring(contentStart, nextMarkerIndex).trim()
    }

    // ==================== Future Self Analysis ====================

    /**
     * Analyzes a future self letter when it's opened.
     */
    suspend fun analyzeFutureSelfLetter(
        letterContent: String,
        daysAgo: Int,
        goalsIncluded: String? = null
    ): FutureSelfAnalysis = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext FutureSelfAnalysis.Error("API key not configured")

        try {
            val prompt = """
                |You are Buddha, helping someone reflect on a letter they wrote to their future self $daysAgo days ago.
                |
                |The letter they wrote:
                |"$letterContent"
                |${goalsIncluded?.let { "\nGoals/commitments they made: $it" } ?: ""}
                |
                |Provide:
                |ANALYSIS: (2-3 sentences analyzing what their past self was going through and what they hoped for)
                |
                |ENCOURAGEMENT: (2-3 sentences of warm, philosophical encouragement for their journey, acknowledging both growth and ongoing challenges)
                |
                |Be warm, wise, and acknowledge the courage it takes to write to one's future self.
            """.trimMargin()

            val response = model.generateContent(content { text(prompt) })
            val responseText = response.text ?: return@withContext FutureSelfAnalysis.Error("Empty response")

            FutureSelfAnalysis.Success(
                analysis = extractSection(responseText, "ANALYSIS:") ?: responseText,
                encouragement = extractSection(responseText, "ENCOURAGEMENT:") ?: ""
            )
        } catch (e: Exception) {
            FutureSelfAnalysis.Error(e.message ?: "Analysis failed")
        }
    }

    // ==================== Vocabulary Enhancement ====================

    /**
     * Generates a mnemonic and usage notes for a vocabulary word.
     */
    suspend fun enhanceVocabulary(
        word: String,
        meaning: String,
        type: String
    ): VocabularyEnhancement = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext VocabularyEnhancement.Error("API key not configured")

        try {
            val prompt = """
                |Create a memorable learning aid for this ${type.lowercase()}:
                |
                |Word/Phrase: "$word"
                |Meaning: "$meaning"
                |
                |Provide:
                |MNEMONIC: (A creative memory trick or association to remember this)
                |
                |USAGE_NOTES: (Practical tips on when and how to use this, with a brief example)
                |
                |Be creative and make it memorable. Keep each section to 1-2 sentences.
            """.trimMargin()

            val response = model.generateContent(content { text(prompt) })
            val responseText = response.text ?: return@withContext VocabularyEnhancement.Error("Empty response")

            VocabularyEnhancement.Success(
                mnemonic = extractSection(responseText, "MNEMONIC:") ?: "",
                usageNotes = extractSection(responseText, "USAGE_NOTES:") ?: ""
            )
        } catch (e: Exception) {
            VocabularyEnhancement.Error(e.message ?: "Enhancement failed")
        }
    }

    // ==================== Daily Wisdom ====================

    /**
     * Generates personalized daily wisdom based on user context.
     */
    suspend fun generateDailyWisdom(
        context: UserContext?,
        mode: BuddhaMode = BuddhaMode.STOIC
    ): DailyWisdom = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext DailyWisdom.Error("API key not configured")

        try {
            val contextInfo = context?.let {
                buildString {
                    it.currentStreak?.let { append("They have a $it day streak. ") }
                    it.recentMood?.let { append("Recent mood: $it. ") }
                    it.currentChallenge?.let { append("Current focus: $it. ") }
                }
            } ?: ""

            val prompt = """
                |As Buddha (${mode.displayName}), provide today's wisdom.
                |$contextInfo
                |
                |Generate a brief, meaningful message for today that includes:
                |1. An inspiring thought or philosophical insight (2-3 sentences)
                |2. A practical reflection question or micro-action for the day (1 sentence)
                |
                |Be concise, warm, and avoid clichés. Make it feel personal and relevant.
            """.trimMargin()

            val response = model.generateContent(content { text(prompt) })
            val responseText = response.text ?: return@withContext DailyWisdom.Error("Empty response")

            DailyWisdom.Success(responseText)
        } catch (e: Exception) {
            DailyWisdom.Error(e.message ?: "Failed to generate wisdom")
        }
    }
}

// ==================== Data Classes ====================

data class UserContext(
    val currentStreak: Int? = null,
    val totalWordsLearned: Int? = null,
    val recentMood: String? = null,
    val currentChallenge: String? = null,
    val recentJournalSummary: String? = null
)

sealed class BuddhaResponse {
    data class Success(
        val message: String,
        val responseTimeMs: Long,
        val tokenCount: Int
    ) : BuddhaResponse()

    data class Error(val message: String) : BuddhaResponse()
}

sealed class JournalAnalysis {
    data class Success(
        val summary: String,
        val reflection: String,
        val suggestion: String,
        val sentiment: String,
        val keyThemes: String
    ) : JournalAnalysis()

    data class Error(val message: String) : JournalAnalysis()
}

sealed class FutureSelfAnalysis {
    data class Success(
        val analysis: String,
        val encouragement: String
    ) : FutureSelfAnalysis()

    data class Error(val message: String) : FutureSelfAnalysis()
}

sealed class VocabularyEnhancement {
    data class Success(
        val mnemonic: String,
        val usageNotes: String
    ) : VocabularyEnhancement()

    data class Error(val message: String) : VocabularyEnhancement()
}

sealed class DailyWisdom {
    data class Success(val wisdom: String) : DailyWisdom()
    data class Error(val message: String) : DailyWisdom()
}
