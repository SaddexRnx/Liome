package com.example.ui.theme

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class LocalMindThemeStyle(
    val title: String,
    val subtitle: String,
    val description: String,
    val fontLabel: String,
    val cornerLabel: String
) {
    MINIMAL_EDITORIAL(
        title = "THEME 1 — MINIMAL EDITORIAL",
        subtitle = "Content-First & Reading",
        description = "Large confident typography, generous whitespace, warm paper/charcoal palette, subtle 1px borders, restrained 6dp radius.",
        fontLabel = "Editorial Serif/Clean",
        cornerLabel = "6dp Structured"
    ),
    PREMIUM_SOFT(
        title = "THEME 2 — PREMIUM SOFT",
        subtitle = "Polished Consumer",
        description = "Soft layered surfaces, elegant 18dp rounded containers, tactile controls, gentle tonal depth, comfortable spacing.",
        fontLabel = "Refined Geometric",
        cornerLabel = "18dp Rounded"
    ),
    MONOCHROME(
        title = "THEME 3 — MONOCHROME",
        subtitle = "Brutal Minimal",
        description = "High contrast black, white & grayscale. Sharp 2dp geometry, bold hierarchy, strong alignment, zero decorative color.",
        fontLabel = "High-Contrast Grotesque",
        cornerLabel = "2dp Sharp Block"
    ),
    MODERN_ORGANIC(
        title = "THEME 4 — MODERN ORGANIC",
        subtitle = "Warm Natural & Human",
        description = "Warm oatmeal/earthy backgrounds, gentle sage/terracotta accents, fluid 26dp organic curves, comfortable breathing room.",
        fontLabel = "Warm Humanist",
        cornerLabel = "26dp Organic Pill"
    ),
    FUTURE_MINIMAL(
        title = "THEME 5 — FUTURE MINIMAL",
        subtitle = "Precision Silicon",
        description = "Deep carbon/titanium surfaces, crisp platinum hairlines, 10dp technical precision, telemetry hierarchy without neon clichés.",
        fontLabel = "Technical Monospace/Sans",
        cornerLabel = "10dp Precision"
    )
}
