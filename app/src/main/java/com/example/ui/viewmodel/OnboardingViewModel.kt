package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UserPreferencesRepository
import com.example.device.DeviceHardwareProfile
import com.example.device.DeviceProfiler
import com.example.model.ModelCatalog
import com.example.model.ModelItem
import com.example.model.ModelRecommendation
import com.example.model.ModelRecommendationEngine
import com.example.model.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object DeviceAnalysis : OnboardingStep()
    object SetupModeChoice : OnboardingStep()
    object SimpleQuestionnaire : OnboardingStep()
    object AdvancedCatalog : OnboardingStep()
    object RecommendationReady : OnboardingStep()
}

data class DeviceScanState(
    val stageText: String = "Initializing device scanner...",
    val progressFraction: Float = 0f,
    val isComplete: Boolean = false,
    val currentAspect: String = "Memory"
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)

    private val _currentStep = MutableStateFlow<OnboardingStep>(OnboardingStep.Welcome)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private val _deviceProfile = MutableStateFlow<DeviceHardwareProfile?>(null)
    val deviceProfile: StateFlow<DeviceHardwareProfile?> = _deviceProfile.asStateFlow()

    private val _scanState = MutableStateFlow(DeviceScanState())
    val scanState: StateFlow<DeviceScanState> = _scanState.asStateFlow()

    // Questionnaire state
    private val _selectedIntent = MutableStateFlow("General assistant")
    val selectedIntent = _selectedIntent.asStateFlow()

    private val _selectedPriority = MutableStateFlow("Balanced")
    val selectedPriority = _selectedPriority.asStateFlow()

    private val _selectedStorage = MutableStateFlow("1–3 GB")
    val selectedStorage = _selectedStorage.asStateFlow()

    private val _selectedStyle = MutableStateFlow("Balanced")
    val selectedStyle = _selectedStyle.asStateFlow()

    private val _recommendedModel = MutableStateFlow<Pair<ModelItem, ModelRecommendation>?>(null)
    val recommendedModel: StateFlow<Pair<ModelItem, ModelRecommendation>?> = _recommendedModel.asStateFlow()

    init {
        // Pre-profile in background
        viewModelScope.launch {
            _deviceProfile.value = DeviceProfiler.profileDevice(getApplication())
        }
    }

    fun setStep(step: OnboardingStep) {
        _currentStep.value = step
        if (step is OnboardingStep.DeviceAnalysis && !_scanState.value.isComplete) {
            startDeviceAnalysis()
        }
    }

    fun startDeviceAnalysis() {
        viewModelScope.launch {
            val stages = listOf(
                Pair("Checking memory and available RAM headroom...", "Memory"),
                Pair("Checking processor architecture & CPU topology...", "Processor"),
                Pair("Checking vector acceleration (NEON / Vulkan)...", "Acceleration"),
                Pair("Checking available storage capacity...", "Storage"),
                Pair("Synthesizing device readiness...", "Compatibility")
            )

            for ((index, stage) in stages.withIndex()) {
                val progress = (index + 1).toFloat() / stages.size
                _scanState.value = DeviceScanState(
                    stageText = stage.first,
                    progressFraction = progress * 0.9f,
                    isComplete = false,
                    currentAspect = stage.second
                )
                delay(400)
            }

            val profile = DeviceProfiler.profileDevice(getApplication())
            _deviceProfile.value = profile

            _scanState.value = DeviceScanState(
                stageText = "Your device is ready.",
                progressFraction = 1.0f,
                isComplete = true,
                currentAspect = "Ready"
            )
        }
    }

    fun setIntent(intent: String) { _selectedIntent.value = intent }
    fun setPriority(priority: String) { _selectedPriority.value = priority }
    fun setStorage(storage: String) { _selectedStorage.value = storage }
    fun setStyle(style: String) { _selectedStyle.value = style }

    fun computeRecommendation() {
        val profile = _deviceProfile.value ?: DeviceProfiler.profileDevice(getApplication())
        val prefs = UserPreferences(
            mainIntent = _selectedIntent.value,
            priority = _selectedPriority.value,
            storageBudget = _selectedStorage.value,
            answerStyle = _selectedStyle.value
        )

        // Evaluate all catalog models
        val scored = ModelCatalog.curatedModels.map { model ->
            val rec = ModelRecommendationEngine.evaluate(model, profile, prefs)
            Pair(model, rec)
        }.sortedByDescending { it.second.score }

        // Pick top compatible model
        val best = scored.firstOrNull { it.second.isCompatible } ?: scored.first()
        _recommendedModel.value = best
        _currentStep.value = OnboardingStep.RecommendationReady
    }

    fun selectModelDirectly(model: ModelItem) {
        val profile = _deviceProfile.value ?: DeviceProfiler.profileDevice(getApplication())
        val prefs = UserPreferences(
            mainIntent = _selectedIntent.value,
            priority = _selectedPriority.value,
            storageBudget = _selectedStorage.value,
            answerStyle = _selectedStyle.value
        )
        val rec = ModelRecommendationEngine.evaluate(model, profile, prefs)
        _recommendedModel.value = Pair(model, rec)
        _currentStep.value = OnboardingStep.RecommendationReady
    }

    fun finishOnboarding(selectedModelId: String) {
        userPrefsRepo.saveQuestionnaireAnswers(
            intent = _selectedIntent.value,
            priority = _selectedPriority.value,
            storage = _selectedStorage.value,
            style = _selectedStyle.value
        )
        userPrefsRepo.setActiveModelId(selectedModelId)
        userPrefsRepo.setOnboardingCompleted(true)
    }
}
