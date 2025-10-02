package com.fnt.notiglyph.ui.navigation

/**
 * Navigation destinations for the app
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object PatternEditor : Screen("pattern_editor/{patternId}") {
        fun createRoute(patternId: Long = 0L) = "pattern_editor/$patternId"
    }
    object PatternLibrary : Screen("pattern_library")
    object History : Screen("history")
    object Settings : Screen("settings")
}
