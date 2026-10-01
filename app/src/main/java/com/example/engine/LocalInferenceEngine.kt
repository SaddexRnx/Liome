package com.example.engine

import kotlinx.coroutines.flow.Flow
import java.io.File

interface LocalInferenceEngine {
    val engineName: String
    val isModelLoaded: Boolean
    val currentModelPath: String?

    fun loadModel(modelPath: String, config: InferenceConfig): Flow<ModelLoadProgress>
    fun unloadModel()
    fun generate(parameters: GenerationParameters): Flow<InferenceEvent>
    fun stopGeneration()
    fun getModelInfo(modelFile: File): GgufModelMetadata
    fun getPerformanceStats(): EnginePerformanceStats
    fun runHardwareBenchmark(threads: Int): Flow<BenchmarkResult>
}
