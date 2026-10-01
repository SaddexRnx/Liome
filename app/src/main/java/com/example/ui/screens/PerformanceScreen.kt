package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Speed
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
import com.example.ui.theme.LocalMindTheme
import com.example.ui.theme.LocalMindThemeStyle
import com.example.ui.viewmodel.PerformanceViewModel

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    viewModel: PerformanceViewModel,
    currentThemeStyle: LocalMindThemeStyle = LocalMindThemeStyle.MINIMAL_EDITORIAL,
    onSelectThemeStyle: (LocalMindThemeStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.deviceProfile
    val stats = uiState.engineStats
    val tokens = LocalMindTheme.tokens
    var showThemeSheet by remember { mutableStateOf(false) }

    LocalMindScaffold(
        modifier = modifier,
        topBar = {
            LocalMindTopBar(
                title = {
                    Text(
                        text = if (tokens.style == LocalMindThemeStyle.MONOCHROME) "PERFORMANCE" else "Performance",
                        style = tokens.typography.headlineMedium,
                        color = tokens.colors.textPrimary,
                        fontWeight = if (tokens.style == LocalMindThemeStyle.MONOCHROME) FontWeight.Bold else FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showThemeSheet = true },
                        modifier = Modifier.testTag("theme_preset_button_performance")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Explore Theme Styles",
                            tint = tokens.colors.accent,
                            modifier = Modifier.size(20.dp)
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
            // Hardware Status Card
            item {
                LocalMindCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LocalMindSectionHeader(
                            title = "Device Hardware",
                            subtitle = "Local processor architecture and memory."
                        )

                        RowSpec("Processor", "${profile.cpuCores} cores (${profile.cpuArch})")
                        RowSpec("System RAM", "${String.format("%.1f GB", profile.totalRamGb)} total (${String.format("%.1f GB", profile.availableRamGb)} available)")
                        RowSpec("Storage", "${String.format("%.1f GB", profile.availableStorageGb)} free of ${String.format("%.1f GB", profile.totalStorageGb)}")
                        RowSpec("Vector Accelerators", profile.cpuFeatures.joinToString(", ").ifBlank { "ARM NEON" })
                        RowSpec("Acceleration", if (profile.hasVulkan) "Vulkan ${profile.vulkanVersion}" else "CPU NEON Vectorized")
                    }
                }
            }

            // Active Model Configuration Card
            item {
                LocalMindCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LocalMindSectionHeader(
                            title = "Active Model",
                            subtitle = "Current runtime execution parameters."
                        )

                        RowSpec("Selected Model", uiState.activeModelName)
                        RowSpec("Allocated Threads", "${stats.activeThreads} CPU threads")
                        RowSpec("Context Window", "${stats.contextSize} tokens")
                        RowSpec("Memory Mapped", if (stats.memoryMappedMb > 0) "${stats.memoryMappedMb} MB" else "Not loaded")
                        RowSpec(
                            "Inference Speed",
                            if (stats.lastTokensPerSec > 0) "${(stats.lastTokensPerSec * 10).toInt() / 10f} tok/s (Measured)"
                            else "Not benchmarked"
                        )
                    }
                }
            }

            // Silicon Stress Benchmark Card
            item {
                LocalMindCard {
                    Column {
                        LocalMindSectionHeader(
                            title = "Silicon Benchmark",
                            subtitle = "Measures matrix operations and RAM bandwidth on device silicon."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.isBenchmarking) {
                            Column {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(tokens.shapes.chipShape),
                                    color = tokens.colors.accent,
                                    trackColor = tokens.colors.borderSubtle
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.benchmarkResult?.stage ?: "Stressing CPU silicon...",
                                    style = tokens.typography.bodyMedium,
                                    color = tokens.colors.textSecondary
                                )
                            }
                        } else if (uiState.benchmarkResult != null) {
                            val res = uiState.benchmarkResult!!
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RowSpec("CPU Compute (Measured)", "${res.cpuGflops} GFLOPS")
                                RowSpec("RAM Bandwidth (Measured)", "${res.memoryBandwidthMbPerSec.toInt()} MB/s")
                                RowSpec("Throughput (Estimated)", "~${(res.estimatedTokensPerSec * 10).toInt() / 10f} tok/s")
                            }
                        } else {
                            Text(
                                text = "Not benchmarked on this device yet.",
                                style = tokens.typography.bodyMedium,
                                color = tokens.colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LocalMindButton(
                            text = if (uiState.isBenchmarking) "Testing..." else "Run Hardware Benchmark",
                            onClick = { viewModel.runBenchmark() },
                            enabled = !uiState.isBenchmarking,
                            variant = LocalMindButtonVariant.SECONDARY,
                            icon = Icons.Outlined.Speed,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "run_benchmark_button"
                        )
                    }
                }
            }
        }
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
private fun RowSpec(label: String, value: String) {
    val tokens = LocalMindTheme.tokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = tokens.typography.bodyMedium, color = tokens.colors.textSecondary)
        Text(text = value, style = tokens.typography.bodyMedium, color = tokens.colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}
