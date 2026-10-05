package com.example.data.gemini

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val groundingSources: List<GroundingCitation> = emptyList(),
    val isError: Boolean = false
)

enum class MessageSender {
    USER,
    ASSISTANT
}

data class GroundingCitation(
    val title: String,
    val uri: String? = null,
    val type: CitationType = CitationType.WEB
)

enum class CitationType {
    WEB,
    MAPS
}

enum class GeminiModelTier(
    val modelId: String,
    val displayName: String,
    val description: String,
    val supportsSearch: Boolean = false,
    val supportsMaps: Boolean = false
) {
    FLASH_GENERAL(
        modelId = "gemini-3.5-flash",
        displayName = "3.5 Flash (General + Search & Maps)",
        description = "General tasks with Google Search & Maps Grounding",
        supportsSearch = true,
        supportsMaps = true
    ),
    FLASH_LITE_FAST(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "3.1 Flash Lite (Fast)",
        description = "Low latency telemetry queries and rapid Q&A",
        supportsSearch = false,
        supportsMaps = false
    ),
    PRO_COMPLEX(
        modelId = "gemini-3.1-pro-preview",
        displayName = "3.1 Pro (Complex Optimization)",
        description = "Advanced network optimization and statistical reasoning",
        supportsSearch = false,
        supportsMaps = false
    )
}
