package com.example.pathx01

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pathx01.data.DataManager
import com.example.pathx01.navigation.PathXBottomNavigation
import com.example.pathx01.notifications.NotificationService
import com.example.pathx01.ui.screens.WelcomeScreen
import com.example.pathx01.ui.theme.PathXTheme
import com.example.pathx01.utils.UserPreferencesManager
import com.example.pathx01.utils.ThemeManager
import android.util.Log

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, initialize notification system
            NotificationService.requestNotificationPermission(this)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize DataManager with context
        DataManager.initialize(this)
        
        // Request notification permission and initialize notification system
        requestNotificationPermission()
        
        // Add lifecycle observer to save data when app goes to background
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d("MainActivity", "App paused - saving all data")
                        DataManager.immediateSave()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        Log.d("MainActivity", "App stopped - saving all data")
                        DataManager.immediateSave()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        Log.d("MainActivity", "Activity destroying - final save")
                        DataManager.forceSaveAll()
                        DataManager.cleanup()
                    }
                    else -> {}
                }
            }
        })
        
        setContent {
            val themeManager = remember { ThemeManager(this) }
            var isDarkTheme by remember { mutableStateOf(themeManager.isDarkTheme()) }
            
            PathXTheme(darkTheme = isDarkTheme) {
                AppContent(themeManager) {
                    isDarkTheme = themeManager.isDarkTheme()
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission already granted
                        NotificationService.requestNotificationPermission(this)
                    }
                    else -> {
                        // Request permission
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            else -> {
                // Permission not needed for older Android versions
                NotificationService.requestNotificationPermission(this)
            }
        }
    }
}

@Composable
fun AppContent(themeManager: ThemeManager, onThemeChanged: () -> Unit) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    var showWelcome by remember { mutableStateOf(userPreferencesManager.isFirstLaunch()) }
    var showExampleDataDialog by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }
    var lastPulse by remember { mutableStateOf(0) }
    
    // Initialize notification system when app starts
    LaunchedEffect(Unit) {
        if (!showWelcome) {
            NotificationService.initialize(context)
            // Show example data dialog if there's data present and user hasn't seen it before
            if (DataManager.hasData() && !userPreferencesManager.hasSeenExampleDataDialog()) {
                showExampleDataDialog = true
                userPreferencesManager.setExampleDataDialogSeen()
            }
        }
    }
    
    if (showWelcome) {
        WelcomeScreen(
            onComplete = {
                showWelcome = false
                // First launch is automatically set to false in updateUserName
                // Initialize notification system after welcome screen is completed
                NotificationService.initialize(context)
                // Show example data dialog if there's data present and user hasn't seen it before
                if (DataManager.hasData() && !userPreferencesManager.hasSeenExampleDataDialog()) {
                    showExampleDataDialog = true
                    userPreferencesManager.setExampleDataDialogSeen()
                }
            }
        )
    } else {
        PathXBottomNavigation(themeManager, onThemeChanged)

        // Saved indicator: flash when DataManager.savePulse increments
        val pulse = DataManager.savePulse.value
        LaunchedEffect(pulse) {
            if (pulse != lastPulse) {
                lastPulse = pulse
                showSaved = true
                kotlinx.coroutines.delay(1500)
                showSaved = false
            }
        }
        
        // Example Data Explanation Dialog
        if (showExampleDataDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showExampleDataDialog = false },
                title = {
                    androidx.compose.material3.Text(
                        text = "Example Data",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    androidx.compose.material3.Text(
                        text = "The data you see in the app is example data to help you understand how PathX works. You can clear all example data or reload it using the dropdown menu in the top-right corner of the home screen."
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showExampleDataDialog = false }
                    ) {
                        androidx.compose.material3.Text("Got it!")
                    }
                }
            )
        }


        // Saved toast-like chip
        androidx.compose.animation.AnimatedVisibility(visible = showSaved) {
            androidx.compose.material3.Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = androidx.compose.material3.MaterialTheme.shapes.small
            ) {
                androidx.compose.material3.Text(
                    modifier = androidx.compose.ui.Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    text = "Saved",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}