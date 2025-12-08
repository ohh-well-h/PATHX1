package com.example.pathx01.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pathx01.R
import com.example.pathx01.ui.screens.*
import com.example.pathx01.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathXBottomNavigation(themeManager: ThemeManager, onThemeChanged: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var filterState by remember { mutableStateOf<String?>(null) }

    val bottomNavItems = listOf(
        BottomNavItem("dashboard", "Home", painterResource(R.drawable.ic_home)),
        BottomNavItem("planner", "Planner", painterResource(R.drawable.ic_list)),
        BottomNavItem("projects", "Projects", painterResource(R.drawable.ic_star)),
        BottomNavItem("reading", "Reading", painterResource(R.drawable.ic_book)),
        BottomNavItem("writing", "Writing", painterResource(R.drawable.ic_edit))
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(item.icon, contentDescription = item.title)
                        },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            if (item.route == "dashboard") {
                                filterState = null // Clear filter when going back to dashboard
                                // Clear the entire navigation stack and go to dashboard
                                navController.navigate(item.route) {
                                    popUpTo(0) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    themeManager = themeManager,
                    onThemeChanged = onThemeChanged,
                    onNavigateToPlanner = { filter ->
                        filterState = filter
                        navController.navigate("planner")
                    },
                    onNavigateToProjects = {
                        navController.navigate("projects")
                    }
                )
            }
            composable("planner") {
                PlannerScreen(
                    filter = filterState,
                    onNavigateBack = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("projects") {
                ProjectsScreen()
            }
            composable("reading") {
                ReadingScreen()
            }
            composable("writing") {
                WritingScreen()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: Painter
)
