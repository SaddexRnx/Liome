package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalMindTheme
import com.example.ui.theme.LocalMindThemeStyle
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val tokens = LocalMindTheme.tokens

    var showClearConversationsDialog by remember { mutableStateOf(false) }
    var showDeleteModelsDialog by remember { mutableStateOf(false) }

    LocalMindScaffold(
        modifier = modifier,
        topBar = {
            LocalMindTopBar(
                title = {
                    Text(
                        text = if (tokens.style == LocalMindThemeStyle.MONOCHROME) "SETTINGS" else "Settings",
                        style = tokens.typography.headlineMedium,
                        color = tokens.colors.textPrimary,
                        fontWeight = if (tokens.style == LocalMindThemeStyle.MONOCHROME) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = tokens.spacing.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.itemSpacing),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Theme Style Explorer & Mode Selection
            item {
                ThemeStyleSelector(
                    selectedStyle = settings.themeStyle,
                    onSelectStyle = { viewModel.setThemeStyle(it) },
                    selectedMode = settings.themeMode,
                    onSelectMode = { viewModel.setThemeMode(it) },
                    isDark = tokens.isDark
                )
            }

            // Privacy Center Navigation Card
            item {
                LocalMindCard(
                    onClick = onNavigateToPrivacy,
                    modifier = Modifier.testTag("privacy_center_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Privacy & Verification",
                                style = tokens.typography.titleMedium,
                                color = tokens.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Audit local SQLite storage and zero-cloud architecture.",
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = tokens.colors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Inference Parameters Card
            item {
                LocalMindCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LocalMindSectionHeader(
                            title = "Inference Configuration",
                            subtitle = "Tuned for on-device memory and execution threads."
                        )

                        // Thread Count
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CPU Threads", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                                Text("${settings.cpuThreads} threads", style = tokens.typography.bodyMedium, color = tokens.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Slider(
                                value = settings.cpuThreads.toFloat(),
                                onValueChange = { viewModel.updateCpuThreads(it.toInt()) },
                                valueRange = 1f..8f,
                                steps = 6,
                                modifier = Modifier.testTag("cpu_threads_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = tokens.colors.accent,
                                    activeTrackColor = tokens.colors.accent,
                                    inactiveTrackColor = tokens.colors.borderSubtle
                                )
                            )
                        }

                        // Context Size
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Context Size", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                                Text("${settings.contextSize} tokens", style = tokens.typography.bodyMedium, color = tokens.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1024, 2048, 4096).forEach { size ->
                                    val isSelected = settings.contextSize == size
                                    LocalMindChip(
                                        label = "$size",
                                        isSelected = isSelected,
                                        onClick = { viewModel.updateContextSize(size) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Temperature
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Temperature", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                                Text("${(settings.temperature * 10).toInt() / 10f}", style = tokens.typography.bodyMedium, color = tokens.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Slider(
                                value = settings.temperature,
                                onValueChange = { viewModel.updateTemperature(it) },
                                valueRange = 0.1f..1.5f,
                                modifier = Modifier.testTag("temperature_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = tokens.colors.accent,
                                    activeTrackColor = tokens.colors.accent,
                                    inactiveTrackColor = tokens.colors.borderSubtle
                                )
                            )
                        }
                    }
                }
            }

            // Local Data Management
            item {
                LocalMindCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LocalMindSectionHeader(
                            title = "Local Data Storage",
                            subtitle = "On-device database and model weight files."
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Model Weights", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                            Text(
                                text = if (uiState.totalModelsSizeBytes >= 1024 * 1024 * 1024)
                                    String.format("%.2f GB", uiState.totalModelsSizeBytes / (1024f * 1024f * 1024f))
                                else
                                    String.format("%.0f MB", uiState.totalModelsSizeBytes / (1024f * 1024f)),
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Saved Chats", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                            Text(
                                text = "${uiState.conversationCount} chats (${uiState.totalMessageCount} messages)",
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LocalMindButton(
                            text = "Clear All Conversations",
                            onClick = { showClearConversationsDialog = true },
                            variant = LocalMindButtonVariant.OUTLINED,
                            modifier = Modifier.fillMaxWidth()
                        )

                        LocalMindButton(
                            text = "Delete All Downloaded Models",
                            onClick = { showDeleteModelsDialog = true },
                            variant = LocalMindButtonVariant.OUTLINED,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showClearConversationsDialog) {
        AlertDialog(
            onDismissRequest = { showClearConversationsDialog = false },
            containerColor = tokens.colors.surface,
            shape = tokens.shapes.cardShape,
            title = { Text("Clear all conversations?", style = tokens.typography.titleLarge, color = tokens.colors.textPrimary) },
            text = { Text("This will permanently remove all stored conversations from the local database.", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearConversationsDialog = false
                    }
                ) {
                    Text("Clear", color = tokens.colors.statusRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConversationsDialog = false }) {
                    Text("Cancel", color = tokens.colors.textSecondary)
                }
            }
        )
    }

    if (showDeleteModelsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteModelsDialog = false },
            containerColor = tokens.colors.surface,
            shape = tokens.shapes.cardShape,
            title = { Text("Delete all downloaded models?", style = tokens.typography.titleLarge, color = tokens.colors.textPrimary) },
            text = { Text("This will delete all downloaded GGUF files to reclaim device storage.", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllModels()
                        showDeleteModelsDialog = false
                    }
                ) {
                    Text("Delete", color = tokens.colors.statusRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteModelsDialog = false }) {
                    Text("Cancel", color = tokens.colors.textSecondary)
                }
            }
        )
    }
}
