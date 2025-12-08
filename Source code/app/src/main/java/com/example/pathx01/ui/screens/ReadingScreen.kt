package com.example.pathx01.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.pathx01.R
import com.example.pathx01.data.DataManager
import com.example.pathx01.data.model.Book
import com.example.pathx01.data.model.BookStatus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen() {
    // Use DataManager for persistent data
    var books by remember { mutableStateOf(DataManager.getBooks()) }
    
    // Update books when DataManager changes
    LaunchedEffect(Unit) {
        books = DataManager.getBooks()
    }
    
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showUpdateProgressDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<Book?>(null) }
    var updatingBook by remember { mutableStateOf<Book?>(null) }
    var deletingBook by remember { mutableStateOf<Book?>(null) }
    var showInstructions by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // 2xl spacing from DayFlow design system
            verticalArrangement = Arrangement.spacedBy(24.dp) // 2xl spacing
        ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Reading Log",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showInstructions = !showInstructions }) {
                        Text(
                            text = "🧭",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = { showAddBookDialog = true },
                        modifier = Modifier.size(56.dp), // FAB size from DayFlow design system
                        containerColor = MaterialTheme.colorScheme.secondary, // Gold color
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp) // FAB radius from DayFlow
                    ) {
                        Icon(painterResource(R.drawable.ic_add), "Add Book")
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Your Books (${books.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (books.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📚 No books yet!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start building your reading collection by adding your first book!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(books) { book ->
                BookCard(
                    book = book,
                    onEdit = { 
                        editingBook = book
                        showEditBookDialog = true
                    },
                    onDelete = { 
                        deletingBook = book
                        showDeleteConfirmDialog = true
                    },
                    onUpdateProgress = { 
                        updatingBook = book
                        showUpdateProgressDialog = true
                    }
                )
            }
        }
    }
    
    // Add Book Dialog
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onAddBook = { title, author, totalPages ->
                val newBook = Book(
                    id = 0, // Auto-generated
                    title = title,
                    author = author,
                    status = BookStatus.TO_READ,
                    totalPages = totalPages,
                    pagesRead = 0,
                    rating = null,
                    startedDate = null,
                    completedDate = null
                )
                DataManager.addBook(newBook)
                books = DataManager.getBooks()
                showAddBookDialog = false
            }
        )
    }
    
    // Edit Book Dialog
    if (showEditBookDialog && editingBook != null) {
        EditBookDialog(
            book = editingBook!!,
            onDismiss = { 
                showEditBookDialog = false
                editingBook = null
            },
            onUpdateBook = { updatedBook ->
                DataManager.updateBook(updatedBook)
                books = DataManager.getBooks()
                showEditBookDialog = false
                editingBook = null
            }
        )
    }
    
    // Update Progress Dialog
    if (showUpdateProgressDialog && updatingBook != null) {
        UpdateProgressDialog(
            book = updatingBook!!,
            onDismiss = { 
                showUpdateProgressDialog = false
                updatingBook = null
            },
            onUpdateProgress = { pagesRead ->
                val book = updatingBook!!
                val totalPages = book.totalPages ?: 0
                val isCompleted = totalPages > 0 && pagesRead >= totalPages
                val updatedBook = book.copy(
                    pagesRead = pagesRead,
                    status = if (isCompleted) BookStatus.COMPLETED else BookStatus.READING,
                    completedDate = if (isCompleted) LocalDateTime.now() else null,
                    startedDate = if (book.startedDate == null && pagesRead > 0) LocalDateTime.now() else book.startedDate
                )
                DataManager.updateBook(updatedBook)
                books = DataManager.getBooks()
                showUpdateProgressDialog = false
                updatingBook = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && deletingBook != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false
                deletingBook = null
            },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete '${deletingBook?.title}' from your reading log?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        DataManager.deleteBook(deletingBook?.id ?: -1)
                        books = DataManager.getBooks()
                        showDeleteConfirmDialog = false
                        deletingBook = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false
                        deletingBook = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Instructions Dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("Reading Log Instructions") },
            text = {
                Column {
                    Text("Welcome to your Reading Log!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Tap + to add new books to your reading list")
                    Text("• Set total pages when adding a book")
                    Text("• Update progress as you read")
                    Text("• Progress bar shows completion percentage")
                    Text("• Tap edit to modify book details")
                    Text("• Rate completed books 1-5 stars")
                    Text("• Delete books you no longer want to track")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun BookCard(
    book: Book,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (book.totalPages != null && book.totalPages > 0) book.pagesRead.toFloat() / book.totalPages.toFloat() else 0f
    val percentage = (progress * 100).toInt()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (book.startedDate != null) {
                        Text(
                            text = "Started: ${book.startedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (book.completedDate != null) {
                        Text(
                            text = "Completed: ${book.completedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(painterResource(R.drawable.ic_edit), "Edit Book")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(painterResource(R.drawable.ic_delete), "Delete Book", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Reading Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress: ${book.pagesRead}/${book.totalPages ?: 0} pages ($percentage%)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (book.status != BookStatus.COMPLETED) {
                    TextButton(onClick = onUpdateProgress) {
                        Text("Update Progress")
                    }
                }
            }
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status and Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(book.status.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (book.status) {
                            BookStatus.READING -> MaterialTheme.colorScheme.primaryContainer
                            BookStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
                            BookStatus.TO_READ -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
                
                if (book.rating != null) {
                    AssistChip(
                        onClick = { },
                        label = { Text("⭐ ${book.rating}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onAddBook: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var totalPagesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Book") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = totalPagesText,
                    onValueChange = { totalPagesText = it },
                    label = { Text("Total Pages") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank() && totalPagesText.isNotBlank()) {
                        val totalPages = totalPagesText.toIntOrNull() ?: 0
                        if (totalPages > 0) {
                            onAddBook(title, author, totalPages)
                        }
                    }
                },
                enabled = title.isNotBlank() && author.isNotBlank() && totalPagesText.isNotBlank()
            ) {
                Text("Add Book")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onUpdateBook: (Book) -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var totalPagesText by remember { mutableStateOf(book.totalPages?.toString() ?: "") }
    var rating by remember { mutableStateOf(book.rating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = totalPagesText,
                    onValueChange = { totalPagesText = it },
                    label = { Text("Total Pages") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Rating Selection
                if (book.status == BookStatus.COMPLETED) {
                    Text(
                        text = "Rating (1-5 stars)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..5) {
                            FilterChip(
                                selected = rating == i,
                                onClick = { rating = if (rating == i) null else i },
                                label = { Text("$i") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank() && totalPagesText.isNotBlank()) {
                        val totalPages = totalPagesText.toIntOrNull() ?: book.totalPages ?: 0
                        val updatedBook = book.copy(
                            title = title,
                            author = author,
                            totalPages = totalPages,
                            rating = rating
                        )
                        onUpdateBook(updatedBook)
                    }
                },
                enabled = title.isNotBlank() && author.isNotBlank() && totalPagesText.isNotBlank()
            ) {
                Text("Update Book")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdateProgressDialog(
    book: Book,
    onDismiss: () -> Unit,
    onUpdateProgress: (Int) -> Unit
) {
    var pagesReadText by remember { mutableStateOf(book.pagesRead.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Reading Progress") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "How many pages have you read in '${book.title}'?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = pagesReadText,
                    onValueChange = { pagesReadText = it },
                    label = { Text("Pages Read") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Total pages: ${book.totalPages ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (pagesReadText.isNotBlank()) {
                    val pagesRead = pagesReadText.toIntOrNull() ?: 0
                    val totalPages = book.totalPages ?: 0
                    val percentage = if (totalPages > 0) (pagesRead.toFloat() / totalPages * 100).toInt() else 0
                    
                    Text(
                        text = "Progress: $percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (totalPages > 0 && pagesRead >= totalPages) {
                        Text(
                            text = "🎉 Congratulations! You've finished this book!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pagesRead = pagesReadText.toIntOrNull() ?: book.pagesRead
                    val totalPages = book.totalPages ?: 0
                    if (pagesRead >= 0 && (totalPages == 0 || pagesRead <= totalPages)) {
                        onUpdateProgress(pagesRead)
                    }
                },
                enabled = pagesReadText.isNotBlank()
            ) {
                Text("Update Progress")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

