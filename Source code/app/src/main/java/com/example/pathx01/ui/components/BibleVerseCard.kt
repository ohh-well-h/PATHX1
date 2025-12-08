package com.example.pathx01.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pathx01.R
import com.example.pathx01.data.model.BibleVerse
import com.example.pathx01.data.model.BibleVerseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleVerseCard(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {}
) {
    var currentVerse by remember { mutableStateOf(BibleVerseRepository.getVerseOfTheDay()) }
    var showFullVerse by remember { mutableStateOf(false) }
    
    // Beautiful gradient colors - theme conditional
    // Check if we're in dark theme by examining the surface color luminance
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDarkTheme = (surfaceColor.red + surfaceColor.green + surfaceColor.blue) / 3f < 0.5f
    
    val gradientColors = if (isDarkTheme) {
        // Dark theme - use original orange/blue/green gradient
        listOf(
            Color(0xFFFF9800).copy(alpha = 0.3f), // Orange
            Color(0xFF2196F3).copy(alpha = 0.3f), // Blue
            Color(0xFF4CAF50).copy(alpha = 0.3f)  // Green
        )
    } else {
        // Light theme - beautiful soft gradient colors
        listOf(
            Color(0xFFE8F5E8).copy(alpha = 0.8f), // Soft sage green
            Color(0xFFF3E5F5).copy(alpha = 0.8f), // Soft lavender
            Color(0xFFE3F2FD).copy(alpha = 0.8f)  // Soft sky blue
        )
    }
    
    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    // Pulse animation for the verse reference
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Gradient animation
    val gradientProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showFullVerse = true }
            .graphicsLayer {
                translationY = floatingOffset
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(
                            x = gradientProgress * 100f,
                            y = 0f
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            x = 200f + gradientProgress * 100f,
                            y = 200f
                        )
                    )
                )
                .clip(RoundedCornerShape(20.dp))
        ) {
            // Animated particles
            repeat(3) { index ->
                val particleOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000 + index * 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "particle$index"
                )
                
                val particleAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000 + index * 500, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "particleAlpha$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            translationX = 50f + index * 30f + particleOffset * 0.5f
                            translationY = 80f + index * 40f + particleOffset * 0.3f
                            alpha = particleAlpha
                        }
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with refresh button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cross),
                            contentDescription = "Cross",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bible Verse",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            currentVerse = BibleVerseRepository.getRandomVerse()
                            onRefresh()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = "New Verse",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Verse reference with pulse animation
                Text(
                    text = currentVerse.reference,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(pulseAlpha)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Book name
                Text(
                    text = currentVerse.book,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tap hint
                Text(
                    text = "Tap to read full verse",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    // Full verse dialog
    if (showFullVerse) {
        BibleVerseDialog(
            verse = currentVerse,
            onDismiss = { showFullVerse = false }
        )
    }
}

@Composable
fun BibleVerseDialog(
    verse: BibleVerse,
    onDismiss: () -> Unit
) {
    var selectedBackground by remember { mutableStateOf(0) }
    
    // Beautiful gradient backgrounds
    val gradientOptions = listOf(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        ),
        listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        ),
        listOf(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )
    
    val selectedGradient = gradientOptions[selectedBackground % gradientOptions.size]
    
    // Animated entrance
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Beautiful animated gradient background with darker overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = selectedGradient
                            )
                        )
                )
                
                // Dark overlay to make background darker
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cross),
                                contentDescription = "Cross",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Bible Verse of the Day",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_x),
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Verse reference
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(800, easing = EaseOutQuart)
                        ) + fadeIn(animationSpec = tween(800))
                    ) {
                        Text(
                            text = verse.reference,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Full verse text
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(1000, delayMillis = 200, easing = EaseOutQuart)
                        ) + fadeIn(animationSpec = tween(1000, delayMillis = 200))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = "\"${verse.text}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier = Modifier.padding(24.dp),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Book and chapter info
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            animationSpec = tween(1200, delayMillis = 400)
                        )
                    ) {
                        Text(
                            text = "${verse.book} ${verse.chapter}:${verse.verse}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Background slider selector
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(800, delayMillis = 600)
                        ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Background Theme",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Slider for background selection
                            Slider(
                                value = selectedBackground.toFloat(),
                                onValueChange = { newValue ->
                                    selectedBackground = newValue.toInt().coerceIn(0, gradientOptions.size - 1)
                                },
                                valueRange = 0f..(gradientOptions.size - 1).toFloat(),
                                steps = gradientOptions.size - 2,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                            
                            // Show current background preview
                            Card(
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(top = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = gradientOptions[selectedBackground]
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
