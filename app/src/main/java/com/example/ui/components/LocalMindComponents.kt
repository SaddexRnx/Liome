package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ModelItem
import com.example.ui.theme.LocalMindTheme
import com.example.ui.theme.LocalMindThemeStyle

@Composable
fun LocalMindScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val tokens = LocalMindTheme.tokens
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = tokens.colors.background,
        contentColor = tokens.colors.textPrimary,
        topBar = topBar,
        bottomBar = bottomBar,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMindTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val tokens = LocalMindTheme.tokens
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = tokens.colors.background,
            titleContentColor = tokens.colors.textPrimary,
            navigationIconContentColor = tokens.colors.textPrimary,
            actionIconContentColor = tokens.colors.textPrimary
        )
    )
}

@Composable
fun LocalMindCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = LocalMindTheme.colors.surface,
    borderColor: Color = LocalMindTheme.colors.border,
    elevation: Dp = LocalMindTheme.spacing.elevation,
    contentPadding: Dp = LocalMindTheme.spacing.cardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalMindTheme.tokens
    val shape = tokens.shapes.cardShape
    val borderWidth = tokens.spacing.borderWidth

    val baseModifier = modifier
        .fillMaxWidth()
        .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
        .clip(shape)
        .background(backgroundColor)
        .then(if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, shape) else Modifier)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(contentPadding)

    Column(modifier = baseModifier, content = content)
}

enum class LocalMindButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    GHOST
}

@Composable
fun LocalMindButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LocalMindButtonVariant = LocalMindButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val tokens = LocalMindTheme.tokens
    val shape = tokens.shapes.buttonShape

    val (bg, contentColor, borderStroke) = when (variant) {
        LocalMindButtonVariant.PRIMARY -> Triple(
            if (enabled) tokens.colors.accent else tokens.colors.surfaceVariant,
            if (enabled) tokens.colors.onAccent else tokens.colors.textTertiary,
            null
        )
        LocalMindButtonVariant.SECONDARY -> Triple(
            tokens.colors.surfaceVariant,
            tokens.colors.textPrimary,
            null
        )
        LocalMindButtonVariant.OUTLINED -> Triple(
            Color.Transparent,
            tokens.colors.textPrimary,
            tokens.spacing.borderWidth to tokens.colors.border
        )
        LocalMindButtonVariant.GHOST -> Triple(
            Color.Transparent,
            tokens.colors.textPrimary,
            null
        )
    }

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .height(48.dp)
            .clip(shape)
            .background(bg)
            .then(
                if (borderStroke != null) Modifier.border(borderStroke.first, borderStroke.second, shape)
                else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = tokens.typography.labelLarge,
                color = contentColor,
                fontWeight = if (tokens.style == LocalMindThemeStyle.MONOCHROME) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun LocalMindChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val tokens = LocalMindTheme.tokens
    val shape = tokens.shapes.chipShape

    val bg = if (isSelected) tokens.colors.accentContainer else tokens.colors.surfaceVariant
    val textColor = if (isSelected) tokens.colors.onAccentContainer else tokens.colors.textPrimary
    val borderColor = if (isSelected) tokens.colors.accent else tokens.colors.border

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(tokens.spacing.borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
fun LocalMindSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val tokens = LocalMindTheme.tokens
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (tokens.style == LocalMindThemeStyle.MONOCHROME) title.uppercase() else title,
                style = tokens.typography.titleLarge,
                color = tokens.colors.textPrimary,
                fontWeight = if (tokens.style == LocalMindThemeStyle.MONOCHROME) FontWeight.Bold else FontWeight.SemiBold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = tokens.typography.bodyMedium,
                    color = tokens.colors.textSecondary
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun LocalMindEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null
) {
    val tokens = LocalMindTheme.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = if (tokens.style == LocalMindThemeStyle.MINIMAL_EDITORIAL) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(tokens.shapes.chipShape)
                    .background(tokens.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tokens.colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = title,
            style = tokens.typography.headlineMedium,
            color = tokens.colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = tokens.typography.bodyMedium,
            color = tokens.colors.textSecondary,
            lineHeight = 20.sp
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
fun LocalMindSettingRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    val tokens = LocalMindTheme.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = tokens.typography.bodyLarge,
                color = tokens.colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = tokens.typography.bodyMedium,
                    color = tokens.colors.textSecondary
                )
            }
        }
        trailing()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMindBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalMindTheme.tokens
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
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
        },
        modifier = modifier,
        content = content
    )
}

@Composable
fun LocalMindMessage(
    isUser: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalMindTheme.tokens
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(tokens.shapes.userMessageShape)
                    .background(tokens.colors.userBubbleBg)
                    .then(
                        if (tokens.style == LocalMindThemeStyle.MONOCHROME) {
                            Modifier.border(tokens.spacing.borderWidth, tokens.colors.border, tokens.shapes.userMessageShape)
                        } else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                content()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (tokens.style != LocalMindThemeStyle.MINIMAL_EDITORIAL) {
                            Modifier
                                .clip(tokens.shapes.assistantMessageShape)
                                .background(tokens.colors.assistantBubbleBg)
                                .then(
                                    if (tokens.spacing.borderWidth > 0.dp && tokens.style == LocalMindThemeStyle.MONOCHROME)
                                        Modifier.border(tokens.spacing.borderWidth, tokens.colors.border, tokens.shapes.assistantMessageShape)
                                    else Modifier
                                )
                                .padding(tokens.spacing.cardPadding)
                        } else {
                            Modifier.padding(vertical = 4.dp)
                        }
                    )
            ) {
                content()
            }
        }
    }
}

@Composable
fun LocalMindModelCard(
    model: ModelItem,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    details: (@Composable () -> Unit)? = null
) {
    val tokens = LocalMindTheme.tokens

    LocalMindCard(
        borderColor = if (isActive) tokens.colors.accent else tokens.colors.border,
        modifier = modifier.testTag("model_card_${model.id}")
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
                if (badge != null) {
                    badge()
                }
            }

            if (details != null) {
                Spacer(modifier = Modifier.height(10.dp))
                details()
            }

            if (actions != null) {
                Spacer(modifier = Modifier.height(12.dp))
                actions()
            }
        }
    }
}

