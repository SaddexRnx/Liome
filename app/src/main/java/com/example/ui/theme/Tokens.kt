package com.example.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class LocalMindColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHover: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val border: Color,
    val borderSubtle: Color,
    val userBubbleBg: Color,
    val userBubbleText: Color,
    val assistantBubbleBg: Color,
    val assistantBubbleText: Color,
    val codeBg: Color,
    val codeText: Color,
    val statusGreen: Color,
    val statusAmber: Color,
    val statusRed: Color
)

@Immutable
data class LocalMindShapes(
    val cardShape: CornerBasedShape,
    val buttonShape: CornerBasedShape,
    val chipShape: CornerBasedShape,
    val userMessageShape: CornerBasedShape,
    val assistantMessageShape: CornerBasedShape,
    val inputShape: CornerBasedShape,
    val bottomSheetShape: CornerBasedShape,
    val tagShape: CornerBasedShape
)

@Immutable
data class LocalMindTypography(
    val displayLarge: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val labelLarge: TextStyle,
    val labelSmall: TextStyle,
    val code: TextStyle
)

@Immutable
data class LocalMindSpacing(
    val screenHorizontalPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val borderWidth: Dp,
    val elevation: Dp
)

@Immutable
data class LocalMindTokens(
    val style: LocalMindThemeStyle,
    val isDark: Boolean,
    val colors: LocalMindColors,
    val shapes: LocalMindShapes,
    val typography: LocalMindTypography,
    val spacing: LocalMindSpacing
)

val LocalLocalMindTokens = staticCompositionLocalOf<LocalMindTokens> {
    error("No LocalMindTokens provided")
}

@Composable
fun getLocalMindTokens(
    style: LocalMindThemeStyle,
    isDark: Boolean
): LocalMindTokens {
    val colors = when (style) {
        LocalMindThemeStyle.MINIMAL_EDITORIAL -> {
            if (isDark) {
                LocalMindColors(
                    background = Color(0xFF111113),
                    surface = Color(0xFF19191C),
                    surfaceVariant = Color(0xFF222226),
                    surfaceHover = Color(0xFF2B2B30),
                    textPrimary = Color(0xFFEDEDEB),
                    textSecondary = Color(0xFF96948F),
                    textTertiary = Color(0xFF6E6C68),
                    accent = Color(0xFFC47A46), // Bronze ochre
                    onAccent = Color(0xFF1A0E06),
                    accentContainer = Color(0xFF2E1C12),
                    onAccentContainer = Color(0xFFF7E2D4),
                    border = Color(0xFF29292E),
                    borderSubtle = Color(0xFF1F1F24),
                    userBubbleBg = Color(0xFF222226),
                    userBubbleText = Color(0xFFEDEDEB),
                    assistantBubbleBg = Color.Transparent,
                    assistantBubbleText = Color(0xFFEDEDEB),
                    codeBg = Color(0xFF0D0D0F),
                    codeText = Color(0xFFE4E4E6),
                    statusGreen = Color(0xFF4E9E68),
                    statusAmber = Color(0xFFD98A36),
                    statusRed = Color(0xFFBA4D4D)
                )
            } else {
                LocalMindColors(
                    background = Color(0xFFF9F8F5), // Warm paper
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFF1EFEA),
                    surfaceHover = Color(0xFFE8E5DE),
                    textPrimary = Color(0xFF1C1B1A),
                    textSecondary = Color(0xFF6C6963),
                    textTertiary = Color(0xFF9C9992),
                    accent = Color(0xFFB46B38),
                    onAccent = Color(0xFFFFFFFF),
                    accentContainer = Color(0xFFFBECE2),
                    onAccentContainer = Color(0xFF4E260E),
                    border = Color(0xFFE2DFD7),
                    borderSubtle = Color(0xFFECEAE3),
                    userBubbleBg = Color(0xFFEFECE5),
                    userBubbleText = Color(0xFF1C1B1A),
                    assistantBubbleBg = Color.Transparent,
                    assistantBubbleText = Color(0xFF1C1B1A),
                    codeBg = Color(0xFFF0EDE6),
                    codeText = Color(0xFF1C1B1A),
                    statusGreen = Color(0xFF2D7A46),
                    statusAmber = Color(0xFFB46B38),
                    statusRed = Color(0xFFA63939)
                )
            }
        }
        LocalMindThemeStyle.PREMIUM_SOFT -> {
            if (isDark) {
                LocalMindColors(
                    background = Color(0xFF12161E),
                    surface = Color(0xFF1A202C),
                    surfaceVariant = Color(0xFF222A3A),
                    surfaceHover = Color(0xFF2B3547),
                    textPrimary = Color(0xFFF1F5F9),
                    textSecondary = Color(0xFF94A3B8),
                    textTertiary = Color(0xFF64748B),
                    accent = Color(0xFF6BA3C7), // Powder slate
                    onAccent = Color(0xFF0F202D),
                    accentContainer = Color(0xFF1B3145),
                    onAccentContainer = Color(0xFFE0F0FA),
                    border = Color(0xFF283446),
                    borderSubtle = Color(0xFF1E2838),
                    userBubbleBg = Color(0xFF1E293B),
                    userBubbleText = Color(0xFFF1F5F9),
                    assistantBubbleBg = Color(0xFF1A202C),
                    assistantBubbleText = Color(0xFFF1F5F9),
                    codeBg = Color(0xFF0E131B),
                    codeText = Color(0xFFE2E8F0),
                    statusGreen = Color(0xFF34D399),
                    statusAmber = Color(0xFFFBBF24),
                    statusRed = Color(0xFFF87171)
                )
            } else {
                LocalMindColors(
                    background = Color(0xFFF6F8FA),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFEDF2F7),
                    surfaceHover = Color(0xFFE2E8F0),
                    textPrimary = Color(0xFF1E293B),
                    textSecondary = Color(0xFF64748B),
                    textTertiary = Color(0xFF94A3B8),
                    accent = Color(0xFF457B9D),
                    onAccent = Color(0xFFFFFFFF),
                    accentContainer = Color(0xFFE2F0F9),
                    onAccentContainer = Color(0xFF153549),
                    border = Color(0xFFE2E8F0),
                    borderSubtle = Color(0xFFEDF2F7),
                    userBubbleBg = Color(0xFFEDF2F7),
                    userBubbleText = Color(0xFF1E293B),
                    assistantBubbleBg = Color(0xFFFFFFFF),
                    assistantBubbleText = Color(0xFF1E293B),
                    codeBg = Color(0xFFF1F5F9),
                    codeText = Color(0xFF1E293B),
                    statusGreen = Color(0xFF10B981),
                    statusAmber = Color(0xFFF59E0B),
                    statusRed = Color(0xFFEF4444)
                )
            }
        }
        LocalMindThemeStyle.MONOCHROME -> {
            if (isDark) {
                LocalMindColors(
                    background = Color(0xFF000000),
                    surface = Color(0xFF111111),
                    surfaceVariant = Color(0xFF1C1C1C),
                    surfaceHover = Color(0xFF262626),
                    textPrimary = Color(0xFFFFFFFF),
                    textSecondary = Color(0xFFAAAAAA),
                    textTertiary = Color(0xFF666666),
                    accent = Color(0xFFFFFFFF),
                    onAccent = Color(0xFF000000),
                    accentContainer = Color(0xFF262626),
                    onAccentContainer = Color(0xFFFFFFFF),
                    border = Color(0xFF333333),
                    borderSubtle = Color(0xFF222222),
                    userBubbleBg = Color(0xFF1A1A1A),
                    userBubbleText = Color(0xFFFFFFFF),
                    assistantBubbleBg = Color(0xFF111111),
                    assistantBubbleText = Color(0xFFFFFFFF),
                    codeBg = Color(0xFF0A0A0A),
                    codeText = Color(0xFFEEEEEE),
                    statusGreen = Color(0xFFFFFFFF),
                    statusAmber = Color(0xFFCCCCCC),
                    statusRed = Color(0xFF888888)
                )
            } else {
                LocalMindColors(
                    background = Color(0xFFFAFAFA),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFEFEFEF),
                    surfaceHover = Color(0xFFE4E4E4),
                    textPrimary = Color(0xFF000000),
                    textSecondary = Color(0xFF555555),
                    textTertiary = Color(0xFF888888),
                    accent = Color(0xFF000000),
                    onAccent = Color(0xFFFFFFFF),
                    accentContainer = Color(0xFFE5E5E5),
                    onAccentContainer = Color(0xFF000000),
                    border = Color(0xFF000000),
                    borderSubtle = Color(0xFFCCCCCC),
                    userBubbleBg = Color(0xFFEFEFEF),
                    userBubbleText = Color(0xFF000000),
                    assistantBubbleBg = Color(0xFFFFFFFF),
                    assistantBubbleText = Color(0xFF000000),
                    codeBg = Color(0xFFF2F2F2),
                    codeText = Color(0xFF000000),
                    statusGreen = Color(0xFF000000),
                    statusAmber = Color(0xFF444444),
                    statusRed = Color(0xFF777777)
                )
            }
        }
        LocalMindThemeStyle.MODERN_ORGANIC -> {
            if (isDark) {
                LocalMindColors(
                    background = Color(0xFF151412),
                    surface = Color(0xFF1F1D19),
                    surfaceVariant = Color(0xFF292622),
                    surfaceHover = Color(0xFF332F2A),
                    textPrimary = Color(0xFFF2EFE8),
                    textSecondary = Color(0xFFA69F94),
                    textTertiary = Color(0xFF7A746B),
                    accent = Color(0xFF5E9979), // Earthy sage
                    onAccent = Color(0xFF0B1F14),
                    accentContainer = Color(0xFF203529),
                    onAccentContainer = Color(0xFFE2F2E9),
                    border = Color(0xFF302B24),
                    borderSubtle = Color(0xFF24201A),
                    userBubbleBg = Color(0xFF292622),
                    userBubbleText = Color(0xFFF2EFE8),
                    assistantBubbleBg = Color(0xFF1F1D19),
                    assistantBubbleText = Color(0xFFF2EFE8),
                    codeBg = Color(0xFF100F0D),
                    codeText = Color(0xFFE6E2D8),
                    statusGreen = Color(0xFF5E9979),
                    statusAmber = Color(0xFFD49258),
                    statusRed = Color(0xFFCC5A5A)
                )
            } else {
                LocalMindColors(
                    background = Color(0xFFF4F1EA), // Oatmeal
                    surface = Color(0xFFFAF8F4),
                    surfaceVariant = Color(0xFFEAE5DA),
                    surfaceHover = Color(0xFFDFD9CC),
                    textPrimary = Color(0xFF23201C),
                    textSecondary = Color(0xFF6B655B),
                    textTertiary = Color(0xFF948C7F),
                    accent = Color(0xFF3D7058),
                    onAccent = Color(0xFFFFFFFF),
                    accentContainer = Color(0xFFE1EFE7),
                    onAccentContainer = Color(0xFF173827),
                    border = Color(0xFFDDD7CB),
                    borderSubtle = Color(0xFFE7E2D7),
                    userBubbleBg = Color(0xFFEAE5DA),
                    userBubbleText = Color(0xFF23201C),
                    assistantBubbleBg = Color(0xFFFAF8F4),
                    assistantBubbleText = Color(0xFF23201C),
                    codeBg = Color(0xFFECE7DC),
                    codeText = Color(0xFF23201C),
                    statusGreen = Color(0xFF3D7058),
                    statusAmber = Color(0xFFBF7434),
                    statusRed = Color(0xFFB84040)
                )
            }
        }
        LocalMindThemeStyle.FUTURE_MINIMAL -> {
            if (isDark) {
                LocalMindColors(
                    background = Color(0xFF090A0C),
                    surface = Color(0xFF121418),
                    surfaceVariant = Color(0xFF1A1D23),
                    surfaceHover = Color(0xFF23272F),
                    textPrimary = Color(0xFFF8FAFC),
                    textSecondary = Color(0xFF94A3B8),
                    textTertiary = Color(0xFF64748B),
                    accent = Color(0xFFE2E8F0), // Titanium platinum
                    onAccent = Color(0xFF0F172A),
                    accentContainer = Color(0xFF262C38),
                    onAccentContainer = Color(0xFFF8FAFC),
                    border = Color(0xFF232731),
                    borderSubtle = Color(0xFF171920),
                    userBubbleBg = Color(0xFF1A1D23),
                    userBubbleText = Color(0xFFF8FAFC),
                    assistantBubbleBg = Color(0xFF121418),
                    assistantBubbleText = Color(0xFFF8FAFC),
                    codeBg = Color(0xFF0B0D10),
                    codeText = Color(0xFFE2E8F0),
                    statusGreen = Color(0xFF34D399),
                    statusAmber = Color(0xFFFBBF24),
                    statusRed = Color(0xFFF87171)
                )
            } else {
                LocalMindColors(
                    background = Color(0xFFF0F2F5), // Titanium
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFE4E7EB),
                    surfaceHover = Color(0xFFD9DDE2),
                    textPrimary = Color(0xFF111827),
                    textSecondary = Color(0xFF4B5563),
                    textTertiary = Color(0xFF9CA3AF),
                    accent = Color(0xFF374151),
                    onAccent = Color(0xFFFFFFFF),
                    accentContainer = Color(0xFFE5E7EB),
                    onAccentContainer = Color(0xFF111827),
                    border = Color(0xFFD1D5DB),
                    borderSubtle = Color(0xFFE5E7EB),
                    userBubbleBg = Color(0xFFE5E7EB),
                    userBubbleText = Color(0xFF111827),
                    assistantBubbleBg = Color(0xFFFFFFFF),
                    assistantBubbleText = Color(0xFF111827),
                    codeBg = Color(0xFFEAECEF),
                    codeText = Color(0xFF111827),
                    statusGreen = Color(0xFF059669),
                    statusAmber = Color(0xFFD97706),
                    statusRed = Color(0xFFDC2626)
                )
            }
        }
    }

    val shapes = when (style) {
        LocalMindThemeStyle.MINIMAL_EDITORIAL -> LocalMindShapes(
            cardShape = RoundedCornerShape(6.dp),
            buttonShape = RoundedCornerShape(6.dp),
            chipShape = RoundedCornerShape(4.dp),
            userMessageShape = RoundedCornerShape(6.dp),
            assistantMessageShape = RoundedCornerShape(6.dp),
            inputShape = RoundedCornerShape(8.dp),
            bottomSheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            tagShape = RoundedCornerShape(3.dp)
        )
        LocalMindThemeStyle.PREMIUM_SOFT -> LocalMindShapes(
            cardShape = RoundedCornerShape(18.dp),
            buttonShape = RoundedCornerShape(14.dp),
            chipShape = RoundedCornerShape(12.dp),
            userMessageShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
            assistantMessageShape = RoundedCornerShape(18.dp),
            inputShape = RoundedCornerShape(16.dp),
            bottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tagShape = RoundedCornerShape(8.dp)
        )
        LocalMindThemeStyle.MONOCHROME -> LocalMindShapes(
            cardShape = RoundedCornerShape(2.dp),
            buttonShape = RoundedCornerShape(2.dp),
            chipShape = RoundedCornerShape(2.dp),
            userMessageShape = RoundedCornerShape(2.dp),
            assistantMessageShape = RoundedCornerShape(2.dp),
            inputShape = RoundedCornerShape(2.dp),
            bottomSheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            tagShape = RoundedCornerShape(1.dp)
        )
        LocalMindThemeStyle.MODERN_ORGANIC -> LocalMindShapes(
            cardShape = RoundedCornerShape(24.dp),
            buttonShape = RoundedCornerShape(50.dp), // Full pill
            chipShape = RoundedCornerShape(16.dp),
            userMessageShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 6.dp),
            assistantMessageShape = RoundedCornerShape(22.dp),
            inputShape = RoundedCornerShape(28.dp),
            bottomSheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            tagShape = RoundedCornerShape(12.dp)
        )
        LocalMindThemeStyle.FUTURE_MINIMAL -> LocalMindShapes(
            cardShape = RoundedCornerShape(10.dp),
            buttonShape = RoundedCornerShape(8.dp),
            chipShape = RoundedCornerShape(6.dp),
            userMessageShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 2.dp),
            assistantMessageShape = RoundedCornerShape(10.dp),
            inputShape = RoundedCornerShape(10.dp),
            bottomSheetShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            tagShape = RoundedCornerShape(4.dp)
        )
    }

    val typography = when (style) {
        LocalMindThemeStyle.MINIMAL_EDITORIAL -> LocalMindTypography(
            displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Normal, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
            headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
            headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
            titleLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
            titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp),
            labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
            labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp),
            code = TextStyle(fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, lineHeight = 19.sp)
        )
        LocalMindThemeStyle.PREMIUM_SOFT -> LocalMindTypography(
            displayLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp),
            headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 31.sp),
            headlineMedium = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
            titleLarge = TextStyle(fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, lineHeight = 23.sp),
            titleMedium = TextStyle(fontSize = 14.5.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp),
            bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
            bodyMedium = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
            labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            labelSmall = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
            code = TextStyle(fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
        )
        LocalMindThemeStyle.MONOCHROME -> LocalMindTypography(
            displayLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 38.sp, letterSpacing = (-1).sp),
            headlineLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp, letterSpacing = (-0.5).sp),
            headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
            titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp),
            titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
            bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp),
            bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 19.sp),
            labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            code = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, lineHeight = 17.sp)
        )
        LocalMindThemeStyle.MODERN_ORGANIC -> LocalMindTypography(
            displayLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Medium, lineHeight = 38.sp),
            headlineLarge = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp),
            headlineMedium = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp),
            titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 23.sp),
            titleMedium = TextStyle(fontSize = 14.5.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp),
            bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 23.sp),
            bodyMedium = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp),
            labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            labelSmall = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Normal),
            code = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
        )
        LocalMindThemeStyle.FUTURE_MINIMAL -> LocalMindTypography(
            displayLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Medium, lineHeight = 34.sp, letterSpacing = 0.5.sp),
            headlineLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp, letterSpacing = 0.2.sp),
            headlineMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
            titleLarge = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
            titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
            bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp, letterSpacing = 0.1.sp),
            bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 19.sp),
            labelLarge = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp),
            labelSmall = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp),
            code = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
        )
    }

    val spacing = when (style) {
        LocalMindThemeStyle.MINIMAL_EDITORIAL -> LocalMindSpacing(
            screenHorizontalPadding = 22.dp,
            cardPadding = 16.dp,
            itemSpacing = 14.dp,
            borderWidth = 1.dp,
            elevation = 0.dp
        )
        LocalMindThemeStyle.PREMIUM_SOFT -> LocalMindSpacing(
            screenHorizontalPadding = 18.dp,
            cardPadding = 18.dp,
            itemSpacing = 14.dp,
            borderWidth = 0.8.dp,
            elevation = 2.dp
        )
        LocalMindThemeStyle.MONOCHROME -> LocalMindSpacing(
            screenHorizontalPadding = 16.dp,
            cardPadding = 14.dp,
            itemSpacing = 12.dp,
            borderWidth = 1.5.dp,
            elevation = 0.dp
        )
        LocalMindThemeStyle.MODERN_ORGANIC -> LocalMindSpacing(
            screenHorizontalPadding = 20.dp,
            cardPadding = 18.dp,
            itemSpacing = 16.dp,
            borderWidth = 0.8.dp,
            elevation = 1.dp
        )
        LocalMindThemeStyle.FUTURE_MINIMAL -> LocalMindSpacing(
            screenHorizontalPadding = 16.dp,
            cardPadding = 14.dp,
            itemSpacing = 12.dp,
            borderWidth = 0.8.dp,
            elevation = 0.dp
        )
    }

    return LocalMindTokens(
        style = style,
        isDark = isDark,
        colors = colors,
        shapes = shapes,
        typography = typography,
        spacing = spacing
    )
}
