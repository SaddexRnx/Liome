package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.RandomAccessFile

object DeviceProfiler {

    fun profileDevice(context: Context): DeviceHardwareProfile {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalRamBytes = if (memInfo.totalMem > 0) memInfo.totalMem else Runtime.getRuntime().maxMemory()
        val availRamBytes = if (memInfo.availMem > 0) memInfo.availMem else Runtime.getRuntime().freeMemory()
        val isLowRam = memInfo.lowMemory

        // Storage info
        val storagePath = context.filesDir ?: Environment.getDataDirectory()
        val statFs = StatFs(storagePath.path)
        val totalStorageBytes = statFs.blockCountLong * statFs.blockSizeLong
        val availStorageBytes = statFs.availableBlocksLong * statFs.blockSizeLong

        // CPU cores
        val cpuCores = Runtime.getRuntime().availableProcessors()

        // Supported ABIs
        val supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: listOf(Build.CPU_ABI)
        val primaryArch = supportedAbis.firstOrNull() ?: "unknown"

        // CPU features from /proc/cpuinfo
        val cpuFeatures = parseCpuFeatures()

        // Vulkan check
        val packageManager = context.packageManager
        val hasVulkan = packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
        val vulkanVersion = if (hasVulkan) {
            val feature = packageManager.systemAvailableFeatures.firstOrNull {
                it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION
            }
            if (feature != null && feature.version > 0) {
                "v${(feature.version shr 22)}.${(feature.version shr 12) and 0x3ff}.${feature.version and 0xfff}"
            } else {
                "Available"
            }
        } else {
            "Not detected"
        }

        // NNAPI check
        val hasNnapi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

        // OpenCL check (probe common OpenCL library locations on Android)
        val hasOpenCl = checkOpenClAvailable()

        // Thread recommendation:
        // Use performance cores, leaving at least 1-2 cores for Android OS and UI rendering.
        val recommendedThreads = when {
            cpuCores >= 8 -> 4
            cpuCores >= 6 -> 3
            cpuCores >= 4 -> 2
            else -> 1
        }

        // Context size recommendation based on RAM:
        val totalRamGb = totalRamBytes / (1024f * 1024f * 1024f)
        val recommendedContextSize = when {
            totalRamGb >= 7.5f -> 4096
            totalRamGb >= 5.5f -> 2048
            else -> 1024
        }

        // Max recommended model parameter size:
        val maxParams = when {
            totalRamGb >= 11f -> 7.0f
            totalRamGb >= 7.5f -> 3.5f
            totalRamGb >= 5.5f -> 1.5f
            totalRamGb >= 3.5f -> 0.6f
            else -> 0.2f
        }

        return DeviceHardwareProfile(
            deviceModel = Build.MODEL ?: "Unknown",
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            cpuArch = primaryArch,
            supportedAbis = supportedAbis,
            cpuCores = cpuCores,
            cpuFeatures = cpuFeatures,
            totalRamBytes = totalRamBytes,
            availableRamBytes = availRamBytes,
            isLowRamDevice = isLowRam,
            totalStorageBytes = totalStorageBytes,
            availableStorageBytes = availStorageBytes,
            hasVulkan = hasVulkan,
            vulkanVersion = vulkanVersion,
            hasNnapi = hasNnapi,
            hasOpenCl = hasOpenCl,
            recommendedThreads = recommendedThreads,
            recommendedContextSize = recommendedContextSize,
            maxRecommendedParamBillions = maxParams
        )
    }

    private fun parseCpuFeatures(): List<String> {
        val detected = mutableSetOf<String>()
        try {
            val file = File("/proc/cpuinfo")
            if (file.exists() && file.canRead()) {
                file.forEachLine { line ->
                    val lower = line.lowercase()
                    if (lower.startsWith("features") || lower.startsWith("flags")) {
                        val tokens = line.substringAfter(":").trim().split("\\s+".toRegex())
                        for (token in tokens) {
                            val tLower = token.lowercase()
                            if (tLower in listOf("fp", "asimd", "neon", "fphp", "asimdhp", "i8mm", "dotprod", "sve", "crc32", "aes", "sha1", "sha2")) {
                                detected.add(token.uppercase())
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Safe fallback if /proc/cpuinfo is restricted on newer SELinux
        }

        if (detected.isEmpty()) {
            if (Build.SUPPORTED_ABIS.any { it.contains("arm64") }) {
                detected.add("ARM64")
                detected.add("NEON")
                detected.add("ASIMD")
            } else if (Build.SUPPORTED_ABIS.any { it.contains("x86_64") }) {
                detected.add("x86_64")
                detected.add("AVX")
            }
        }
        return detected.toList()
    }

    private fun checkOpenClAvailable(): Boolean {
        val commonPaths = listOf(
            "/system/vendor/lib64/libOpenCL.so",
            "/system/lib64/libOpenCL.so",
            "/vendor/lib64/libOpenCL.so",
            "/system/vendor/lib/libOpenCL.so",
            "/system/lib/libOpenCL.so",
            "/vendor/lib/libOpenCL.so"
        )
        return commonPaths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }
}
