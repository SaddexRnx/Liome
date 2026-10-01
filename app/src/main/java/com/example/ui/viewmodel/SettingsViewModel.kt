package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.data.LocalMindDatabase
import com.example.data.UserPreferencesRepository
import com.example.data.UserSettings
import com.example.model.ModelCatalog
import com.example.model.ModelDownloadManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val totalModelsSizeBytes: Long = 0L,
    val conversationCount: Int = 0,
    val totalMessageCount: Int = 0,
    val isClearingData: Boolean = false,
    val showClearDialog: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferencesRepository(application)
    private val db = LocalMindDatabase.getDatabase(application)
    private val chatRepo = ChatRepository(db.chatDao())
    private val downloadManager = ModelDownloadManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPrefs.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        refreshStorageStats()
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val stats = chatRepo.getStats()
            val modelsSize = downloadManager.getTotalInstalledSizeBytes()
            _uiState.update {
                it.copy(
                    conversationCount = stats.first,
                    totalMessageCount = stats.second,
                    totalModelsSizeBytes = modelsSize
                )
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        userPrefs.setThemeMode(mode)
    }

    fun setThemeStyle(style: com.example.ui.theme.LocalMindThemeStyle) {
        userPrefs.setThemeStyle(style)
    }

    fun updateCpuThreads(threads: Int) {
        userPrefs.updateInferenceConfig(threads = threads)
    }

    fun updateContextSize(contextSize: Int) {
        userPrefs.updateInferenceConfig(contextSize = contextSize)
    }

    fun updateTemperature(temp: Float) {
        userPrefs.updateInferenceConfig(temperature = temp)
    }

    fun updateTopP(topP: Float) {
        userPrefs.updateInferenceConfig(topP = topP)
    }

    fun updateRepeatPenalty(penalty: Float) {
        userPrefs.updateInferenceConfig(repeatPenalty = penalty)
    }

    fun toggleMemoryLock() {
        val current = _uiState.value.settings.memoryLock
        userPrefs.updateInferenceConfig(memoryLock = !current)
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            chatRepo.clearAllConversations()
            refreshStorageStats()
        }
    }

    fun deleteAllModels() {
        viewModelScope.launch {
            ModelCatalog.curatedModels.forEach {
                downloadManager.deleteModel(it.filename)
            }
            refreshStorageStats()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            chatRepo.clearAllConversations()
            ModelCatalog.curatedModels.forEach {
                downloadManager.deleteModel(it.filename)
            }
            userPrefs.setOnboardingCompleted(false)
            refreshStorageStats()
        }
    }
}
