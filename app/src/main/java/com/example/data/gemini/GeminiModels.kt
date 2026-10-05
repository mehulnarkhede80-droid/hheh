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
    val description: String
) {
    FLASH_GENERAL(
        modelId = "gemini-3.8-flash",
        displayName = "3.8 Flash",
        description = "Fast general-purpose chat"
    ),
    FLASH_LITE_FAST(
        modelId = "gemini-3.5-flash-lite",
        displayName = "3.5 Flash Lite",
        description = "Fast, cost-efficient transit questions"
    ),
    PRO_COMPLEX(
        modelId = "gemini-3.1-pro-preview",
        displayName = "3.1 Pro",
        description = "Complex network optimization"
    )
}
