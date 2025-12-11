package com.prody.prashant.ui.screens.buddha

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import com.prody.prashant.ai.BuddhaResponse
import com.prody.prashant.data.local.entity.BuddhaMessageEntity
import com.prody.prashant.data.local.entity.BuddhaMode
import com.prody.prashant.data.local.entity.MessageRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuddhaConversationScreen(
    conversationId: Long?,
    journalId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var currentConversationId by remember { mutableStateOf(conversationId) }
    var messageInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val messages by remember(currentConversationId) {
        if (currentConversationId != null) {
            app.buddhaRepository.observeMessagesByConversation(currentConversationId!!)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val buddhaMode by app.preferencesManager.buddhaMode
        .collectAsStateWithLifecycle(initialValue = BuddhaMode.STOIC)

    // Initialize conversation
    LaunchedEffect(Unit) {
        if (currentConversationId == null) {
            currentConversationId = app.buddhaRepository.createConversation()
            app.userProgressRepository.incrementBuddhaConversations()
        }

        // If coming from a journal entry, add context
        if (journalId != null) {
            val journal = app.journalRepository.getById(journalId)
            if (journal != null) {
                val contextMessage = "I'd like to discuss something I wrote in my journal: \"${journal.content.take(500)}${if (journal.content.length > 500) "..." else ""}\""
                messageInput = contextMessage
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Buddha", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = buddhaMode.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                value = messageInput,
                onValueChange = { messageInput = it },
                onSend = {
                    if (messageInput.isNotBlank() && !isLoading && currentConversationId != null) {
                        val message = messageInput.trim()
                        messageInput = ""
                        isLoading = true

                        scope.launch {
                            // Add user message
                            app.buddhaRepository.addUserMessage(
                                conversationId = currentConversationId!!,
                                content = message,
                                relatedJournalId = journalId
                            )

                            // Update daily stats
                            app.userProgressRepository.incrementTodayStats(buddhaMessages = 1)

                            // Get Buddha's response
                            val aiService = app.buddhaAiService
                            if (aiService != null && aiService.isConfigured()) {
                                val conversationHistory = app.buddhaRepository.buildConversationContext(
                                    currentConversationId!!,
                                    messageLimit = 10
                                )

                                val response = aiService.chat(
                                    userMessage = message,
                                    conversationHistory = conversationHistory,
                                    mode = buddhaMode
                                )

                                when (response) {
                                    is BuddhaResponse.Success -> {
                                        app.buddhaRepository.addBuddhaMessage(
                                            conversationId = currentConversationId!!,
                                            content = response.message,
                                            tokenCount = response.tokenCount,
                                            responseTimeMs = response.responseTimeMs,
                                            modelUsed = "gemini"
                                        )

                                        // Update daily stats for Buddha response
                                        app.userProgressRepository.incrementTodayStats(buddhaMessages = 1)
                                    }
                                    is BuddhaResponse.Error -> {
                                        // Add error message
                                        app.buddhaRepository.addBuddhaMessage(
                                            conversationId = currentConversationId!!,
                                            content = "I apologize, but I'm having trouble connecting right now. ${response.message}"
                                        )
                                    }
                                }
                            } else {
                                app.buddhaRepository.addBuddhaMessage(
                                    conversationId = currentConversationId!!,
                                    content = "Please configure your Gemini API key in Settings to enable our conversations."
                                )
                            }

                            isLoading = false
                        }
                    }
                },
                isLoading = isLoading
            )
        }
    ) { padding ->
        if (messages.isEmpty() && !isLoading) {
            // Empty state with greeting
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Greetings, seeker.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "What weighs upon your mind today? Share your thoughts, and let us explore them together.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Share your thoughts...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: BuddhaMessageEntity) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
