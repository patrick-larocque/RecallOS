package com.patricklarocque.recallos.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.patricklarocque.recallos.core.navigation.RecallDestination
import com.patricklarocque.recallos.feature.home.HomeScreen
import com.patricklarocque.recallos.feature.search.SearchScreen
import com.patricklarocque.recallos.feature.settings.SettingsScreen
import com.patricklarocque.recallos.feature.spaces.SpacesScreen

@Composable
fun RecallOsNavHost() {
    val navController = rememberNavController()
    val destinations = RecallDestination.topLevelDestinations
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == destination.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(
                                route = destination.route,
                                navOptions = navOptions {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                },
                            )
                        },
                        icon = {
                            Text(text = destination.label.first().toString())
                        },
                        label = {
                            Text(text = destination.label)
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = RecallDestination.Home.route,
        ) {
            composable(RecallDestination.Home.route) {
                HomeScreen(contentPadding = paddingValues)
            }
            composable(RecallDestination.Search.route) {
                SearchScreen(contentPadding = paddingValues)
            }
            composable(RecallDestination.Spaces.route) {
                SpacesScreen(contentPadding = paddingValues)
            }
            composable(RecallDestination.Settings.route) {
                SettingsScreen(contentPadding = paddingValues)
            }
        }
    }
}
