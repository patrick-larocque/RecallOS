package com.patricklarocque.recallos.core.navigation

sealed class RecallDestination(
    val route: String,
    val label: String,
) {
    data object Home : RecallDestination(route = "home", label = "Home")
    data object Capture : RecallDestination(route = "capture", label = "Capture")
    data object Search : RecallDestination(route = "search", label = "Search")
    data object Spaces : RecallDestination(route = "spaces", label = "Spaces")
    data object Settings : RecallDestination(route = "settings", label = "Settings")

    companion object {
        val topLevelDestinations = listOf(Home, Search, Spaces, Settings)
    }
}
