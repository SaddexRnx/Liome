package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.ui.components.*
import com.example.ui.theme.LocalMindTheme
import com.example.ui.theme.LocalMindThemeStyle
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToModels: () -> Unit,
    currentThemeStyle: LocalMindThemeStyle = LocalMindThemeStyle.MINIMAL_EDITORIAL,
    onSelectThemeStyle: (LocalMindThemeStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()
    val tokens = LocalMindTheme.tokens

    var inputText by remember { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var conversationToRename by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        if (uiState.messages.isNotEmpty() || uiState.streamingContent.isNotEmpty()) {
            val totalCount = uiState.messages.size + (if (uiState.isGenerating) 1 else 0)
            if (totalCount > 0) {
                listState.animateScrollToItem(totalCount - 1)
            }
        }
    }

    LocalMindScaffold(
        modifier = modifier,
        topBar = {
            LocalMindTopBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(tokens.shapes.chipShape)
                            .clickable { onNavigateToModels() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isModelLoaded) tokens.colors.statusGreen else tokens.colors.statusAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.activeModelName,
                                    style = tokens.typography.titleMedium,
                                    color = tokens.colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                if (!uiState.isModelLoaded) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(tokens.shapes.chipShape)
                                            .background(tokens.colors.surfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Not Loaded",
                                            style = tokens.typography.labelSmall,
                                            color = tokens.colors.textTertiary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (uiState.isModelLoaded) "Model loaded into memory" else "Selected model — not loaded",
                                style = tokens.typography.labelSmall,
                                color = if (uiState.isModelLoaded) tokens.colors.statusGreen else tokens.colors.textTertiary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Switch model",
                            modifier = Modifier.size(16.dp),
                            tint = tokens.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("chat_history_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notes,
                            contentDescription = "History",
                            tint = tokens.colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    // Visual Theme Style Picker Quick Action
                    IconButton(
                        onClick = { showThemeSheet = true },
                        modifier = Modifier.testTag("theme_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Explore Theme Styles",
                            tint = tokens.colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.startNewConversation() },
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "New chat",
                            tint = tokens.colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                isGenerating = uiState.isGenerating,
                onSend = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                onStop = { viewModel.stopGeneration() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                EmptyChatPlaceholder(
                    modelName = uiState.activeModelName,
                    isModelLoaded = uiState.isModelLoaded,
                    onSuggestionClick = { prompt -> viewModel.sendMessage(prompt) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.spacing.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.spacing.itemSpacing),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            isNativeEngineAvailable = uiState.isNativeEngineAvailable,
                            onRegenerate = { viewModel.regenerateLastMessage() },
                            isLastMessage = message == uiState.messages.lastOrNull()
                        )
                    }

                    if (uiState.isGenerating) {
                        item(key = "streaming_bubble") {
                            StreamingBubble(
                                text = uiState.streamingContent,
                                isModelLoaded = uiState.isModelLoaded
                            )
                        }
                    }
                }
            }
        }
    }

    // 5-Theme Style Switcher Bottom Sheet
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
                    text = "Visual Exploration",
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

    // Conversation History Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
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
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tokens.style == LocalMindThemeStyle.MONOCHROME) "CONVERSATIONS" else "Conversations",
                        style = tokens.typography.titleLarge,
                        color = tokens.colors.textPrimary
                    )
                    TextButton(
                        onClick = {
                            viewModel.startNewConversation()
                            showHistorySheet = false
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = tokens.colors.accent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", style = tokens.typography.labelLarge, color = tokens.colors.accent)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (conversations.isEmpty()) {
                    Text(
                        text = "No saved conversations.",
                        style = tokens.typography.bodyMedium,
                        color = tokens.colors.textSecondary,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(conversations, key = { it.id }) { conv ->
                            val isSelected = conv.id == activeConversationId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(tokens.shapes.chipShape)
                                    .background(
                                        if (isSelected) tokens.colors.surfaceVariant
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.selectConversation(conv.id)
                                        showHistorySheet = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = conv.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = tokens.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = tokens.colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            conversationToRename = conv
                                            renameText = conv.title
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Rename",
                                            modifier = Modifier.size(14.dp),
                                            tint = tokens.colors.textSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteConversation(conv.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(14.dp),
                                            tint = tokens.colors.statusRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    conversationToRename?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToRename = null },
            containerColor = tokens.colors.surface,
            shape = tokens.shapes.cardShape,
            title = { Text("Rename conversation", style = tokens.typography.titleMedium, color = tokens.colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = tokens.shapes.inputShape,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameConversation(conv.id, renameText.trim())
                        }
                        conversationToRename = null
                    }
                ) {
                    Text("Save", color = tokens.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToRename = null }) {
                    Text("Cancel", color = tokens.colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun EmptyChatPlaceholder(
    modelName: String,
    isModelLoaded: Boolean,
    onSuggestionClick: (String) -> Unit
) {
    val tokens = LocalMindTheme.tokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = tokens.spacing.screenHorizontalPadding),
        horizontalAlignment = if (tokens.style == LocalMindThemeStyle.MINIMAL_EDITORIAL) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (tokens.style == LocalMindThemeStyle.MONOCHROME) "LOCALMIND" else "LocalMind",
            style = tokens.typography.displayLarge,
            color = tokens.colors.textPrimary,
            fontWeight = if (tokens.style == LocalMindThemeStyle.MONOCHROME) FontWeight.ExtraBold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isModelLoaded) "Executing on device with $modelName."
                   else "Offline assistant ready. ($modelName selected — not loaded)",
            style = tokens.typography.bodyMedium,
            color = tokens.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        val prompts = listOf(
            "hi",
            "what is 2 + 2?",
            "explain photosynthesis briefly",
            "write a simple Python hello world"
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (p in prompts) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(tokens.shapes.cardShape)
                        .background(tokens.colors.surface)
                        .border(tokens.spacing.borderWidth, tokens.colors.border, tokens.shapes.cardShape)
                        .clickable { onSuggestionClick(p) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = p,
                        style = tokens.typography.bodyMedium,
                        color = tokens.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: MessageEntity,
    isNativeEngineAvailable: Boolean,
    onRegenerate: () -> Unit,
    isLastMessage: Boolean
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    val tokens = LocalMindTheme.tokens

    Column(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = message.content,
                    style = tokens.typography.bodyLarge,
                    color = tokens.colors.userBubbleText
                )
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
                                .padding(14.dp)
                        } else {
                            Modifier.padding(vertical = 4.dp)
                        }
                    )
            ) {
                MarkdownContent(
                    text = message.content,
                    textColor = tokens.colors.assistantBubbleText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isNativeEngineAvailable && message.tokensPerSecond > 0f) {
                        Text(
                            text = "${(message.tokensPerSecond * 10).toInt() / 10f} tok/s • ${message.completionTokens} tokens",
                            style = tokens.typography.labelSmall,
                            color = tokens.colors.textSecondary
                        )
                    } else {
                        Text(
                            text = "Local response",
                            style = tokens.typography.labelSmall,
                            color = tokens.colors.textTertiary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("LocalMind", message.content))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(13.dp),
                                tint = tokens.colors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, message.content)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share"))
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(13.dp),
                                tint = tokens.colors.textSecondary
                            )
                        }

                        if (isLastMessage) {
                            IconButton(
                                onClick = onRegenerate,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Regenerate",
                                    modifier = Modifier.size(13.dp),
                                    tint = tokens.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingBubble(
    text: String,
    isModelLoaded: Boolean
) {
    val tokens = LocalMindTheme.tokens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (text.isNotBlank()) {
            MarkdownContent(text = text, textColor = tokens.colors.assistantBubbleText)
        } else {
            Text(
                text = if (isModelLoaded) "Running local inference..." else "Generating local response...",
                style = tokens.typography.bodyMedium,
                color = tokens.colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isModelLoaded) "Streaming local inference" else "Local response",
            style = tokens.typography.labelSmall,
            color = tokens.colors.textTertiary
        )
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isGenerating: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val tokens = LocalMindTheme.tokens

    Surface(
        color = tokens.colors.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.spacing.screenHorizontalPadding, vertical = 10.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = {
                    Text(
                        text = "Message LocalMind...",
                        style = tokens.typography.bodyMedium,
                        color = tokens.colors.textSecondary
                    )
                },
                maxLines = 4,
                shape = tokens.shapes.inputShape,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = tokens.colors.surface,
                    unfocusedContainerColor = tokens.colors.surface,
                    focusedBorderColor = tokens.colors.accent,
                    unfocusedBorderColor = tokens.colors.border,
                    focusedTextColor = tokens.colors.textPrimary,
                    unfocusedTextColor = tokens.colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isGenerating) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(tokens.shapes.buttonShape)
                        .background(tokens.colors.surfaceVariant)
                        .testTag("stop_generation_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = "Stop",
                        tint = tokens.colors.statusRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(tokens.shapes.buttonShape)
                        .background(
                            if (inputText.isNotBlank()) tokens.colors.accent
                            else tokens.colors.surfaceVariant
                        )
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) tokens.colors.onAccent
                        else tokens.colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
