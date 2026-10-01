package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.UserPreferencesRepository
import com.example.ui.screens.*
import com.example.ui.viewmodel.*

enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    CHAT("Chat", Icons.Outlined.ChatBubbleOutline, "tab_chat"),
    MODELS("Models", Icons.Outlined.Hub, "tab_models"),
    PERFORMANCE("Performance", Icons.Outlined.Speed, "tab_performance"),
    SETTINGS("Settings", Icons.Outlined.Tune, "tab_settings")
}

@Composable
fun AppNavigation(
    userPrefs: UserPreferencesRepository,
    modifier: Modifier = Modifier
) {
    val userSettings by userPrefs.settings.collectAsState()
    var selectedTab by remember { mutableStateOf(MainTab.CHAT) }
    var isInPrivacyScreen by remember { mutableStateOf(false) }

    val onboardingViewModel: OnboardingViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val modelsViewModel: ModelsViewModel = viewModel()
    val performanceViewModel: PerformanceViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    if (!userSettings.hasCompletedOnboarding) {
        OnboardingScreen(
            viewModel = onboardingViewModel,
            onComplete = { selectedModelId ->
                userPrefs.setOnboardingCompleted(true)
                userPrefs.setActiveModelId(selectedModelId)
                modelsViewModel.refreshModels()
            },
            currentThemeStyle = userSettings.themeStyle,
            onSelectThemeStyle = { userPrefs.setThemeStyle(it) },
            modifier = modifier
        )
    } else {
        if (isInPrivacyScreen) {
            BackHandler { isInPrivacyScreen = false }
            PrivacyScreen(
                onNavigateBack = { isInPrivacyScreen = false },
                modifier = modifier
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    val colors = com.example.ui.theme.LocalMindTheme.colors
                    NavigationBar(
                        containerColor = colors.surface,
                        tonalElevation = com.example.ui.theme.LocalMindTheme.spacing.elevation
                    ) {
                        MainTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.accent,
                                    selectedTextColor = colors.accent,
                                    indicatorColor = colors.accentContainer,
                                    unselectedIconColor = colors.textSecondary,
                                    unselectedTextColor = colors.textSecondary
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        MainTab.CHAT -> {
                            ChatScreen(
                                viewModel = chatViewModel,
                                onNavigateToModels = { selectedTab = MainTab.MODELS },
                                currentThemeStyle = userSettings.themeStyle,
                                onSelectThemeStyle = { userPrefs.setThemeStyle(it) }
                            )
                        }
                        MainTab.MODELS -> {
                            ModelsScreen(
                                viewModel = modelsViewModel,
                                currentThemeStyle = userSettings.themeStyle,
                                onSelectThemeStyle = { userPrefs.setThemeStyle(it) }
                            )
                        }
                        MainTab.PERFORMANCE -> {
                            PerformanceScreen(
                                viewModel = performanceViewModel,
                                currentThemeStyle = userSettings.themeStyle,
                                onSelectThemeStyle = { userPrefs.setThemeStyle(it) }
                            )
                        }
                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateToPrivacy = { isInPrivacyScreen = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
