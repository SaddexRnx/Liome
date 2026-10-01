package com.example.model

enum class SuitabilityStatus {
    RECOMMENDED,
    FAST,
    HIGH_QUALITY_SLOWER,
    LIGHTWEIGHT,
    REQUIRES_MORE_STORAGE,
    NOT_SUITABLE
}

data class ModelRecommendation(
    val status: SuitabilityStatus,
    val badgeLabel: String,
    val score: Int,
    val explanation: String,
    val isCompatible: Boolean
)

data class ModelItem(
    val id: String,
    val name: String,
    val parameterCount: String,
    val quantization: String,
    val downloadSizeBytes: Long,
    val requiredRamBytes: Long,
    val minStorageBytes: Long,
    val contextSize: Int,
    val tagline: String,
    val description: String,
    val targetUseCases: List<String>,
    val downloadUrl: String,
    val sha256Checksum: String = "",
    val filename: String,
    val isInstalled: Boolean = false,
    val isActive: Boolean = false,
    val localFilePath: String? = null,
    val localSizeBytes: Long = 0L,
    val isVerified: Boolean = false,
    val recommendation: ModelRecommendation? = null
) {
    val downloadSizeMb: Float
        get() = downloadSizeBytes / (1024f * 1024f)

    val downloadSizeFormatted: String
        get() = if (downloadSizeBytes >= 1024L * 1024 * 1024) {
            String.format("%.1f GB", downloadSizeBytes / (1024f * 1024 * 1024))
        } else {
            String.format("%.0f MB", downloadSizeBytes / (1024f * 1024))
        }

    val requiredRamFormatted: String
        get() = String.format("%.1f GB", requiredRamBytes / (1024f * 1024 * 1024))
}
