package com.example.pathx01.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pathx01.R
import com.example.pathx01.utils.UserPreferencesManager
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    
    var userName by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }
    var showHebronSpecialDialog by remember { mutableStateOf(false) }
    var showHebronMamaDialog by remember { mutableStateOf(false) }
    var showHebronCrownDialog by remember { mutableStateOf(false) }
    
    // Calvin special dialogs
    var showCalvinSpecialDialog by remember { mutableStateOf(false) }
    var showCalvinMeanDialog by remember { mutableStateOf(false) }
    var showCalvinRaceCarDialog by remember { mutableStateOf(false) }
    
    // Animation for content appearance
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    // Beautiful gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    
    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Welcome content
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(1000, easing = EaseOutQuart)
                ) + fadeIn(animationSpec = tween(1000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = floatingOffset
                        }
                ) {
                    // App icon/logo area
                    Card(
                        modifier = Modifier.size(120.dp),
                        shape = RoundedCornerShape(60.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cross),
                                contentDescription = "PathX Logo",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Welcome text
                    Text(
                        text = "Welcome to PathX",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Your Personal Productivity Companion",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Name input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "What's your name?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it.trim() },
                                placeholder = {
                                    Text(
                                        text = "Enter your name",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    if (userName.isNotBlank()) {
                                        // Set original username for special user privileges (first time only)
                                        userPreferencesManager.setOriginalUserName(userName)
                                        userPreferencesManager.updateUserName(userName)
                                        
                                        // Check for special users
                                        when {
                                            userName.equals("Hebron", ignoreCase = true) -> {
                                                showHebronSpecialDialog = true
                                            }
                                            userName.equals("Calvin", ignoreCase = true) -> {
                                                showCalvinSpecialDialog = true
                                            }
                                            else -> {
                                                onComplete()
                                            }
                                        }
                                    }
                                },
                                enabled = userName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Get Started",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Calvin's message at bottom
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(1500, delayMillis = 800)
                )
            ) {
                Text(
                    text = "Yes Calvin i made this for ur ahh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(0.7f)
                        .padding(16.dp)
                )
            }
        }
        
        // Hebron Special Dialog
        if (showHebronSpecialDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showHebronSpecialDialog = false
                    showHebronMamaDialog = true
                },
                title = {
                    Text(
                        text = "Special User Detected! 🎉",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "OOOWWW , we have a special user",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showHebronSpecialDialog = false
                        showHebronMamaDialog = true
                    }) {
                        Text("Continue")
                    }
                }
            )
        }
        
        // Hebron Mama Dialog
        if (showHebronMamaDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showHebronMamaDialog = false
                    showHebronCrownDialog = true
                },
                title = {
                    Text(
                        text = "Welcome! ☺️",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "HEY MAMA ☺️",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showHebronMamaDialog = false
                        showHebronCrownDialog = true
                    }) {
                        Text("Continue")
                    }
                }
            )
        }
        
        // Hebron Crown Dialog
        if (showHebronCrownDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showHebronCrownDialog = false
                    onComplete()
                },
                title = {
                    Text(
                        text = "Here is your crown Your Honor",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "You are royalty in our app!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Crown icon at bottom center
                        Icon(
                            painter = painterResource(R.drawable.ic_crown),
                            contentDescription = "Crown",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFFFD700) // Gold color
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showHebronCrownDialog = false
                        onComplete()
                    }) {
                        Text("Claim My Crown")
                    }
                }
            )
        }
    }
    
    // Calvin Special Dialog Sequence
    if (showCalvinSpecialDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Calvin!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Calvin, we could be world champions Calvin, Calviinnn!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Racing car icon
                    Icon(
                        painter = painterResource(R.drawable.ic_racing_car),
                        contentDescription = "Racing Car",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFD54F) // Yellow sports car color
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showCalvinSpecialDialog = false
                    showCalvinMeanDialog = true
                }) {
                    Text("What?")
                }
            }
        )
    }
    
    if (showCalvinMeanDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Oops...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Anyways no welcome sht for you",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    showCalvinMeanDialog = false
                    showCalvinRaceCarDialog = true
                }) {
                    Text("Excuse me?")
                }
            }
        )
    }
    
    if (showCalvinRaceCarDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Just Kidding!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "I am joking, because I feel nice, here is ur race car",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // F1 Racing car icon at bottom center
                    Icon(
                        painter = painterResource(R.drawable.ic_racing_car),
                        contentDescription = "F1 Racing Car",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFFFD54F) // Yellow sports car color
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showCalvinRaceCarDialog = false
                    onComplete()
                }) {
                    Text("Let's Race!")
                }
            }
        )
    }
}
