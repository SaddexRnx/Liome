package com.example.model

import com.example.device.DeviceHardwareProfile

data class UserPreferences(
    val mainIntent: String = "General assistant",
    val priority: String = "Balanced", // "Faster responses", "Better answers", "Balanced"
    val storageBudget: String = "1–3 GB", // "Less than 1 GB", "1–3 GB", "3–6 GB", "More than 6 GB"
    val answerStyle: String = "Balanced" // "Short and fast answers", "More detailed reasoning", "Balanced"
)

object ModelRecommendationEngine {

    fun evaluate(
        model: ModelItem,
        profile: DeviceHardwareProfile,
        preferences: UserPreferences
    ): ModelRecommendation {
        val totalRamBytes = profile.totalRamBytes
        val availStorageBytes = profile.availableStorageBytes

        // 1. Hardware RAM Check
        // Rule: Android OS and system services need at least ~1.8-2.2 GB of free headroom
        val requiredHeadroom = 1.8 * 1024 * 1024 * 1024
        val maxModelMemoryAllowed = maxOf(0.0, totalRamBytes - requiredHeadroom)

        val ramDeficit = model.requiredRamBytes > maxModelMemoryAllowed
        val storageDeficit = model.minStorageBytes > availStorageBytes

        // 2. Storage user preference ceiling in bytes
        val userStorageCapBytes = when (preferences.storageBudget) {
            "Less than 1 GB" -> 900L * 1024 * 1024
            "1–3 GB" -> 3000L * 1024 * 1024
            "3–6 GB" -> 6000L * 1024 * 1024
            else -> Long.MAX_VALUE
        }
        val exceedsUserStorageBudget = model.downloadSizeBytes > userStorageCapBytes

        if (ramDeficit) {
            return ModelRecommendation(
                status = SuitabilityStatus.NOT_SUITABLE,
                badgeLabel = "High RAM Requirement",
                score = 15,
                explanation = "Requires ${model.requiredRamFormatted} of dedicated memory (estimated requirement). Available device RAM may be insufficient for stable execution.",
                isCompatible = false
            )
        }

        if (storageDeficit) {
            return ModelRecommendation(
                status = SuitabilityStatus.REQUIRES_MORE_STORAGE,
                badgeLabel = "Requires more storage",
                score = 25,
                explanation = "Requires ${model.downloadSizeFormatted} of free storage space. Available device storage: ${String.format("%.1f GB", profile.availableStorageGb)}.",
                isCompatible = false
            )
        }

        // Calculate preference alignment score (0 - 100)
        var score = 70

        // Use case match
        val intentMatch = model.targetUseCases.any {
            it.contains(preferences.mainIntent, ignoreCase = true) ||
            preferences.mainIntent.contains(it, ignoreCase = true)
        }
        if (intentMatch) score += 15

        // Priority weighting
        when (preferences.priority) {
            "Faster responses" -> {
                if (model.downloadSizeBytes < 500L * 1024 * 1024) score += 15
                else score -= 10
            }
            "Better answers" -> {
                if (model.downloadSizeBytes >= 800L * 1024 * 1024) score += 15
                else score -= 5
            }
            else -> { // Balanced
                if (model.downloadSizeBytes in (200L * 1024 * 1024)..(1200L * 1024 * 1024)) score += 10
            }
        }

        // Answer style
        when (preferences.answerStyle) {
            "Short and fast answers" -> {
                if (model.id.contains("135m") || model.id.contains("360m")) score += 10
            }
            "More detailed reasoning" -> {
                if (model.id.contains("1b") || model.id.contains("2b") || model.id.contains("phi")) score += 12
            }
            else -> {}
        }

        if (exceedsUserStorageBudget) {
            score -= 20
        }

        // Assign neutral factual label
        val (status, badgeLabel, explanation) = when {
            exceedsUserStorageBudget -> {
                Triple(
                    SuitabilityStatus.REQUIRES_MORE_STORAGE,
                    "Requires more storage",
                    "This model size exceeds your selected storage preference (${preferences.storageBudget}), but fits available device storage."
                )
            }
            preferences.priority == "Faster responses" && model.downloadSizeBytes < 400L * 1024 * 1024 -> {
                Triple(
                    SuitabilityStatus.FAST,
                    "Suggested Model",
                    "Suggested based on your selected preferences and available device information."
                )
            }
            model.downloadSizeBytes < 200L * 1024 * 1024 -> {
                Triple(
                    SuitabilityStatus.LIGHTWEIGHT,
                    "Suggested Model",
                    "Suggested based on your selected preferences and available device information."
                )
            }
            model.downloadSizeBytes > 1500L * 1024 * 1024 -> {
                Triple(
                    SuitabilityStatus.HIGH_QUALITY_SLOWER,
                    "Suggested Model",
                    "Suggested based on your selected preferences and available device information."
                )
            }
            else -> {
                Triple(
                    SuitabilityStatus.RECOMMENDED,
                    "Suggested Model",
                    "Suggested based on your selected preferences and available device information."
                )
            }
        }

        return ModelRecommendation(
            status = status,
            badgeLabel = badgeLabel,
            score = score.coerceIn(10, 99),
            explanation = explanation,
            isCompatible = true
        )
    }
}
