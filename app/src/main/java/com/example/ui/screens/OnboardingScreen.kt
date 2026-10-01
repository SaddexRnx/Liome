package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.DeviceHardwareProfile
import com.example.model.ModelCatalog
import com.example.model.ModelItem
import com.example.model.ModelRecommendation
import com.example.ui.components.*
import com.example.ui.theme.LocalMindTheme
import com.example.ui.theme.LocalMindThemeStyle
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.DeviceScanState
import com.example.ui.viewmodel.OnboardingStep
import com.example.ui.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: (selectedModelId: String) -> Unit,
    currentThemeStyle: LocalMindThemeStyle = LocalMindThemeStyle.MINIMAL_EDITORIAL,
    onSelectThemeStyle: (LocalMindThemeStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val deviceProfile by viewModel.deviceProfile.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val recommendedModel by viewModel.recommendedModel.collectAsState()

    val selectedIntent by viewModel.selectedIntent.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val selectedStorage by viewModel.selectedStorage.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()

    var showThemeSheet by remember { mutableStateOf(false) }
    val tokens = LocalMindTheme.tokens

    LocalMindScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LocalMindTopBar(
                title = {
                    Text(
                        text = "LocalMind",
                        style = tokens.typography.titleMedium,
                        color = tokens.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    TextButton(
                        onClick = { showThemeSheet = true },
                        modifier = Modifier.testTag("onboarding_theme_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Theme Style",
                            tint = tokens.colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Theme Style",
                            style = tokens.typography.labelLarge,
                            color = tokens.colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "OnboardingTransition"
            ) { step ->
                when (step) {
                    is OnboardingStep.Welcome -> {
                        WelcomeStep(
                            onNext = { viewModel.setStep(OnboardingStep.DeviceAnalysis) }
                        )
                    }
                    is OnboardingStep.DeviceAnalysis -> {
                        DeviceAnalysisStep(
                            scanState = scanState,
                            profile = deviceProfile,
                            onProceed = { viewModel.setStep(OnboardingStep.SetupModeChoice) }
                        )
                    }
                    is OnboardingStep.SetupModeChoice -> {
                        SetupModeChoiceStep(
                            onSimpleMode = { viewModel.setStep(OnboardingStep.SimpleQuestionnaire) },
                            onAdvancedMode = { viewModel.setStep(OnboardingStep.AdvancedCatalog) }
                        )
                    }
                    is OnboardingStep.SimpleQuestionnaire -> {
                        SimpleQuestionnaireStep(
                            selectedIntent = selectedIntent,
                            selectedPriority = selectedPriority,
                            selectedStorage = selectedStorage,
                            selectedStyle = selectedStyle,
                            onIntentChange = viewModel::setIntent,
                            onPriorityChange = viewModel::setPriority,
                            onStorageChange = viewModel::setStorage,
                            onStyleChange = viewModel::setStyle,
                            onSubmit = { viewModel.computeRecommendation() }
                        )
                    }
                    is OnboardingStep.AdvancedCatalog -> {
                        AdvancedCatalogStep(
                            models = ModelCatalog.curatedModels,
                            onSelectModel = { model -> viewModel.selectModelDirectly(model) }
                        )
                    }
                    is OnboardingStep.RecommendationReady -> {
                        RecommendationReadyStep(
                            pair = recommendedModel,
                            currentThemeStyle = currentThemeStyle,
                            onOpenThemeSheet = { showThemeSheet = true },
                            onFinish = { modelId ->
                                viewModel.finishOnboarding(modelId)
                                onComplete(modelId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = tokens.colors.surface,
            shape = tokens.shapes.bottomSheetShape,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .size(36.dp, 4.dp)
                        .clip(CircleShape)
                        .background(tokens.colors.border)
                )
            }
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
                    text = "Select one of 5 complete design systems. Theme preference is independent of hardware or model choice.",
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
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 28.dp)) {
            Text(
                text = "LocalMind",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your AI.\nYour device.\nYour privacy.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 38.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LocalMind executes open-source language models directly on the silicon of your phone. Conversations remain on your storage and never leave your control.",
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            PillarRow(
                icon = Icons.Outlined.Shield,
                title = "Private by architecture",
                subtitle = "No remote telemetry, accounts, or cloud API calls."
            )
            Spacer(modifier = Modifier.height(14.dp))
            PillarRow(
                icon = Icons.Outlined.WifiOff,
                title = "Fully offline",
                subtitle = "Core inference functions with networking turned off."
            )
            Spacer(modifier = Modifier.height(14.dp))
            PillarRow(
                icon = Icons.Outlined.Memory,
                title = "On-device silicon",
                subtitle = "Optimized for your phone's processor memory and threads."
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("welcome_get_started_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Analyze Device Compatibility", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PillarRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DeviceAnalysisStep(
    scanState: DeviceScanState,
    profile: DeviceHardwareProfile?,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = "Compatibility Analysis",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inspecting processor topology, RAM capacity, and available storage.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Minimalist progress indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = scanState.stageText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { scanState.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (profile != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpecsRow("Architecture", profile.cpuArch)
                    SpecsRow("CPU Cores", "${profile.cpuCores} cores")
                    SpecsRow("Total RAM", "${String.format("%.1f GB", profile.totalRamGb)} (${String.format("%.1f GB", profile.availableRamGb)} available)")
                    SpecsRow("Free Storage", "${String.format("%.1f GB", profile.availableStorageGb)} free")
                    SpecsRow("Vector Acceleration", if (profile.hasVulkan) "Vulkan ${profile.vulkanVersion}" else "ARM NEON")
                }
            }
        }

        Button(
            onClick = onProceed,
            enabled = scanState.isComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("device_analysis_proceed_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Select Model", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SpecsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SetupModeChoiceStep(
    onSimpleMode: () -> Unit,
    onAdvancedMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = "Model Setup",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select how you would like to choose your on-device model.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Simple Mode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { onSimpleMode() }
                    .padding(18.dp)
                    .testTag("simple_mode_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Guided questionnaire", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "Recommended",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Answer four brief questions regarding intent and storage to receive a tailored model recommendation.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Advanced Mode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { onAdvancedMode() }
                    .padding(18.dp)
                    .testTag("advanced_mode_card")
            ) {
                Column {
                    Text("Manual catalog selection", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Directly inspect parameter scale (135M to 3.8B), quantization schemes (Q4_K_M), and context lengths.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = "Model selection can be adjusted anytime in the Models section.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SimpleQuestionnaireStep(
    selectedIntent: String,
    selectedPriority: String,
    selectedStorage: String,
    selectedStyle: String,
    onIntentChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onStorageChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Text("Preferences", fontSize = 24.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "These preferences guide the parameter scale and context sizing.",
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            QuestionSection(
                title = "Primary use case",
                options = listOf(
                    "General assistant", "Studying", "Coding",
                    "Writing", "Translation", "Brainstorming", "A little of everything"
                ),
                selected = selectedIntent,
                onSelect = onIntentChange
            )
        }

        item {
            QuestionSection(
                title = "Priority",
                options = listOf("Faster responses", "Better answers", "Balanced"),
                selected = selectedPriority,
                onSelect = onPriorityChange
            )
        }

        item {
            QuestionSection(
                title = "Dedicated storage budget",
                options = listOf("Less than 1 GB", "1–3 GB", "3–6 GB", "More than 6 GB"),
                selected = selectedStorage,
                onSelect = onStorageChange
            )
        }

        item {
            QuestionSection(
                title = "Response depth",
                options = listOf("Short and fast answers", "More detailed reasoning", "Balanced"),
                selected = selectedStyle,
                onSelect = onStyleChange
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("find_matching_model_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("View Recommendation", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun QuestionSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(title, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { opt ->
                        val isSelected = opt == selected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelect(opt) }
                                .padding(vertical = 10.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedCatalogStep(
    models: List<ModelItem>,
    onSelectModel: (ModelItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Curated Models", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Select an architecture to inspect specifications.",
            fontSize = 13.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(models) { model ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable { onSelectModel(model) }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(model.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(model.downloadSizeFormatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(model.description, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${model.parameterCount}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Q4_K_M", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${model.contextSize} ctx", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationReadyStep(
    pair: Pair<ModelItem, ModelRecommendation>?,
    currentThemeStyle: LocalMindThemeStyle,
    onOpenThemeSheet: () -> Unit,
    onFinish: (String) -> Unit
) {
    val model = pair?.first ?: ModelCatalog.curatedModels.first()
    val rec = pair?.second
    val tokens = LocalMindTheme.tokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "Recommendation",
                style = tokens.typography.headlineLarge,
                color = tokens.colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Suggested based on your selected preferences and available device information.",
                style = tokens.typography.bodyMedium,
                color = tokens.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            LocalMindCard(
                borderColor = tokens.colors.accent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    rec?.let {
                        Text(
                            text = it.badgeLabel,
                            style = tokens.typography.labelSmall,
                            color = tokens.colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = model.name,
                        style = tokens.typography.titleLarge,
                        color = tokens.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = model.tagline,
                        style = tokens.typography.bodyMedium,
                        color = tokens.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = rec?.explanation ?: model.description,
                        style = tokens.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = tokens.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Parameters", style = tokens.typography.labelSmall, color = tokens.colors.textSecondary)
                            Text(model.parameterCount, style = tokens.typography.bodyMedium, fontWeight = FontWeight.Medium, color = tokens.colors.textPrimary)
                        }
                        Column {
                            Text("Download Size", style = tokens.typography.labelSmall, color = tokens.colors.textSecondary)
                            Text(model.downloadSizeFormatted, style = tokens.typography.bodyMedium, fontWeight = FontWeight.Medium, color = tokens.colors.textPrimary)
                        }
                        Column {
                            Text("Est. RAM", style = tokens.typography.labelSmall, color = tokens.colors.textSecondary)
                            Text(model.requiredRamFormatted, style = tokens.typography.bodyMedium, fontWeight = FontWeight.Medium, color = tokens.colors.textPrimary)
                        }
                        Column {
                            Text("Context", style = tokens.typography.labelSmall, color = tokens.colors.textSecondary)
                            Text("${model.contextSize} tok", style = tokens.typography.bodyMedium, fontWeight = FontWeight.Medium, color = tokens.colors.textPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Theme Preference Card (Independent of hardware/model)
            LocalMindCard(
                onClick = onOpenThemeSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recommendation_theme_picker_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Visual Theme System",
                            style = tokens.typography.labelSmall,
                            color = tokens.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentThemeStyle.title,
                            style = tokens.typography.titleMedium,
                            color = tokens.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap to choose between the 5 design systems",
                            style = tokens.typography.bodyMedium,
                            color = tokens.colors.accent
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Change Theme",
                        tint = tokens.colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LocalMindButton(
            text = "Continue to Workspace",
            onClick = { onFinish(model.id) },
            testTag = "start_using_localmind_button",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
