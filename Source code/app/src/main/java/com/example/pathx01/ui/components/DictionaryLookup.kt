package com.example.pathx01.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.pathx01.data.dictionary.DictionaryEntry
import com.example.pathx01.data.dictionary.DictionaryRepository
import androidx.compose.foundation.background
import kotlinx.coroutines.CancellationException

@Composable
fun DictionaryLookupPanel(
    onAddToNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { DictionaryRepository(context) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<DictionaryEntry>()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val dictionarySize = remember { mutableStateOf(0) }
    // On first render, asynchronously measure dictionary size
    LaunchedEffect(Unit) {
        try {
            dictionarySize.value = repository.size()
        } catch (_: CancellationException) {
            // ignore: composition left
        } catch (_: Exception) {
            dictionarySize.value = 0
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Warning banner if dictionary entries < 1000
            if (dictionarySize.value in 1..999) {
                Box(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer).padding(8.dp)
                ) {
                    Text(
                        text = "⚠️ The full dictionary is missing or failed to load. Only a tiny sample is available. To fix: Copy all WordNet database files (data.noun, index.noun, ... etc) into app/src/main/assets/wordnet in this project, uninstall the app, and reinstall.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = "Dictionary",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search word") },
                trailingIcon = {
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Auto-search as user types (debounced)
            LaunchedEffect(query) {
                if (query.isBlank()) {
                    results = emptyList()
                    errorMessage = null
                    return@LaunchedEffect
                }
                
                isSearching = true
                errorMessage = null
                
                // Debounce: wait 500ms after user stops typing
                kotlinx.coroutines.delay(500)
                
                if (query.isNotBlank()) {
                    try {
                        results = repository.search(query)
                        errorMessage = null
                    } catch (_: CancellationException) {
                        return@LaunchedEffect
                    } catch (e: Exception) {
                        errorMessage = "Search error: ${e.message}"
                        results = emptyList()
                    }
                }
                isSearching = false
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Status message
            if (!isSearching && query.isNotBlank() && results.isEmpty() && errorMessage == null) {
                Text(
                    text = "No results for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isSearching) {
                Text(
                    text = "Searching...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                results.forEach { entry ->
                    DictionaryResultItem(entry = entry, onAddToNote = onAddToNote)
                }
            }
        }
    }
}

@Composable
private fun DictionaryResultItem(
    entry: DictionaryEntry,
    onAddToNote: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = entry.word, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.meaning, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val text = "${entry.word} — ${entry.meaning}"
                    onAddToNote(text)
                }) {
                    Text("Add to note")
                }
                OutlinedButton(onClick = {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString("${entry.word} — ${entry.meaning}"))
                }) {
                    Text("Copy")
                }
            }
        }
    }
}


