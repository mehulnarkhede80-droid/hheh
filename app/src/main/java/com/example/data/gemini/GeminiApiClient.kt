package com.example.data.gemini

import android.content.Context

object GeminiApiClient {
    private const val SYSTEM_INSTRUCTION =
        "You are TransitPulse Copilot, a senior transportation systems analyst and route optimization strategist. " +
        "You assist transit authorities in analyzing route efficiency, forecasting peak ridership surges, mitigating corridor bottlenecks, and optimizing vehicle headways and fleet sizing. " +
        "Keep answers concise, highly structured, and actionable."

    suspend fun sendChatMessage(
        context: Context,
        history: List<ChatMessage>,
        userMessage: String,
        modelTier: GeminiModelTier
    ): ChatMessage {
        val prompt = buildString {
            appendLine(SYSTEM_INSTRUCTION)
            history.takeLast(8).forEach { message ->
                val role = if (message.sender == MessageSender.USER) "User" else "Assistant"
                appendLine("$role: ${message.text}")
            }
            append("User: ")
            append(userMessage)
        }
        return try {
            val responseText = PuterApiClient.chat(context, prompt, modelTier.modelId)
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = responseText,
                modelUsed = "${modelTier.displayName} via Puter.js"
            )
        } catch (e: Exception) {
            val localResponse = generateLocalFreeResponse(userMessage, modelTier)
            localResponse.copy(
                text = "Puter.js request failed: ${e.localizedMessage ?: "Unknown error"}\n\n${localResponse.text}",
                modelUsed = "${modelTier.displayName} (Local Fallback)"
            )
        }
    }

    private fun generateLocalFreeResponse(userMessage: String, modelTier: GeminiModelTier): ChatMessage {
        val lower = userMessage.lowercase()
        val text = buildString {
            append("💡 **[Free Tier Operations Engine]**\n\n")
            if (lower.contains("rain") || lower.contains("weather")) {
                append("🌧️ **Weather Congestion Impact Strategy:**\n")
                append("• **Demand Surge:** Inclement weather typically increases transit ridership by +18–25% near central interchanges (e.g. Grand Central, Civic Plaza).\n")
                append("• **Dispatch Headway:** Tighten scheduled headway from 12 mins to 6 mins on core corridors to mitigate station overcrowding.\n")
                append("• **Punctuality Buffer:** Add a +4 to +6 minute buffer at arterial signalized junctions to preserve on-time performance.\n")
            } else if (lower.contains("headway") || lower.contains("peak") || lower.contains("frequency")) {
                append("🚌 **Peak Hour Fleet Headway Optimization:**\n")
                append("• **Morning Window (07:00 – 09:00):** Maintain 5–7 min headway on high-density trunk lines (e.g., Metro Red & Crosstown BRT 101).\n")
                append("• **Capacity Target:** Keep vehicle load factor between 70% and 85% to ensure passenger comfort while preventing skipped stops.\n")
                append("• **Short-Turn Vehicles:** Pre-position 2 reserve articulated units at high-boarding hubs for express bypass runs.\n")
            } else if (lower.contains("bottleneck") || lower.contains("delay")) {
                append("⚠️ **Corridor Bottleneck Mitigation:**\n")
                append("• **Pinch Point Detection:** Telemetry highlights major dwell delays at transfer junctions.\n")
                append("• **Recommended Action:** Implement off-board fare validation and all-door boarding to shave 35–45 seconds off dwell times per station.\n")
                append("• **Dedicated Right-of-Way:** Designate peak transit-only lanes along congested surface streets.\n")
            } else {
                append("📊 **Transit Network Efficiency Directives:**\n")
                append("• **Load Balancing:** Shift surplus vehicles from low-occupancy routes (e.g., Shuttle 12 off-peak) to heavily congested trunk corridors.\n")
                append("• **Environmental Savings:** Every 1,000 riders diverted from private automobiles saves approximately 120 kg of CO₂.\n")
                append("• **Data Studio Tip:** You can inject custom CSV/JSON datasets or test extreme monsoon scenarios via the Data Studio tab.\n")
            }
            append("\n*Puter.js could not be reached, so this response uses local transit heuristics.*")
        }

        return ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = text,
            modelUsed = "${modelTier.displayName} (Free Engine)"
        )
    }

    private fun generateLocalAnalysisSnippet(userMessage: String): String {
        return "💡 **Local Analytical Insight:** Historical transit modeling indicates that optimizing vehicle headway to 6 minutes during peak windows reduces passenger platform wait times by 38% and avoids vehicle crowding."
    }
}
