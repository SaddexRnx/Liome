package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MarkdownContent(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = parseMarkdownBlocks(text)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Heading -> {
                    Text(
                        text = block.content,
                        fontSize = when (block.level) {
                            1 -> 18.sp
                            2 -> 16.sp
                            else -> 15.sp
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Code -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.content
                    )
                }
                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier.padding(start = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "–",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = renderInlineStyles(block.content, textColor),
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = textColor
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = renderInlineStyles(block.content, textColor),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val bg = if (isDark) CodeBgDark else CodeBgLight
    val borderCol = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.8.dp, borderCol, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.lowercase(),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                    Toast.makeText(context, "Copied code", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy code",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text = renderCodeHighlighting(code, isDark),
                fontSize = 12.5.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val content: String) : MarkdownBlock()
    data class Code(val language: String, val content: String) : MarkdownBlock()
    data class Bullet(val content: String) : MarkdownBlock()
    data class Paragraph(val content: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        if (line.trim().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.Code(lang, codeLines.joinToString("\n")))
            i++
            continue
        }

        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val content = line.substring(level).trim()
            blocks.add(MarkdownBlock.Heading(level, content))
            i++
            continue
        }

        if (line.trim().startsWith("• ") || line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            val content = line.trim().substring(2).trim()
            blocks.add(MarkdownBlock.Bullet(content))
            i++
            continue
        }

        if (line.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(line))
        }

        i++
    }

    return blocks
}

private fun renderInlineStyles(text: String, defaultColor: Color) = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        if (text.startsWith("**", index)) {
            val end = text.indexOf("**", index + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(text.substring(index + 2, end))
                }
                index = end + 2
                continue
            }
        }

        if (text.startsWith("`", index)) {
            val end = text.indexOf("`", index + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = defaultColor.copy(alpha = 0.08f),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(text.substring(index + 1, end))
                }
                index = end + 1
                continue
            }
        }

        append(text[index])
        index++
    }
}

private fun renderCodeHighlighting(code: String, isDark: Boolean) = buildAnnotatedString {
    val keywords = setOf(
        "fun", "val", "var", "def", "class", "import", "return", "if", "else", "for",
        "while", "try", "catch", "throw", "suspend", "flow", "private", "public", "const",
        "async", "await", "function", "let", "switch", "case", "default"
    )

    val lines = code.lines()
    for ((lineIdx, line) in lines.withIndex()) {
        if (lineIdx > 0) append("\n")
        val trimmed = line.trimStart()
        if (trimmed.startsWith("//") || trimmed.startsWith("#")) {
            withStyle(SpanStyle(color = CodeCommentColor)) {
                append(line)
            }
            continue
        }

        val words = line.split("(?<=\\s|\\b)|(?=\\s|\\b)".toRegex())
        for (word in words) {
            when {
                word in keywords -> {
                    withStyle(SpanStyle(color = CodeKeywordColor, fontWeight = FontWeight.Medium)) {
                        append(word)
                    }
                }
                word.startsWith("\"") && word.endsWith("\"") -> {
                    withStyle(SpanStyle(color = CodeStringColor)) {
                        append(word)
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = if (isDark) CodeTextDark else CodeTextLight)) {
                        append(word)
                    }
                }
            }
        }
    }
}
