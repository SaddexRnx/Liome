package com.example.device

data class DeviceHardwareProfile(
    val deviceModel: String,
    val manufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val cpuArch: String,
    val supportedAbis: List<String>,
    val cpuCores: Int,
    val cpuFeatures: List<String>,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val isLowRamDevice: Boolean,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val hasVulkan: Boolean,
    val vulkanVersion: String,
    val hasNnapi: Boolean,
    val hasOpenCl: Boolean,
    val recommendedThreads: Int,
    val recommendedContextSize: Int,
    val maxRecommendedParamBillions: Float
) {
    val totalRamGb: Float
        get() = totalRamBytes / (1024f * 1024f * 1024f)

    val availableRamGb: Float
        get() = availableRamBytes / (1024f * 1024f * 1024f)

    val totalStorageGb: Float
        get() = totalStorageBytes / (1024f * 1024f * 1024f)

    val availableStorageGb: Float
        get() = availableStorageBytes / (1024f * 1024f * 1024f)

    val is64Bit: Boolean
        get() = supportedAbis.any { it.contains("64") }

    val hardwareReadinessScore: Int
        get() {
            var score = 30
            if (is64Bit) score += 20
            if (cpuCores >= 8) score += 15 else if (cpuCores >= 4) score += 10
            if (totalRamGb >= 7.5f) score += 25 else if (totalRamGb >= 5.5f) score += 18 else if (totalRamGb >= 3.5f) score += 10
            if (availableStorageGb >= 10f) score += 10 else if (availableStorageGb >= 4f) score += 5
            return score.coerceIn(10, 100)
        }

    val readinessTier: String
        get() = when {
            hardwareReadinessScore >= 80 -> "High Performance (Runs 1B–3B smoothly)"
            hardwareReadinessScore >= 55 -> "Standard Mobile (Optimized for 0.5B–1.5B)"
            else -> "Entry Level (Best with <0.5B lightweight models)"
        }
}
