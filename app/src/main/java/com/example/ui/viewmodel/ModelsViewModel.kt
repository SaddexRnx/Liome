package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UserPreferencesRepository
import com.example.device.DeviceProfiler
import com.example.engine.GgufBinaryParser
import com.example.engine.GgufModelMetadata
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class ModelsUiState(
    val models: List<ModelItem> = emptyList(),
    val activeModelId: String = "smollm2-135m-instruct",
    val totalInstalledSizeBytes: Long = 0L,
    val availableStorageBytes: Long = 0L,
    val downloadState: DownloadState = DownloadState.Idle,
    val verifiedMetadata: GgufModelMetadata? = null,
    val showVerifyDialog: Boolean = false,
    val verifyTargetModel: ModelItem? = null
)

class ModelsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferencesRepository(application)
    val downloadManager = ModelDownloadManager(application)
    private val deviceProfile = DeviceProfiler.profileDevice(application)

    private val _uiState = MutableStateFlow(ModelsUiState())
    val uiState: StateFlow<ModelsUiState> = _uiState.asStateFlow()

    init {
        // Observe downloads
        viewModelScope.launch {
            downloadManager.downloadState.collect { dState ->
                _uiState.update { it.copy(downloadState = dState) }
                if (dState is DownloadState.Completed) {
                    refreshModels()
                }
            }
        }

        // Observe user preferences
        viewModelScope.launch {
            userPrefs.settings.collect { settings ->
                _uiState.update { it.copy(activeModelId = settings.activeModelId) }
                refreshModels()
            }
        }
    }

    fun refreshModels() {
        val prefs = userPrefs.settings.value
        val userPreferences = UserPreferences(
            mainIntent = prefs.userIntent,
            priority = prefs.userPriority,
            storageBudget = prefs.userStorageBudget,
            answerStyle = prefs.userAnswerStyle
        )

        val updated = ModelCatalog.curatedModels.map { model ->
            val file = downloadManager.getModelFile(model.filename)
            val isInstalled = file.exists() && file.length() > 1024 * 1024
            val localSize = if (isInstalled) file.length() else 0L
            val rec = ModelRecommendationEngine.evaluate(model, deviceProfile, userPreferences)

            model.copy(
                isInstalled = isInstalled,
                isActive = model.id == prefs.activeModelId,
                localFilePath = if (isInstalled) file.absolutePath else null,
                localSizeBytes = localSize,
                recommendation = rec
            )
        }

        _uiState.update {
            it.copy(
                models = updated,
                totalInstalledSizeBytes = downloadManager.getTotalInstalledSizeBytes(),
                availableStorageBytes = deviceProfile.availableStorageBytes
            )
        }
    }

    fun startDownload(model: ModelItem) {
        downloadManager.startDownload(model)
    }

    fun cancelDownload() {
        downloadManager.cancelDownload()
    }

    fun activateModel(modelId: String) {
        userPrefs.setActiveModelId(modelId)
    }

    fun deleteModel(model: ModelItem) {
        downloadManager.deleteModel(model.filename)
        refreshModels()
    }

    fun inspectAndVerifyModel(model: ModelItem) {
        val file = downloadManager.getModelFile(model.filename)
        if (file.exists()) {
            val metadata = GgufBinaryParser.parse(file)
            _uiState.update {
                it.copy(
                    verifiedMetadata = metadata,
                    verifyTargetModel = model,
                    showVerifyDialog = true
                )
            }
        }
    }

    fun dismissVerifyDialog() {
        _uiState.update { it.copy(showVerifyDialog = false, verifyTargetModel = null, verifiedMetadata = null) }
    }
}
