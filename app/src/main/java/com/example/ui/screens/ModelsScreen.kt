package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.LocalMindTheme
import com.example.ui.theme.LocalMindThemeStyle
import com.example.ui.viewmodel.ModelsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    viewModel: ModelsViewModel,
    currentThemeStyle: LocalMindThemeStyle = LocalMindThemeStyle.MINIMAL_EDITORIAL,
    onSelectThemeStyle: (LocalMindThemeStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val tokens = LocalMindTheme.tokens
    var showThemeSheet by remember { mutableStateOf(false) }

    LocalMindScaffold(
        modifier = modifier,
        topBar = {
            LocalMindTopBar(
                title = {
                    Text(
                        text = if (tokens.style == LocalMindThemeStyle.MONOCHROME) "MODELS" else "Models",
                        style = tokens.typography.headlineMedium,
                        color = tokens.colors.textPrimary,
                        fontWeight = if (tokens.style == LocalMindThemeStyle.MONOCHROME) FontWeight.Bold else FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showThemeSheet = true },
                        modifier = Modifier.testTag("theme_preset_button_models")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Explore Theme Styles",
                            tint = tokens.colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.refreshModels() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = tokens.colors.textSecondary
                        )
                    }
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
            item {
                StorageSummary(
                    usedBytes = uiState.totalInstalledSizeBytes,
                    availableBytes = uiState.availableStorageBytes
                )
            }

            item {
                LocalMindSectionHeader(
                    title = "Model Library",
                    subtitle = "Open-source GGUF models running locally on device silicon."
                )
            }

            items(uiState.models, key = { it.id }) { model ->
                val isDownloadingThis = when (val ds = uiState.downloadState) {
                    is DownloadState.Downloading -> ds.modelId == model.id
                    is DownloadState.CheckingStorage -> ds.modelId == model.id
                    is DownloadState.Verifying -> ds.modelId == model.id
                    else -> false
                }

                ModelCardItem(
                    model = model,
                    isActive = model.isActive,
                    isDownloading = isDownloadingThis,
                    downloadState = uiState.downloadState,
                    onDownload = { viewModel.startDownload(model) },
                    onCancelDownload = { viewModel.cancelDownload() },
                    onActivate = { viewModel.activateModel(model.id) },
                    onDelete = { viewModel.deleteModel(model) },
                    onVerify = { viewModel.inspectAndVerifyModel(model) }
                )
            }
        }
    }

    if (uiState.showVerifyDialog && uiState.verifyTargetModel != null) {
        val metadata = uiState.verifiedMetadata
        val target = uiState.verifyTargetModel!!

        AlertDialog(
            onDismissRequest = { viewModel.dismissVerifyDialog() },
            containerColor = tokens.colors.surface,
            shape = tokens.shapes.cardShape,
            title = {
                Text(
                    text = "GGUF Binary Structure",
                    style = tokens.typography.titleLarge,
                    color = tokens.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Model: ${target.name}",
                        style = tokens.typography.titleMedium,
                        color = tokens.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (metadata != null) {
                        Text("Architecture: ${metadata.architecture}", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                        Text("Quantization: ${metadata.quantizationType}", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                        Text("Context Length: ${metadata.contextLength} tokens", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                        Text("Layers: ${metadata.blockCount}", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                        Text("Tensors: ${metadata.tensorCount}", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
                        Text("File Size: ${String.format("%.1f MB", metadata.fileSizeBytes / (1024f * 1024f))}", style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)

                        Spacer(modifier = Modifier.height(6.dp))
                        if (metadata.isValidGguf) {
                            Text(
                                text = "Header valid. Direct memory mapping ready.",
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.statusGreen,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Validation error: ${metadata.validationError}",
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.statusRed
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissVerifyDialog() }) {
                    Text("Close", color = tokens.colors.accent)
                }
            }
        )
    }

    if (showThemeSheet) {
        LocalMindBottomSheet(
            onDismissRequest = { showThemeSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Visual Theme System",
                    style = tokens.typography.headlineMedium,
                    color = tokens.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Switch between 5 complete design systems for LocalMind.",
                    style = tokens.typography.bodyMedium,
                    color = tokens.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LocalMindThemeStyle.values()) { style ->
                        ThemeStyleCard(
                            style = style,
                            isSelected = currentThemeStyle == style,
                            isDark = tokens.isDark,
                            onClick = {
                                onSelectThemeStyle(style)
                                showThemeSheet = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun StorageSummary(usedBytes: Long, availableBytes: Long) {
    val tokens = LocalMindTheme.tokens
    val usedMb = usedBytes / (1024f * 1024f)
    val availGb = availableBytes / (1024f * 1024f * 1024f)

    LocalMindCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Installed Storage",
                    style = tokens.typography.labelSmall,
                    color = tokens.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (usedMb >= 1024) String.format("%.2f GB", usedMb / 1024f) else String.format("%.0f MB", usedMb),
                    style = tokens.typography.headlineLarge,
                    color = tokens.colors.textPrimary
                )
            }

            Text(
                text = "${String.format("%.1f GB", availGb)} free space",
                style = tokens.typography.bodyMedium,
                color = tokens.colors.textSecondary
            )
        }
    }
}

@Composable
private fun ModelCardItem(
    model: ModelItem,
    isActive: Boolean,
    isDownloading: Boolean,
    downloadState: DownloadState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onVerify: () -> Unit
) {
    val tokens = LocalMindTheme.tokens

    LocalMindCard(
        borderColor = if (isActive) tokens.colors.accent else tokens.colors.border,
        modifier = Modifier.testTag("model_card_${model.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.name,
                            style = tokens.typography.titleLarge,
                            color = tokens.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(tokens.shapes.tagShape)
                                    .background(tokens.colors.accentContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.colors.onAccentContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = model.tagline,
                        style = tokens.typography.labelSmall,
                        color = tokens.colors.accent
                    )
                }

                model.recommendation?.let { rec ->
                    Text(
                        text = rec.badgeLabel,
                        style = tokens.typography.labelSmall,
                        color = tokens.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.description,
                style = tokens.typography.bodyMedium,
                color = tokens.colors.textSecondary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SpecItem("Parameters", model.parameterCount)
                SpecItem("Format", "Q4_K_M")
                SpecItem("Size", model.downloadSizeFormatted)
                SpecItem("RAM", model.requiredRamFormatted)
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isDownloading) {
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(downloadState.progressFraction * 100).toInt()}% • ${(downloadState.speedMbPerSec * 10).toInt() / 10f} MB/s",
                                    style = tokens.typography.labelSmall,
                                    color = tokens.colors.accent
                                )
                                Text(
                                    text = "ETA ${downloadState.etaSeconds}s",
                                    style = tokens.typography.labelSmall,
                                    color = tokens.colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { downloadState.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(tokens.shapes.chipShape),
                                color = tokens.colors.accent,
                                trackColor = tokens.colors.borderSubtle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Cancel", color = tokens.colors.textSecondary)
                            }
                        }
                    }
                    is DownloadState.Verifying -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.5.dp,
                                color = tokens.colors.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = downloadState.stepDescription,
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.textSecondary
                            )
                        }
                    }
                    else -> {}
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (model.isInstalled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!isActive) {
                                LocalMindButton(
                                    text = "Activate",
                                    onClick = onActivate,
                                    variant = LocalMindButtonVariant.PRIMARY,
                                    testTag = "activate_model_${model.id}"
                                )
                            }
                            LocalMindButton(
                                text = "Inspect",
                                onClick = onVerify,
                                variant = LocalMindButtonVariant.OUTLINED
                            )
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = tokens.colors.statusRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        LocalMindButton(
                            text = "Download (${model.downloadSizeFormatted})",
                            onClick = onDownload,
                            variant = LocalMindButtonVariant.PRIMARY,
                            icon = Icons.Outlined.Download,
                            testTag = "download_model_${model.id}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    val tokens = LocalMindTheme.tokens
    Column {
        Text(text = label, style = tokens.typography.labelSmall, color = tokens.colors.textTertiary)
        Text(text = value, style = tokens.typography.bodyMedium, color = tokens.colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}
