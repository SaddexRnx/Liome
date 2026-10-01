package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalMindThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val hasCompletedOnboarding: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val themeStyle: LocalMindThemeStyle = LocalMindThemeStyle.MINIMAL_EDITORIAL,
    val activeModelId: String = "smollm2-135m-instruct",
    val cpuThreads: Int = 4,
    val contextSize: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val repeatPenalty: Float = 1.1f,
    val memoryLock: Boolean = false,
    val userIntent: String = "General assistant",
    val userPriority: String = "Balanced",
    val userStorageBudget: String = "1–3 GB",
    val userAnswerStyle: String = "Balanced"
)

class UserPreferencesRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("localmind_user_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: UserPreferencesRepository(context.applicationContext).also { instance = it }
            }
        }

        operator fun invoke(context: Context): UserPreferencesRepository = getInstance(context)
    }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeName = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeName)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }

        val styleName = prefs.getString("theme_style", LocalMindThemeStyle.MINIMAL_EDITORIAL.name) ?: LocalMindThemeStyle.MINIMAL_EDITORIAL.name
        val themeStyle = try {
            LocalMindThemeStyle.valueOf(styleName)
        } catch (_: Exception) {
            LocalMindThemeStyle.MINIMAL_EDITORIAL
        }

        val defaultThreads = maxOf(1, Runtime.getRuntime().availableProcessors() / 2)

        return UserSettings(
            hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false),
            themeMode = themeMode,
            themeStyle = themeStyle,
            activeModelId = prefs.getString("active_model_id", "smollm2-135m-instruct") ?: "smollm2-135m-instruct",
            cpuThreads = prefs.getInt("cpu_threads", defaultThreads),
            contextSize = prefs.getInt("context_size", 2048),
            temperature = prefs.getFloat("temperature", 0.7f),
            topP = prefs.getFloat("top_p", 0.9f),
            repeatPenalty = prefs.getFloat("repeat_penalty", 1.1f),
            memoryLock = prefs.getBoolean("memory_lock", false),
            userIntent = prefs.getString("user_intent", "General assistant") ?: "General assistant",
            userPriority = prefs.getString("user_priority", "Balanced") ?: "Balanced",
            userStorageBudget = prefs.getString("user_storage_budget", "1–3 GB") ?: "1–3 GB",
            userAnswerStyle = prefs.getString("user_answer_style", "Balanced") ?: "Balanced"
        )
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding", completed).apply()
        _settings.value = _settings.value.copy(hasCompletedOnboarding = completed)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setThemeStyle(style: LocalMindThemeStyle) {
        prefs.edit().putString("theme_style", style.name).apply()
        _settings.value = _settings.value.copy(themeStyle = style)
    }

    fun setActiveModelId(modelId: String) {
        prefs.edit().putString("active_model_id", modelId).apply()
        _settings.value = _settings.value.copy(activeModelId = modelId)
    }

    fun updateInferenceConfig(
        threads: Int? = null,
        contextSize: Int? = null,
        temperature: Float? = null,
        topP: Float? = null,
        repeatPenalty: Float? = null,
        memoryLock: Boolean? = null
    ) {
        val editor = prefs.edit()
        threads?.let { editor.putInt("cpu_threads", it) }
        contextSize?.let { editor.putInt("context_size", it) }
        temperature?.let { editor.putFloat("temperature", it) }
        topP?.let { editor.putFloat("top_p", it) }
        repeatPenalty?.let { editor.putFloat("repeat_penalty", it) }
        memoryLock?.let { editor.putBoolean("memory_lock", it) }
        editor.apply()

        _settings.value = _settings.value.copy(
            cpuThreads = threads ?: _settings.value.cpuThreads,
            contextSize = contextSize ?: _settings.value.contextSize,
            temperature = temperature ?: _settings.value.temperature,
            topP = topP ?: _settings.value.topP,
            repeatPenalty = repeatPenalty ?: _settings.value.repeatPenalty,
            memoryLock = memoryLock ?: _settings.value.memoryLock
        )
    }

    fun saveQuestionnaireAnswers(
        intent: String,
        priority: String,
        storage: String,
        style: String
    ) {
        prefs.edit()
            .putString("user_intent", intent)
            .putString("user_priority", priority)
            .putString("user_storage_budget", storage)
            .putString("user_answer_style", style)
            .apply()

        _settings.value = _settings.value.copy(
            userIntent = intent,
            userPriority = priority,
            userStorageBudget = storage,
            userAnswerStyle = style
        )
    }
}
