package com.fnt.notiglyph.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fnt.notiglyph.ui.screens.*
import com.fnt.notiglyph.ui.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModelFactory: ViewModelFactory
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        // Main Screen
        composable(Screen.Main.route) {
            val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
            val patterns by viewModel.patterns.collectAsState()

            MainScreen(
                patterns = patterns,
                onPatternClick = { patternId ->
                    navController.navigate(Screen.PatternEditor.createRoute(patternId))
                },
                onPatternToggle = { id, enabled ->
                    viewModel.togglePatternEnabled(id, enabled)
                },
                onPatternDelete = { pattern ->
                    viewModel.deletePattern(pattern)
                },
                onAddPattern = {
                    navController.navigate(Screen.PatternEditor.createRoute(0L))
                },
                onNavigateToLibrary = {
                    navController.navigate(Screen.PatternLibrary.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onStopGlyph = {
                    viewModel.stopGlyph()
                }
            )
        }

        // Pattern Editor Screen
        composable(
            route = Screen.PatternEditor.route,
            arguments = listOf(navArgument("patternId") { type = NavType.LongType })
        ) { backStackEntry ->
            val viewModel: PatternEditorViewModel = viewModel(factory = viewModelFactory)
            val patternId = backStackEntry.arguments?.getLong("patternId") ?: 0L
            val pattern by viewModel.pattern.collectAsState()
            val installedApps by viewModel.installedApps.collectAsState()
            val context = LocalContext.current

            viewModel.loadPattern(patternId)

            PatternEditorScreen(
                pattern = pattern,
                installedApps = installedApps,
                onLoadInstalledApps = { viewModel.loadInstalledApps(context) },
                onNavigateBack = { navController.popBackStack() },
                onSave = { viewModel.savePattern() },
                onTestGlyph = { displayText -> viewModel.testGlyph(context, displayText) },
                onUpdatePattern = { pkg, name, type, patternStr, display, priority, icon, duration ->
                    viewModel.updatePattern(
                        appPackageName = pkg,
                        appDisplayName = name,
                        patternType = type,
                        patternString = patternStr,
                        displayTemplate = display,
                        priority = priority,
                        iconType = icon,
                        displayDurationSeconds = duration
                    )
                }
            )
        }

        // Pattern Library Screen
        composable(Screen.PatternLibrary.route) {
            val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)

            PatternLibraryScreen(
                onNavigateBack = { navController.popBackStack() },
                onInstallPattern = { pattern ->
                    // Install pattern via repository
                    CoroutineScope(Dispatchers.Main).launch {
                        mainViewModel.installPattern(pattern)
                    }
                    navController.popBackStack()
                }
            )
        }

        // History Screen
        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
            val notifications by viewModel.notifications.collectAsState()
            val filterMatched by viewModel.filterMatched.collectAsState()

            HistoryScreen(
                notifications = notifications,
                filterMatched = filterMatched,
                onNavigateBack = { navController.popBackStack() },
                onFilterChange = { filter -> viewModel.setFilter(filter) },
                onClearHistory = { viewModel.clearHistory() }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val settings by viewModel.settings.collectAsState()

            SettingsScreen(
                settings = settings,
                onNavigateBack = { navController.popBackStack() },
                onUpdateRetentionDays = { days -> viewModel.updateRetentionDays(days) },
                onUpdateVoiceAlerts = { enabled -> viewModel.updateVoiceAlerts(enabled) }
            )
        }
    }
}
