package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// LocalMind 4 Design Styles (Derived from User Uploaded References)
// =========================================================================

enum class ThemePreset(
    val title: String,
    val description: String,
    val inspiration: String
) {
    OBSIDIAN_MINT(
        title = "Obsidian Mint",
        description = "Deep dark emerald with luminous seafoam mint accents & voice wave aesthetic",
        inspiration = "Travely Dark AI"
    ),
    PORCELAIN_AIR(
        title = "Porcelain Air",
        description = "Crisp porcelain white floating cards on atmospheric soft sky blue with slate pills",
        inspiration = "Mobilanc / Luxury Clean"
    ),
    SUNLIT_BOLD(
        title = "Sunlit Bold",
        description = "Vibrant warm sunshine gold canvas with bold contrast and jet-black pill buttons",
        inspiration = "Flame Gnome & Sunshine"
    ),
    MINIMAL_CHARCOAL(
        title = "Minimal Charcoal",
        description = "Near-black charcoal & warm linen with restrained warm bronze ochre",
        inspiration = "Editorial Minimalist"
    )
}

// -------------------------------------------------------------------------
// Style 1: Obsidian Mint (Inspired by Image 3 - Travely)
// -------------------------------------------------------------------------
val MintBgDark = Color(0xFF08100C)
val MintSurfaceDark = Color(0xFF101C16)
val MintSurfaceVariantDark = Color(0xFF182820)
val MintBorderDark = Color(0xFF1E3529)
val MintPrimaryDark = Color(0xFF2DD4BF) // Seafoam Mint
val MintPrimaryContainerDark = Color(0xFF13382F)
val MintTextPrimaryDark = Color(0xFFF0FDF4)
val MintTextSecondaryDark = Color(0xFF86A394)

val MintBgLight = Color(0xFFF3FAF6)
val MintSurfaceLight = Color(0xFFFFFFFF)
val MintSurfaceVariantLight = Color(0xFFE5F3EB)
val MintBorderLight = Color(0xFFCEE5D8)
val MintPrimaryLight = Color(0xFF0F766E)
val MintPrimaryContainerLight = Color(0xFFCCFBF1)
val MintTextPrimaryLight = Color(0xFF0A2219)
val MintTextSecondaryLight = Color(0xFF527566)

// -------------------------------------------------------------------------
// Style 2: Porcelain Air (Inspired by Images 4, 5, 6 - Mobilanc)
// -------------------------------------------------------------------------
val PorcelainBgLight = Color(0xFFEBF1F7) // Atmospheric soft sky
val PorcelainSurfaceLight = Color(0xFFFFFFFF) // Crisp pure white
val PorcelainSurfaceVariantLight = Color(0xFFDEE8F1)
val PorcelainBorderLight = Color(0xFFCFDEEC)
val PorcelainPrimaryLight = Color(0xFF4A7A9F) // Soft slate blue pill
val PorcelainPrimaryContainerLight = Color(0xFFD4E4F3)
val PorcelainTextPrimaryLight = Color(0xFF16222E)
val PorcelainTextSecondaryLight = Color(0xFF5E7385)

val PorcelainBgDark = Color(0xFF0F151B)
val PorcelainSurfaceDark = Color(0xFF17202A)
val PorcelainSurfaceVariantDark = Color(0xFF202C39)
val PorcelainBorderDark = Color(0xFF29394A)
val PorcelainPrimaryDark = Color(0xFF7FA7CA)
val PorcelainPrimaryContainerDark = Color(0xFF1C3449)
val PorcelainTextPrimaryDark = Color(0xFFEFF5FA)
val PorcelainTextSecondaryDark = Color(0xFF90A3B5)

// -------------------------------------------------------------------------
// Style 3: Sunlit Bold (Inspired by Images 1 & 2 - Flame / Sunshine)
// -------------------------------------------------------------------------
val SunlitBgLight = Color(0xFFF7F4EE)
val SunlitSurfaceLight = Color(0xFFFFFFFF)
val SunlitSurfaceVariantLight = Color(0xFFF5EFE0)
val SunlitBorderLight = Color(0xFFE8DFC8)
val SunlitPrimaryLight = Color(0xFFD97706) // Rich Sunshine Gold / Amber
val SunlitPrimaryContainerLight = Color(0xFFFEF3C7)
val SunlitTextPrimaryLight = Color(0xFF1C1917)
val SunlitTextSecondaryLight = Color(0xFF78716C)

val SunlitBgDark = Color(0xFF14120D)
val SunlitSurfaceDark = Color(0xFF1E1A13)
val SunlitSurfaceVariantDark = Color(0xFF2B251B)
val SunlitBorderDark = Color(0xFF3B3324)
val SunlitPrimaryDark = Color(0xFFFBBF24) // Luminous Golden Amber
val SunlitPrimaryContainerDark = Color(0xFF382A10)
val SunlitTextPrimaryDark = Color(0xFFFFFBEB)
val SunlitTextSecondaryDark = Color(0xFFA89F91)

// -------------------------------------------------------------------------
// Style 4: Minimal Charcoal (Editorial)
// -------------------------------------------------------------------------
val CharcoalBgDark = Color(0xFF0F0F11)
val CharcoalSurfaceDark = Color(0xFF18181B)
val CharcoalSurfaceVariantDark = Color(0xFF222226)
val CharcoalBorderDark = Color(0xFF27272A)
val CharcoalPrimaryDark = Color(0xFFD49A6A) // Muted Ochre
val CharcoalPrimaryContainerDark = Color(0xFF2A1F18)
val CharcoalTextPrimaryDark = Color(0xFFF4F4F5)
val CharcoalTextSecondaryDark = Color(0xFFA1A1AA)

val CharcoalBgLight = Color(0xFFFAF9F6)
val CharcoalSurfaceLight = Color(0xFFFFFFFF)
val CharcoalSurfaceVariantLight = Color(0xFFF3F1EC)
val CharcoalBorderLight = Color(0xFFE4E2DC)
val CharcoalPrimaryLight = Color(0xFFC88A58)
val CharcoalPrimaryContainerLight = Color(0xFFF7EFE8)
val CharcoalTextPrimaryLight = Color(0xFF18181B)
val CharcoalTextSecondaryLight = Color(0xFF71717A)

// Status & Semantic Colors
val StatusGreen = Color(0xFF34D399)
val StatusAmber = Color(0xFFF59E0B)
val StatusRed = Color(0xFFEF4444)

// Code highlighting colors
val CodeBgDark = Color(0xFF131316)
val CodeBgLight = Color(0xFFF2F0EB)
val CodeTextDark = Color(0xFFE4E4E7)
val CodeTextLight = Color(0xFF27272A)
val CodeKeywordColor = Color(0xFFD49A6A)
val CodeStringColor = Color(0xFF86A873)
val CodeCommentColor = Color(0xFF71717A)
