package com.example.pathx01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pathx01.ai.AnthropicClient
import com.example.pathx01.ai.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun AiChatDialog(
    onDismiss: () -> Unit,
    initialContext: String = ""
) {
    val scope = rememberCoroutineScope()
    val client = remember { AnthropicClient() }

    var input by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialContext) {
        if (initialContext.isNotBlank()) {
            messages.add(ChatMessage("user", "Context from current note/journal:\n" + initialContext.take(2000)))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Assistant", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 360.dp)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            val bg = if (msg.role == "user") MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg, shape = MaterialTheme.shapes.small)
                                    .padding(8.dp)
                            ) {
                                Text(if (msg.role == "user") "You" else "AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(2.dp))
                                Text(msg.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Ask for help, ideas, or edits…") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, enabled = !isSending) { Text("Close") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (input.isBlank() || isSending) return@Button
                        val userMsg = ChatMessage("user", input)
                        messages.add(userMsg)
                        input = ""
                        isSending = true
                        error = null
                        scope.launch {
                            try {
                                val reply = client.sendMessages(messages.toList())
                                messages.add(ChatMessage("assistant", reply.ifBlank { "(No response)" }))
                            } catch (t: Throwable) {
                                error = t.message ?: "Unknown error"
                            } finally {
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending
                ) {
                    Text(if (isSending) "Sending…" else "Send")
                }
            }
        }
    )
}
