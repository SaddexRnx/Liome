package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UserPreferencesRepository
import com.example.device.DeviceHardwareProfile
import com.example.device.DeviceProfiler
import com.example.engine.*
import com.example.model.ModelCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val deviceProfile: DeviceHardwareProfile,
    val engineStats: EnginePerformanceStats,
    val activeModelName: String = "SmolLM2 135M",
    val isBenchmarking: Boolean = false,
    val benchmarkResult: BenchmarkResult? = null,
    val showAdvancedTuning: Boolean = false
)

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferencesRepository(application)
    private val inferenceEngine: LocalInferenceEngine = LocalMindEngine()
    private val initialProfile = DeviceProfiler.profileDevice(application)

    private val _uiState = MutableStateFlow(
        PerformanceUiState(
            deviceProfile = initialProfile,
            engineStats = inferenceEngine.getPerformanceStats()
        )
    )
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPrefs.settings.collect { settings ->
                val modelItem = ModelCatalog.curatedModels.find { it.id == settings.activeModelId }
                _uiState.update {
                    it.copy(
                        activeModelName = modelItem?.name ?: "SmolLM2 135M",
                        engineStats = inferenceEngine.getPerformanceStats().copy(
                            activeThreads = settings.cpuThreads,
                            contextSize = settings.contextSize
                        )
                    )
                }
            }
        }
    }

    fun runBenchmark() {
        if (_uiState.value.isBenchmarking) return
        _uiState.update { it.copy(isBenchmarking = true) }

        viewModelScope.launch {
            val threads = _uiState.value.engineStats.activeThreads.coerceAtLeast(1)
            inferenceEngine.runHardwareBenchmark(threads).collect { result ->
                _uiState.update {
                    it.copy(
                        benchmarkResult = result,
                        isBenchmarking = !result.isComplete
                    )
                }
            }
        }
    }

    fun toggleAdvancedTuning() {
        _uiState.update { it.copy(showAdvancedTuning = !it.showAdvancedTuning) }
    }
}
