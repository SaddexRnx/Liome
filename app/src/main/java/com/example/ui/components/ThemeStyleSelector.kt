package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ThemeStyleSelector(
    selectedStyle: LocalMindThemeStyle,
    onSelectStyle: (LocalMindThemeStyle) -> Unit,
    selectedMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tokens = LocalMindTheme.tokens

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Theme Style Cards
        LocalMindSectionHeader(
            title = "Visual Design System",
            subtitle = "Explore 5 distinct architectural design systems."
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LocalMindThemeStyle.values().forEach { style ->
                ThemeStyleCard(
                    style = style,
                    isSelected = selectedStyle == style,
                    isDark = isDark,
                    onClick = { onSelectStyle(style) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Independent Appearance Mode
        LocalMindSectionHeader(
            title = "Appearance",
            subtitle = "Independent Light, Dark, or System mode."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppThemeMode.values().forEach { mode ->
                val isSelected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(tokens.shapes.buttonShape)
                        .background(
                            if (isSelected) tokens.colors.accentContainer
                            else tokens.colors.surfaceVariant
                        )
                        .border(
                            width = if (isSelected) tokens.spacing.borderWidth * 1.5f else tokens.spacing.borderWidth,
                            color = if (isSelected) tokens.colors.accent else tokens.colors.border,
                            shape = tokens.shapes.buttonShape
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = tokens.typography.labelLarge,
                        color = if (isSelected) tokens.colors.onAccentContainer else tokens.colors.textPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeStyleCard(
    style: LocalMindThemeStyle,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    // Generate the preview tokens for this specific style
    val previewTokens = getLocalMindTokens(style = style, isDark = isDark)
    val currentTokens = LocalMindTheme.tokens

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(previewTokens.shapes.cardShape)
            .background(previewTokens.colors.surface)
            .border(
                width = if (isSelected) previewTokens.spacing.borderWidth * 1.8f else previewTokens.spacing.borderWidth,
                color = if (isSelected) previewTokens.colors.accent else previewTokens.colors.border,
                shape = previewTokens.shapes.cardShape
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("theme_style_${style.name.lowercase()}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Accent indicator dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(previewTokens.colors.accent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = style.title,
                        style = previewTokens.typography.titleLarge,
                        color = previewTokens.colors.textPrimary,
                        fontWeight = if (style == LocalMindThemeStyle.MONOCHROME) FontWeight.Bold else FontWeight.SemiBold
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(previewTokens.colors.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Active",
                            tint = previewTokens.colors.onAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = style.subtitle,
                style = previewTokens.typography.labelSmall,
                color = previewTokens.colors.accent
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = style.description,
                style = previewTokens.typography.bodyMedium,
                color = previewTokens.colors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mini visual token preview strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Background & Surface swatch
                Box(
                    modifier = Modifier
                        .size(height = 20.dp, width = 48.dp)
                        .clip(previewTokens.shapes.chipShape)
                        .background(previewTokens.colors.background)
                        .border(previewTokens.spacing.borderWidth, previewTokens.colors.border, previewTokens.shapes.chipShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "BG",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = previewTokens.colors.textPrimary
                    )
                }

                // Card surface swatch
                Box(
                    modifier = Modifier
                        .size(height = 20.dp, width = 52.dp)
                        .clip(previewTokens.shapes.chipShape)
                        .background(previewTokens.colors.surfaceVariant)
                        .border(previewTokens.spacing.borderWidth, previewTokens.colors.border, previewTokens.shapes.chipShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SURFACE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = previewTokens.colors.textSecondary
                    )
                }

                // Accent button swatch
                Box(
                    modifier = Modifier
                        .size(height = 20.dp, width = 56.dp)
                        .clip(previewTokens.shapes.buttonShape)
                        .background(previewTokens.colors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ACCENT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = previewTokens.colors.onAccent
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = style.cornerLabel,
                    fontSize = 11.sp,
                    color = previewTokens.colors.textTertiary
                )
            }
        }
    }
}
