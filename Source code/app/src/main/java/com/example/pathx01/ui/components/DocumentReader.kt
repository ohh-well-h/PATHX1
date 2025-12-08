package com.example.pathx01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pathx01.R
import com.example.pathx01.data.model.Attachment
import com.example.pathx01.data.model.AttachmentType
import com.example.pathx01.utils.FileTypeUtils
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.math.max
import kotlin.math.min

@Composable
fun DocumentReaderDialog(
    attachment: Attachment,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                DocumentReaderHeader(
                    attachment = attachment,
                    onDismiss = onDismiss
                )
                
                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (attachment.type) {
                        AttachmentType.IMAGE -> ImageViewer(attachment = attachment)
                        AttachmentType.AUDIO -> AudioPlayer(attachment = attachment)
                        AttachmentType.VIDEO -> VideoPlayer(attachment = attachment)
                        AttachmentType.FILE -> FileViewer(attachment = attachment)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentReaderHeader(
    attachment: Attachment,
    onDismiss: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Add share functionality */ }) {
                Icon(painterResource(R.drawable.ic_share), contentDescription = "Share")
            }
            IconButton(onClick = { /* TODO: Add download functionality */ }) {
                Icon(painterResource(R.drawable.ic_download), contentDescription = "Download")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun ImageViewer(attachment: Attachment) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        offset += offsetChange
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .transformable(state = transformableState)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(attachment.path)
                .crossfade(true)
                .build(),
            contentDescription = attachment.name,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
        
        // Image controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { scale = (scale * 0.8f).coerceIn(0.5f, 3f) }
                    ) {
                        Icon(painterResource(R.drawable.ic_zoom_out), contentDescription = "Zoom Out")
                    }
                    
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    IconButton(
                        onClick = { scale = (scale * 1.25f).coerceIn(0.5f, 3f) }
                    ) {
                        Icon(painterResource(R.drawable.ic_zoom_in), contentDescription = "Zoom In")
                    }
                    
                    IconButton(
                        onClick = { 
                            scale = 1f
                            offset = Offset.Zero
                        }
                    ) {
                        Icon(painterResource(R.drawable.ic_fit_screen), contentDescription = "Fit to Screen")
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayer(attachment: Attachment) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0f) }
    var totalDuration by remember { mutableStateOf(100f) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_audio),
                contentDescription = "Audio File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Slider
            Column {
                Slider(
                    value = currentTime,
                    onValueChange = { currentTime = it },
                    valueRange = 0f..totalDuration,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentTime.toInt()),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatTime(totalDuration.toInt()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Previous track */ }
                ) {
                    Icon(painterResource(R.drawable.ic_skip_previous), contentDescription = "Previous")
                }
                
                FloatingActionButton(
                    onClick = { isPlaying = !isPlaying },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.ic_pause) else painterResource(R.drawable.ic_play_arrow),
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
                
                IconButton(
                    onClick = { /* Next track */ }
                ) {
                    Icon(painterResource(R.drawable.ic_skip_next), contentDescription = "Next")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🎵 Built-in Audio Player\n(In a real app, this would connect to MediaPlayer)",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VideoPlayer(attachment: Attachment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_video),
                contentDescription = "Video File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Video placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { /* TODO: Implement video playback */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painterResource(R.drawable.ic_play_arrow),
                        contentDescription = "Play Video",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to play video",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🎬 Built-in Video Player\n(In a real app, this would use ExoPlayer or VideoView)",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileViewer(attachment: Attachment) {
    val fileExtension = FileTypeUtils.getFileExtension(attachment.name)
    
    when {
        fileExtension in FileTypeUtils.SUPPORTED_IMAGE_EXTENSIONS -> ImageViewer(attachment = attachment)
        fileExtension == "txt" -> TextFileViewer(attachment = attachment)
        fileExtension == "pdf" -> PDFViewer(attachment = attachment)
        fileExtension in setOf("doc", "docx") -> DocumentViewer(attachment = attachment)
        else -> GenericFileViewer(attachment = attachment)
    }
}

@Composable
fun TextFileViewer(attachment: Attachment) {
    var fileContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val fileExtension = FileTypeUtils.getFileExtension(attachment.name)
    val context = LocalContext.current
    
    LaunchedEffect(attachment.path) {
        try {
            // First try to read from URI (for actual uploaded files)
            if (attachment.path.startsWith("content://")) {
                try {
                    val uri = Uri.parse(attachment.path)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.use { stream ->
                        fileContent = stream.bufferedReader().use { it.readText() }
                    }
                    isLoading = false
                    return@LaunchedEffect
                } catch (e: Exception) {
                    // If URI reading fails, fall back to sample content
                }
            }
            
            // Handle different file types with sample content
            when (fileExtension) {
                "json" -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "{\n  \"sample\": \"JSON content\",\n  \"file\": \"${attachment.name}\",\n  \"type\": \"json\"\n}"
                    }
                }
                "xml" -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n  <sample>XML content</sample>\n  <file>${attachment.name}</file>\n  <type>xml</type>\n</root>"
                    }
                }
                "csv" -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "Name,Age,City\nJohn,25,New York\nJane,30,Los Angeles\nBob,35,Chicago"
                    }
                }
                "html" -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "<!DOCTYPE html>\n<html>\n<head><title>Sample HTML</title></head>\n<body>\n  <h1>Sample HTML Content</h1>\n  <p>File: ${attachment.name}</p>\n</body>\n</html>"
                    }
                }
                "css" -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "body {\n  font-family: Arial, sans-serif;\n  margin: 0;\n  padding: 20px;\n}\n\nh1 {\n  color: #333;\n  text-align: center;\n}"
                    }
                }
                "js", "kt", "java" -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "// Sample ${fileExtension.uppercase()} file\nfunction sampleFunction() {\n  console.log('Hello from ${attachment.name}');\n  return true;\n}\n\n// File: ${attachment.name}\n// Type: ${fileExtension}"
                    }
                }
                else -> {
                    val file = File(attachment.path)
                    if (file.exists()) {
                        fileContent = file.readText()
                    } else {
                        fileContent = "Sample text content for demonstration:\n\n" +
                                "This is a sample text file content.\n" +
                                "In a real app, this would load the actual file content.\n\n" +
                                "File: ${attachment.name}\n" +
                                "Type: ${fileExtension.uppercase()}\n" +
                                "Path: ${attachment.path}\n\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris."
                    }
                }
            }
            isLoading = false
        } catch (e: Exception) {
            fileContent = "Error reading file: ${e.message}\n\nFile: ${attachment.name}\nType: ${fileExtension.uppercase()}\nPath: ${attachment.path}"
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📄 Text File Viewer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "File: ${attachment.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Type: ${fileExtension.uppercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = fileContent,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = if (fileExtension == "json" || fileExtension == "xml") {
                            FontFamily.Monospace
                        } else {
                            FontFamily.Default
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentViewer(attachment: Attachment) {
    val fileExtension = FileTypeUtils.getFileExtension(attachment.name)
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = "Document File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = FileTypeUtils.getFileCategoryDescription(fileExtension),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "📄 Document Viewer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (fileExtension) {
                "doc", "docx" -> {
                    Text(
                        text = "Microsoft Word Document\n\n" +
                                "In a real app, you would integrate:\n" +
                                "• Apache POI for DOC/DOCX parsing\n" +
                                "• Rich text rendering\n" +
                                "• Document formatting preservation",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                "rtf" -> {
                    Text(
                        text = "Rich Text Format Document\n\n" +
                                "In a real app, you would integrate:\n" +
                                "• RTF parsing library\n" +
                                "• Rich text formatting display",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                "odt" -> {
                    Text(
                        text = "OpenDocument Text\n\n" +
                                "In a real app, you would integrate:\n" +
                                "• Apache POI for ODT parsing\n" +
                                "• OpenDocument format support",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "Document File\n\n" +
                                "In a real app, you would integrate:\n" +
                                "• Appropriate document parsing library\n" +
                                "• Format-specific rendering",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    try {
                        val uri = Uri.parse(attachment.path)
                        val mimeType = when (fileExtension) {
                            "doc" -> "application/msword"
                            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            else -> "*/*"
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle error - no document viewer available
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open with External Viewer")
            }
        }
    }
}

@Composable
fun SpreadsheetViewer(attachment: Attachment) {
    val fileExtension = FileTypeUtils.getFileExtension(attachment.name)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = "Spreadsheet File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Spreadsheet File (${fileExtension.uppercase()})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "📊 Spreadsheet Viewer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Microsoft Excel / LibreOffice Calc\n\n" +
                        "In a real app, you would integrate:\n" +
                        "• Apache POI for XLS/XLSX parsing\n" +
                        "• Table rendering with scrolling\n" +
                        "• Cell editing capabilities\n" +
                        "• Formula calculation display",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { /* TODO: Open with external spreadsheet viewer */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open with External Viewer")
            }
        }
    }
}

@Composable
fun PresentationViewer(attachment: Attachment) {
    val fileExtension = FileTypeUtils.getFileExtension(attachment.name)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = "Presentation File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Presentation File (${fileExtension.uppercase()})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "📈 Presentation Viewer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Microsoft PowerPoint / LibreOffice Impress\n\n" +
                        "In a real app, you would integrate:\n" +
                        "• Apache POI for PPT/PPTX parsing\n" +
                        "• Slide-by-slide navigation\n" +
                        "• Image and text rendering\n" +
                        "• Presentation mode",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { /* TODO: Open with external presentation viewer */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open with External Viewer")
            }
        }
    }
}

@Composable
fun PDFViewer(attachment: Attachment) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = "PDF File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "📄 PDF Viewer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "In a real app, you would integrate a PDF rendering library like:\n" +
                        "• AndroidPdfViewer\n" +
                        "• PdfiumAndroid\n" +
                        "• Or use WebView with PDF.js",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    try {
                        val uri = Uri.parse(attachment.path)
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle error - no PDF viewer available
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open with External PDF Viewer")
            }
        }
    }
}

@Composable
fun GenericFileViewer(attachment: Attachment) {
    val fileExtension = FileTypeUtils.getFileExtension(attachment.name)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = "File",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "File Type: ${fileExtension.uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            attachment.size?.let { size ->
                Text(
                    text = "Size: ${formatFileSize(size)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "This file type is not directly viewable in the app.\n" +
                        "You can download or open it with an external application.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Download file */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painterResource(R.drawable.ic_download), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download")
                }
                
                Button(
                    onClick = { /* TODO: Open with external app */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open External")
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}
