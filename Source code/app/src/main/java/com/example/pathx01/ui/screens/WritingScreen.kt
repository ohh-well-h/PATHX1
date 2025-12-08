package com.example.pathx01.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.pathx01.R
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.pathx01.data.model.WritingEntry
import com.example.pathx01.data.model.WritingType
import com.example.pathx01.data.model.Attachment
import com.example.pathx01.data.model.AttachmentType
import com.example.pathx01.data.model.ChecklistItem
import com.example.pathx01.data.DataManager
import com.example.pathx01.ui.components.DocumentReaderDialog
import com.example.pathx01.ui.components.DictionaryLookupPanel
import com.example.pathx01.ui.components.AiChatDialog
import com.example.pathx01.utils.FileTypeUtils
import com.example.pathx01.utils.FileUtils
import com.example.pathx01.utils.MoodUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.min
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen() {
    val writingEntries = DataManager.getWritingEntries()
    
    var selectedType by remember { mutableStateOf(WritingType.JOURNAL) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var initialContentForAdd by remember { mutableStateOf("") }
    var showEditEntryDialog by remember { mutableStateOf(false) }
    var showViewEntryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<WritingEntry?>(null) }
    var viewingEntry by remember { mutableStateOf<WritingEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<WritingEntry?>(null) }
    var showInstructions by remember { mutableStateOf(false) }
    var showDictionaryPanel by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    
    val filteredEntries = writingEntries.filter { it.type == selectedType }

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
                    text = "Writing",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { showAiDialog = true }) {
                        Text(
                            text = "🤖",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    IconButton(onClick = { showInstructions = !showInstructions }) {
                        Text(
                            text = "🧭",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedType = WritingType.JOURNAL },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == WritingType.JOURNAL) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text("Journaling")
                }

                Button(
                    onClick = { selectedType = WritingType.NOTE },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == WritingType.NOTE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text("Notes")
                }
            }
        }
        
        // Journal-only Mood Tracking Board, or Notes-only Dictionary trigger
        if (selectedType == WritingType.JOURNAL) {
            item {
                MoodTrackingBoard()
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showDictionaryPanel = !showDictionaryPanel }) {
                        Icon(Icons.Filled.Search, contentDescription = "Dictionary search")
                    }
                }
            }
            if (showDictionaryPanel) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Dictionary",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DictionaryLookupPanel(
                                onAddToNote = { insertion ->
                                    initialContentForAdd = if (initialContentForAdd.isBlank()) insertion else initialContentForAdd + "\n" + insertion
                                    showAddEntryDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedType == WritingType.JOURNAL) "Journal Entries" else "Notes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                FloatingActionButton(
                    onClick = { showAddEntryDialog = true },
                    modifier = Modifier.size(56.dp), // FAB size from DayFlow design system
                    containerColor = MaterialTheme.colorScheme.secondary, // Gold color
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp) // FAB radius from DayFlow
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add ${selectedType.name.lowercase()}"
                    )
                }
            }
        }

        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (selectedType == WritingType.JOURNAL) {
                            "No journal entries yet. Start writing your thoughts! ✍️"
                        } else {
                            "No notes yet. Capture your ideas! 💡"
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredEntries) { entry ->
                WritingEntryCard(
                    entry = entry,
                    onEdit = { 
                        editingEntry = entry
                        showEditEntryDialog = true
                    },
                    onDelete = { 
                        deletingEntry = entry
                        showDeleteConfirmDialog = true
                    },
                    onView = {
                        viewingEntry = entry
                        showViewEntryDialog = true
                    }
                )
            }
        }
    }
    
    // Add Entry Dialog
    if (showAddEntryDialog) {
        AddWritingEntryDialog(
            onDismiss = { showAddEntryDialog = false },
            onAddEntry = { title, content, mood, tags, attachments, checklists ->
                val newEntry = WritingEntry(
                    id = (writingEntries.maxOfOrNull { it.id } ?: 0) + 1,
                    title = title,
                    content = content,
                    type = selectedType,
                    mood = mood,
                    tags = tags,
                    attachments = attachments,
                    checklists = checklists,
                    createdAt = LocalDateTime.now()
                )
                DataManager.addWritingEntry(newEntry)
                showAddEntryDialog = false
                initialContentForAdd = ""
            },
            entryType = selectedType,
            initialContent = initialContentForAdd
        )
    }
    
    // Edit Entry Dialog
    if (showEditEntryDialog && editingEntry != null) {
        EditWritingEntryDialog(
            entry = editingEntry!!,
            onDismiss = { 
                showEditEntryDialog = false
                editingEntry = null
            },
            onUpdateEntry = { updatedEntry ->
                DataManager.updateWritingEntry(updatedEntry)
                showEditEntryDialog = false
                editingEntry = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && deletingEntry != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false
                deletingEntry = null
            },
            title = { Text("Delete ${if (deletingEntry?.type == WritingType.JOURNAL) "Journal Entry" else "Note"}") },
            text = { Text("Are you sure you want to delete '${deletingEntry?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingEntry?.id?.let { DataManager.deleteWritingEntry(it) }
                        showDeleteConfirmDialog = false
                        deletingEntry = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false
                        deletingEntry = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // View Entry Dialog
    if (showViewEntryDialog && viewingEntry != null) {
        ViewWritingEntryDialog(
            entry = viewingEntry!!,
            onDismiss = { 
                showViewEntryDialog = false
                viewingEntry = null
            },
            onEdit = {
                showViewEntryDialog = false
                editingEntry = viewingEntry
                showEditEntryDialog = true
                viewingEntry = null
            }
        )
    }
    
    // Instructions Dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("Writing Instructions") },
            text = {
                Column {
                    Text("Welcome to your Writing section!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Switch between Journaling and Notes")
                    Text("• Tap + to create new entries")
                    Text("• Use rich text formatting (bold, italic)")
                    Text("• Add attachments like images")
                    Text("• Create checklists for tasks")
                    Text("• Tag entries for organization")
                    Text("• Edit or delete entries as needed")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) {
                    Text("Got it!")
                }
            }
        )
    }

    // AI Chat Dialog
    if (showAiDialog) {
        AiChatDialog(
            onDismiss = { showAiDialog = false },
            initialContext = ""
        )
    }
}

@Composable
fun WritingEntryCard(
    entry: WritingEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(painterResource(R.drawable.ic_edit), "Edit Entry")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(painterResource(R.drawable.ic_delete), "Delete Entry", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium
            )

            if (entry.mood != null && entry.type == WritingType.JOURNAL) {
                Spacer(modifier = Modifier.height(8.dp))
                val moodData = MoodUtils.getMoodByName(entry.mood)
                AssistChip(
                    onClick = { },
                    label = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (moodData != null) {
                                Icon(
                                    painter = painterResource(moodData.iconRes),
                                    contentDescription = moodData.name,
                                    tint = androidx.compose.ui.graphics.Color(moodData.color),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text("Mood: ${entry.mood}")
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (moodData != null) {
                            androidx.compose.ui.graphics.Color(moodData.color).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                )
            }

            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    entry.tags.forEach { tag ->
                        AssistChip(
                            onClick = { 
                                // TODO: Add tag filtering functionality
                            },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }

            if (entry.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Attachments",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.attachments.forEach { attachment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = when (attachment.type) {
                                        AttachmentType.IMAGE -> painterResource(R.drawable.ic_image)
                                        AttachmentType.AUDIO -> painterResource(R.drawable.ic_audio)
                                        AttachmentType.VIDEO -> painterResource(R.drawable.ic_video)
                                        AttachmentType.FILE -> painterResource(R.drawable.ic_file)
                                    },
                                    contentDescription = attachment.type.name,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = attachment.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = attachment.type.name.lowercase(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (entry.checklists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val completed = entry.checklists.count { it.isCompleted }
                val total = entry.checklists.size
                Text(
                    text = "Checklist: $completed/$total completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}

@Composable
fun AddWritingEntryDialog(
    onDismiss: () -> Unit,
    onAddEntry: (String, String, String?, List<String>, List<Attachment>, List<ChecklistItem>) -> Unit,
    entryType: WritingType,
    initialContent: String = ""
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf(initialContent) }
    var mood by remember { mutableStateOf<String?>(null) }
    var tagsText by remember { mutableStateOf("") }
    val attachments = remember { mutableStateListOf<Attachment>() }
    val checklists = remember { mutableStateListOf<ChecklistItem>() }
    var newChecklistItemText by remember { mutableStateOf("") }
    var showFilePickerDialog by remember { mutableStateOf(false) }

    var showUnsupportedFileDialog by remember { mutableStateOf(false) }
    var unsupportedFileName by remember { mutableStateOf("") }
    
    // Enhanced file picker launcher with validation (persistable URIs)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Extract file name using improved method
            val fileName = FileUtils.getFileNameFromUri(context, it)
            
            // Check if file is supported
            if (FileTypeUtils.isFileSupported(fileName)) {
                // File is supported, add it
                val fileType = FileTypeUtils.getAttachmentType(fileName)
                // Try to persist read permission for later viewing
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { }
                attachments.add(Attachment(fileType, it.toString(), fileName))
            } else {
                // File is not supported, show error dialog
                // Include file extension in error message for debugging
                val extension = FileTypeUtils.getFileExtension(fileName)
                unsupportedFileName = "$fileName (extension: $extension)"
                showUnsupportedFileDialog = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New ${entryType.name.lowercase().replaceFirstChar { it.uppercase() }}") },
        text = {
            val scrollState = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(scrollState)
            ) {
                // Title Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Enter title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Note Section (Optional)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Note (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Enter your note content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 8
                        )
                    }
                }

                // Mood Section (for Journal entries only)
                if (entryType == WritingType.JOURNAL) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Mood (Optional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Mood selection buttons
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(MoodUtils.moods) { moodOption ->
                                    val isSelected = mood == moodOption.name
                                    Card(
                                        modifier = Modifier.clickable {
                                            mood = if (isSelected) null else moodOption.name
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                androidx.compose.ui.graphics.Color(moodOption.color).copy(alpha = 0.3f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (isSelected) 8.dp else 2.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                painter = painterResource(moodOption.iconRes),
                                                contentDescription = moodOption.name,
                                                tint = androidx.compose.ui.graphics.Color(moodOption.color),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = moodOption.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) {
                                                    androidx.compose.ui.graphics.Color(moodOption.color)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tags Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            label = { Text("Enter tags (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                    // Attachments Section - Only for Notes
                    if (entryType == WritingType.NOTE) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // File Attachment Button
                        Button(
                            onClick = { 
                                showFilePickerDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painterResource(R.drawable.ic_file), "Attach File", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browse Files")
                        }

                        // Display Attachments
                        if (attachments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Attached Files:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            attachments.forEachIndexed { index, attachment ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = when (attachment.type) {
                                                AttachmentType.IMAGE -> painterResource(R.drawable.ic_image)
                                                AttachmentType.AUDIO -> painterResource(R.drawable.ic_audio)
                                                AttachmentType.VIDEO -> painterResource(R.drawable.ic_video)
                                                AttachmentType.FILE -> painterResource(R.drawable.ic_file)
                                            },
                                            contentDescription = attachment.type.name,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = attachment.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = attachment.type.name.lowercase(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { attachments.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete), // Delete icon
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                    }

                // Checklists
                Text("Checklist:", style = MaterialTheme.typography.labelMedium)
                checklists.forEachIndexed { index, item ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { checklists[index] = item.copy(isCompleted = it) }
                        )
                        Text(item.text)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { checklists.removeAt(index) }) {
                            Icon(painterResource(R.drawable.ic_delete), "Remove checklist item")
                        }
                    }
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newChecklistItemText,
                        onValueChange = { newChecklistItemText = it },
                        label = { Text("New checklist item") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newChecklistItemText.isNotBlank()) {
                                checklists.add(ChecklistItem(newChecklistItemText, false))
                                newChecklistItemText = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onAddEntry(
                            title,
                            content,
                            mood,
                            tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            attachments,
                            checklists
                        )
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Add Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // File Picker Dialog
    if (showFilePickerDialog) {
        AlertDialog(
            onDismissRequest = { showFilePickerDialog = false },
            title = { Text("Select File Type") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Choose the type of file you want to attach:")
                    Text(
                        text = "Supported: Images, Audio, Video, and Documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Image picker button
                    Button(
                        onClick = { 
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getImageMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_image), "Image", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Image")
                    }
                    
                    // Audio picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getAudioMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_audio), "Audio", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Audio")
                    }
                    
                    // Video picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getVideoMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_video), "Video", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Video")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Document picker button
                    Button(
                        onClick = { 
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getDocumentMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_file), "Document", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Document")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // General file picker button
                    Button(
                        onClick = { 
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_file), "File", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Any Supported File")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Unsupported File Error Dialog
    if (showUnsupportedFileDialog) {
        AlertDialog(
            onDismissRequest = { showUnsupportedFileDialog = false },
            title = { 
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unsupported File Type",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "The file \"$unsupportedFileName\" is not supported by PathX.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📁 Supported File Types:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = FileTypeUtils.getSupportedFormatsMessage(),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    Text(
                        text = "💡 Tip: Convert your file to a supported format or use a different file type.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showUnsupportedFileDialog = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun EditWritingEntryDialog(
    entry: WritingEntry,
    onDismiss: () -> Unit,
    onUpdateEntry: (WritingEntry) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(entry.title) }
    var content by remember { mutableStateOf(entry.content) }
    var mood by remember { mutableStateOf(entry.mood ?: "") }
    var tagsText by remember { mutableStateOf(entry.tags.joinToString(", ")) }
    val attachments = remember { mutableStateListOf(*entry.attachments.toTypedArray()) }
    val checklists = remember { mutableStateListOf(*entry.checklists.toTypedArray()) }
    var newChecklistItemText by remember { mutableStateOf("") }
    var showFilePickerDialog by remember { mutableStateOf(false) }
    var showUnsupportedFileDialog by remember { mutableStateOf(false) }
    var unsupportedFileName by remember { mutableStateOf("") }
    
    // Enhanced file picker launcher with validation for editing (persistable URIs)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Extract file name using improved method
            val fileName = FileUtils.getFileNameFromUri(context, it)
            
            // Check if file is supported
            if (FileTypeUtils.isFileSupported(fileName)) {
                // File is supported, add it
                val fileType = FileTypeUtils.getAttachmentType(fileName)
                // Try to persist read permission for later viewing
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { }
                attachments.add(Attachment(fileType, it.toString(), fileName))
            } else {
                // File is not supported, show error dialog
                // Include file extension in error message for debugging
                val extension = FileTypeUtils.getFileExtension(fileName)
                unsupportedFileName = "$fileName (extension: $extension)"
                showUnsupportedFileDialog = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${if (entry.type == WritingType.JOURNAL) "Journal Entry" else "Note"}") },
        text = {
            val scrollState = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )

                // Dictionary search panel for Notes editing
                if (entry.type == WritingType.NOTE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DictionaryLookupPanel(
                        onAddToNote = { insertion ->
                            val newText = if (content.isBlank()) insertion else content + "\n" + insertion
                            content = newText
                        }
                    )
                }

                // Mood Section (for Journal entries only)
                if (entry.type == WritingType.JOURNAL) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Mood (Optional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Mood selection buttons
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(MoodUtils.moods) { moodOption ->
                                    val isSelected = mood == moodOption.name
                                    Card(
                                        modifier = Modifier.clickable {
                                            mood = if (isSelected) "" else moodOption.name
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                androidx.compose.ui.graphics.Color(moodOption.color).copy(alpha = 0.3f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (isSelected) 8.dp else 2.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                painter = painterResource(moodOption.iconRes),
                                                contentDescription = moodOption.name,
                                                tint = androidx.compose.ui.graphics.Color(moodOption.color),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = moodOption.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) {
                                                    androidx.compose.ui.graphics.Color(moodOption.color)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Attachment Management
                Text("Attachments:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                
                // Add Attachment Button
                Button(
                    onClick = { 
                        showFilePickerDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(painterResource(R.drawable.ic_file), "Attach File", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse Files to Attach")
                }

                // Display Attachments
                if (attachments.isNotEmpty()) {
                    attachments.forEachIndexed { index, attachment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = when (attachment.type) {
                                        AttachmentType.IMAGE -> painterResource(R.drawable.ic_image)
                                        AttachmentType.AUDIO -> painterResource(R.drawable.ic_audio)
                                        AttachmentType.VIDEO -> painterResource(R.drawable.ic_video)
                                        AttachmentType.FILE -> painterResource(R.drawable.ic_file)
                                    },
                                    contentDescription = attachment.type.name,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = attachment.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { attachments.removeAt(index) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        val updatedEntry = entry.copy(
                            title = title,
                            content = content,
                            mood = mood.ifBlank { null },
                            tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            attachments = attachments.toList(),
                            checklists = checklists.toList()
                        )
                        onUpdateEntry(updatedEntry)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Update Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // File Picker Dialog for editing
    if (showFilePickerDialog) {
        AlertDialog(
            onDismissRequest = { showFilePickerDialog = false },
            title = { Text("Select File to Attach") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Choose the type of file you want to attach:")
                    Text(
                        text = "Supported: Images, Audio, Video, and Documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Image picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getImageMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_image), "Image", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Image")
                    }

                    // Audio picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getAudioMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_audio), "Audio", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Audio")
                    }

                    // Video picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getVideoMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_video), "Video", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Video")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Document picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf(FileUtils.getDocumentMimeTypePattern()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_file), "Document", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Document")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // General file picker button
                    Button(
                        onClick = {
                            showFilePickerDialog = false
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_file), "File", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Any Supported File")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Unsupported File Error Dialog for editing
    if (showUnsupportedFileDialog) {
        AlertDialog(
            onDismissRequest = { showUnsupportedFileDialog = false },
            title = { 
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unsupported File Type",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "The file \"$unsupportedFileName\" is not supported by PathX.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📁 Supported File Types:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = FileTypeUtils.getSupportedFormatsMessage(),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    Text(
                        text = "💡 Tip: Convert your file to a supported format or use a different file type.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showUnsupportedFileDialog = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun ViewWritingEntryDialog(
    entry: WritingEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    var showAttachmentViewer by remember { mutableStateOf<Attachment?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = entry.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp), // Limit height for scrollability
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(entry.type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                
                // CONTENT CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_file),
                                contentDescription = "Content",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Note Content",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Content Text
                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        // Mood (for Journal entries)
                        if (entry.mood != null && entry.type == WritingType.JOURNAL) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val moodData = MoodUtils.getMoodByName(entry.mood)
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (moodData != null) {
                                            Icon(
                                                painter = painterResource(moodData.iconRes),
                                                contentDescription = moodData.name,
                                                tint = androidx.compose.ui.graphics.Color(moodData.color),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text("Mood: ${entry.mood}")
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (moodData != null) {
                                        androidx.compose.ui.graphics.Color(moodData.color).copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    }
                                )
                            )
                        }
                        
                        // Tags
                        if (entry.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                entry.tags.forEach { tag ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(tag) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Checklist
                        if (entry.checklists.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Checklist (${entry.checklists.count { it.isCompleted }}/${entry.checklists.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            entry.checklists.forEach { item ->
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (item.isCompleted) "✓" else "○",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                
                // ATTACHMENTS CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_file),
                                contentDescription = "Attachments",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attachments (${entry.attachments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Attachments List
                        if (entry.attachments.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                entry.attachments.forEach { attachment ->
                                    AttachmentViewCard(
                                        attachment = attachment,
                                        onClick = { showAttachmentViewer = attachment }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No attachments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
    
    // Document Reader Dialog
    if (showAttachmentViewer != null) {
        DocumentReaderDialog(
            attachment = showAttachmentViewer!!,
            onDismiss = { showAttachmentViewer = null }
        )
    }
}

@Composable
fun AttachmentViewCard(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // File type icon with colored background
            Card(
                modifier = Modifier.size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (attachment.type) {
                        AttachmentType.IMAGE -> MaterialTheme.colorScheme.primaryContainer
                        AttachmentType.AUDIO -> MaterialTheme.colorScheme.secondaryContainer
                        AttachmentType.VIDEO -> MaterialTheme.colorScheme.tertiaryContainer
                        AttachmentType.FILE -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        painter = when (attachment.type) {
                            AttachmentType.IMAGE -> painterResource(R.drawable.ic_image)
                            AttachmentType.AUDIO -> painterResource(R.drawable.ic_audio)
                            AttachmentType.VIDEO -> painterResource(R.drawable.ic_video)
                            AttachmentType.FILE -> painterResource(R.drawable.ic_file)
                        },
                        contentDescription = attachment.type.name,
                        modifier = Modifier.size(20.dp),
                        tint = when (attachment.type) {
                            AttachmentType.IMAGE -> MaterialTheme.colorScheme.primary
                            AttachmentType.AUDIO -> MaterialTheme.colorScheme.secondary
                            AttachmentType.VIDEO -> MaterialTheme.colorScheme.tertiary
                            AttachmentType.FILE -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // File information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FileTypeUtils.getFileCategoryDescription(FileTypeUtils.getFileExtension(attachment.name)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                attachment.size?.let { size ->
                    Text(
                        text = formatFileSize(size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // View button
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View File",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

@Composable
fun MoodTrackingBoard() {
    val writingEntries = DataManager.getWritingEntries()
    val journalEntries = writingEntries.filter { it.type == WritingType.JOURNAL && it.mood != null }
    
    // Get mood statistics
    val moodCounts = journalEntries.groupBy { it.mood }.mapValues { it.value.size }
    val totalEntries = journalEntries.size
    
    // State for graph dialog
    var showGraphDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mood Tracking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (totalEntries > 0) {
                    Button(
                        onClick = { showGraphDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Graph")
                    }
                }
            }
            
            if (totalEntries > 0) {
                Text(
                    text = "Your mood patterns from $totalEntries journal entries",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Mood grid
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MoodUtils.moods) { mood ->
                        val count = moodCounts[mood.name] ?: 0
                        val percentage = if (totalEntries > 0) (count * 100) / totalEntries else 0
                        val moodColor = Color(mood.color)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(moodColor.copy(alpha = 0.2f))
                                    .border(
                                        width = 2.dp,
                                        color = moodColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getMoodEmoji(mood.name),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            
                            Text(
                                text = mood.name,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = moodColor
                            )
                            
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Mood insights
                if (moodCounts.isNotEmpty()) {
                    val mostCommonMood = moodCounts.maxByOrNull { it.value }?.key
                    val leastCommonMood = moodCounts.minByOrNull { it.value }?.key
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Mood Insights",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (mostCommonMood != null) {
                                val mostCommonMoodObj = MoodUtils.moods.find { it.name == mostCommonMood }
                                val mostCommonColor = if (mostCommonMoodObj != null) Color(mostCommonMoodObj.color) else MaterialTheme.colorScheme.onSurface
                                Text(
                                    text = "Most common: ${getMoodEmoji(mostCommonMood)} $mostCommonMood (${moodCounts[mostCommonMood]} entries)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = mostCommonColor
                                )
                            }
                            
                            if (leastCommonMood != null && moodCounts[leastCommonMood] != moodCounts[mostCommonMood]) {
                                val leastCommonMoodObj = MoodUtils.moods.find { it.name == leastCommonMood }
                                val leastCommonColor = if (leastCommonMoodObj != null) Color(leastCommonMoodObj.color) else MaterialTheme.colorScheme.onSurface
                                Text(
                                    text = "Least common: ${getMoodEmoji(leastCommonMood)} $leastCommonMood (${moodCounts[leastCommonMood]} entries)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = leastCommonColor
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Start journaling to track your mood patterns!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Graph Dialog
    if (showGraphDialog) {
        MoodGraphDialog(
            onDismiss = { showGraphDialog = false },
            journalEntries = journalEntries
        )
    }
}

private fun getMoodEmoji(moodName: String): String {
    return when (moodName.lowercase()) {
        "happy" -> "😊"
        "excited" -> "🤩"
        "neutral" -> "😐"
        "sad" -> "😢"
        "anxious" -> "😰"
        "calm" -> "😌"
        "angry" -> "😤"
        "tired" -> "😴"
        "confused" -> "🤔"
        "loved" -> "😍"
        else -> "😊"
    }
}

@Composable
fun MoodGraphDialog(
    onDismiss: () -> Unit,
    journalEntries: List<WritingEntry>
) {
    var selectedPeriod by remember { mutableStateOf("Weekly") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Mood Patterns Graph",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedPeriod = "Weekly" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == "Weekly") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text("Weekly")
                    }
                    
                    Button(
                        onClick = { selectedPeriod = "Monthly" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == "Monthly") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text("Monthly")
                    }
                }
                
                // Graph content
                MoodCircleGraph(
                    journalEntries = journalEntries,
                    period = selectedPeriod
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun MoodCircleGraph(
    journalEntries: List<WritingEntry>,
    period: String
) {
    val now = LocalDateTime.now()
    val filteredEntries = when (period) {
        "Weekly" -> {
            val weekAgo = now.minusDays(7)
            journalEntries.filter { it.createdAt.isAfter(weekAgo) }
        }
        "Monthly" -> {
            val monthAgo = now.minusDays(30)
            journalEntries.filter { it.createdAt.isAfter(monthAgo) }
        }
        else -> journalEntries
    }
    
    val moodCounts = filteredEntries.groupBy { it.mood }.mapValues { it.value.size }
    val totalEntries = filteredEntries.size
    val sortedMoods = moodCounts.toList().sortedByDescending { it.second }
    
    if (totalEntries > 0) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$period Mood Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Based on $totalEntries entries",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Circle graph representation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Draw pie chart segments
                var currentAngle = 0f
                
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = minOf(size.width, size.height) / 2 - 20
                    
                    sortedMoods.forEach { (moodName, count) ->
                        val moodObj = MoodUtils.moods.find { it.name == moodName }
                        val moodColor = if (moodObj != null) Color(moodObj.color) else Color.Gray
                        val sweepAngle = (count.toFloat() / totalEntries) * 360f
                        
                        drawArc(
                            color = moodColor,
                            startAngle = currentAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        
                        currentAngle += sweepAngle
                    }
                }
                
                // Center text
                Text(
                    text = "$totalEntries\nEntries",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Legend:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                sortedMoods.forEach { (moodName, count) ->
                    val moodObj = MoodUtils.moods.find { it.name == moodName }
                    val moodColor = if (moodObj != null) Color(moodObj.color) else Color.Gray
                    val percentage = (count * 100) / totalEntries
                    val safeMoodName = moodName ?: "Unknown"
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(moodColor, CircleShape)
                        )
                        
                        Text(
                            text = "${getMoodEmoji(safeMoodName)} $safeMoodName: $count ($percentage%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = "No mood data available for the selected period",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

