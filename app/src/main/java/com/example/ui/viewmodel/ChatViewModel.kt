package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.data.LocalMindDatabase
import com.example.data.UserPreferencesRepository
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.engine.*
import com.example.model.ModelCatalog
import com.example.model.ModelDownloadManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveChatUiState(
    val activeConversation: ConversationEntity? = null,
    val messages: List<MessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingContent: String = "",
    val streamingTokensPerSec: Float = 0f,
    val streamingTokenCount: Int = 0,
    val activeModelName: String = "SmolLM2 135M",
    val activeModelId: String = "smollm2-135m-instruct",
    val isModelLoaded: Boolean = false,
    val isNativeEngineAvailable: Boolean = false,
    val engineDiagnostic: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LocalMindDatabase.getDatabase(application)
    private val repository = ChatRepository(db.chatDao())
    private val userPrefs = UserPreferencesRepository(application)
    private val downloadManager = ModelDownloadManager(application)
    private val inferenceEngine: LocalInferenceEngine = LocalMindEngine()

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    private val _uiState = MutableStateFlow(ActiveChatUiState())
    val uiState: StateFlow<ActiveChatUiState> = _uiState.asStateFlow()

    private var activeGenerationJob: Job? = null

    init {
        // Observe preferences to sync active model
        viewModelScope.launch {
            userPrefs.settings.collect { settings ->
                val modelItem = ModelCatalog.curatedModels.find { it.id == settings.activeModelId }
                    ?: ModelCatalog.curatedModels.first()
                val isNativeLinkAvailable = NativeLlamaBridge.isAvailable

                _uiState.update {
                    it.copy(
                        activeModelName = modelItem.name,
                        activeModelId = modelItem.id,
                        isNativeEngineAvailable = isNativeLinkAvailable,
                        engineDiagnostic = inferenceEngine.getPerformanceStats().engineDiagnosticMessage
                    )
                }

                // Check if model file exists and native bridge is linked
                val file = downloadManager.getModelFile(modelItem.filename)
                if (file.exists() && isNativeLinkAvailable) {
                    inferenceEngine.loadModel(
                        modelPath = file.absolutePath,
                        config = InferenceConfig(
                            threads = settings.cpuThreads,
                            contextSize = settings.contextSize,
                            temperature = settings.temperature,
                            topP = settings.topP
                        )
                    ).collect { progress ->
                        if (progress is ModelLoadProgress.Success) {
                            _uiState.update { it.copy(isModelLoaded = true) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isModelLoaded = false) } // Not loaded into RAM
                }
            }
        }

        // Auto select or create initial conversation if list changes
        viewModelScope.launch {
            conversations.collect { list ->
                if (_activeConversationId.value == null && list.isNotEmpty()) {
                    selectConversation(list.first().id)
                } else if (_activeConversationId.value != null) {
                    val current = list.find { it.id == _activeConversationId.value }
                    _uiState.update { it.copy(activeConversation = current) }
                }
            }
        }
    }

    fun selectConversation(conversationId: Long) {
        _activeConversationId.value = conversationId
        viewModelScope.launch {
            repository.getConversation(conversationId).collect { conv ->
                _uiState.update { it.copy(activeConversation = conv) }
            }
        }
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun startNewConversation(title: String = "New Conversation") {
        viewModelScope.launch {
            val convId = repository.createConversation(
                title = title,
                modelId = _uiState.value.activeModelId
            )
            selectConversation(convId)
        }
    }

    fun renameConversation(id: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameConversation(id, newTitle)
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    _activeConversationId.value = null
                    _uiState.update { it.copy(activeConversation = null, messages = emptyList()) }
                }
            }
        }
    }

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        viewModelScope.launch {
            var convId = _activeConversationId.value
            if (convId == null) {
                val title = if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
                convId = repository.createConversation(title, _uiState.value.activeModelId)
                selectConversation(convId)
            }

            // Save user message
            repository.saveMessage(
                conversationId = convId,
                role = "user",
                content = trimmed
            )

            // Auto title first message if title was default
            val currentConv = repository.getConversationOnce(convId)
            if (currentConv?.title == "New Conversation") {
                val newTitle = if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
                repository.renameConversation(convId, newTitle)
            }

            // Start generation
            generateResponse(convId, trimmed)
        }
    }

    fun stopGeneration() {
        inferenceEngine.stopGeneration()
        activeGenerationJob?.cancel()
        _uiState.update { it.copy(isGenerating = false, streamingContent = "") }
    }

    fun regenerateLastMessage() {
        val msgs = _uiState.value.messages
        if (msgs.isEmpty() || _uiState.value.isGenerating) return
        val convId = _activeConversationId.value ?: return

        val lastUserMsg = msgs.lastOrNull { it.role == "user" } ?: return
        // If last message was assistant, delete it
        val lastMsg = msgs.last()
        if (lastMsg.role == "assistant") {
            viewModelScope.launch {
                repository.deleteMessage(lastMsg.id)
                generateResponse(convId, lastUserMsg.content)
            }
        } else {
            generateResponse(convId, lastUserMsg.content)
        }
    }

    private fun generateResponse(conversationId: Long, prompt: String) {
        activeGenerationJob?.cancel()
        _uiState.update {
            it.copy(
                isGenerating = true,
                streamingContent = "",
                streamingTokensPerSec = 0f,
                streamingTokenCount = 0
            )
        }

        activeGenerationJob = viewModelScope.launch {
            val history = repository.getMessagesOnce(conversationId).map { it.role to it.content }
            val settings = userPrefs.settings.value

            val params = GenerationParameters(
                prompt = prompt,
                systemPrompt = _uiState.value.activeConversation?.systemPrompt ?: "",
                conversationHistory = history.takeLast(10),
                config = InferenceConfig(
                    threads = settings.cpuThreads,
                    contextSize = settings.contextSize,
                    temperature = settings.temperature,
                    topP = settings.topP,
                    repeatPenalty = settings.repeatPenalty
                )
            )

            val fullTextBuilder = StringBuilder()
            var lastTps = 0f
            var promptTokens = 0
            var completionTokens = 0
            var latencyMs = 0L

            inferenceEngine.generate(params).collect { event ->
                when (event) {
                    is InferenceEvent.Token -> {
                        fullTextBuilder.append(event.text)
                        _uiState.update {
                            it.copy(
                                streamingContent = fullTextBuilder.toString(),
                                streamingTokenCount = event.tokenIndex
                            )
                        }
                    }
                    is InferenceEvent.Completed -> {
                        lastTps = event.tokensPerSecond
                        promptTokens = event.promptTokens
                        completionTokens = event.completionTokens
                        latencyMs = event.timeToFirstTokenMs

                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                streamingContent = "",
                                streamingTokensPerSec = lastTps
                            )
                        }

                        // Save completed assistant message to Room
                        repository.saveMessage(
                            conversationId = conversationId,
                            role = "assistant",
                            content = event.totalText,
                            tokensPerSecond = event.tokensPerSecond,
                            promptTokens = event.promptTokens,
                            completionTokens = event.completionTokens,
                            latencyMs = event.timeToFirstTokenMs,
                            isStreaming = false
                        )
                    }
                    is InferenceEvent.Error -> {
                        _uiState.update { it.copy(isGenerating = false, streamingContent = "") }
                        repository.saveMessage(
                            conversationId = conversationId,
                            role = "assistant",
                            content = "⚠️ Engine error: ${event.message}"
                        )
                    }
                }
            }
        }
    }
}
