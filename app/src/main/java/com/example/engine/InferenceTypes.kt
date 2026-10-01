package com.example.engine

data class InferenceConfig(
    val threads: Int = 4,
    val contextSize: Int = 2048,
    val batchSize: Int = 512,
    val memoryLock: Boolean = false,
    val useMmap: Boolean = true,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val repeatPenalty: Float = 1.1f,
    val maxTokens: Int = 1024
)

data class GenerationParameters(
    val prompt: String,
    val systemPrompt: String = "",
    val conversationHistory: List<Pair<String, String>> = emptyList(), // role to content
    val config: InferenceConfig = InferenceConfig()
)

sealed class ModelLoadProgress {
    data class Loading(val stage: String, val progressFraction: Float) : ModelLoadProgress()
    data class Success(val modelPath: String, val modelName: String, val contextSize: Int, val memoryUsedMb: Long) : ModelLoadProgress()
    data class Error(val message: String, val cause: Throwable? = null) : ModelLoadProgress()
}

sealed class InferenceEvent {
    data class Token(val text: String, val tokenIndex: Int) : InferenceEvent()
    data class Completed(
        val totalText: String,
        val promptTokens: Int,
        val completionTokens: Int,
        val totalDurationMs: Long,
        val tokensPerSecond: Float,
        val timeToFirstTokenMs: Long,
        val isFallback: Boolean = false
    ) : InferenceEvent()
    data class Error(val message: String) : InferenceEvent()
}

data class EnginePerformanceStats(
    val isModelLoaded: Boolean,
    val loadedModelName: String = "",
    val activeThreads: Int = 0,
    val contextSize: Int = 0,
    val memoryMappedMb: Long = 0,
    val lastTokensPerSec: Float = 0f,
    val backendDescription: String = "CPU (NEON Vectorized)",
    val isNativeLoaded: Boolean = false,
    val engineDiagnosticMessage: String = ""
)

data class BenchmarkResult(
    val stage: String,
    val progressFraction: Float,
    val cpuGflops: Float = 0f,
    val memoryBandwidthMbPerSec: Float = 0f,
    val estimatedTokensPerSec: Float = 0f,
    val isComplete: Boolean = false,
    val summary: String = ""
)
