package com.fnt.notiglyph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.fnt.notiglyph.data.database.NotiGlyphDatabase
import com.fnt.notiglyph.data.repository.NotificationRepository
import com.fnt.notiglyph.data.repository.PatternRepository
import com.fnt.notiglyph.data.repository.SettingsRepository
import com.fnt.notiglyph.ui.navigation.NavGraph
import com.fnt.notiglyph.ui.theme.NotiGlyphTheme
import com.fnt.notiglyph.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repositories
        val database = NotiGlyphDatabase.getInstance(applicationContext)
        val patternRepository = PatternRepository(database.patternDao())
        val notificationRepository = NotificationRepository(database.notificationHistoryDao())
        val settingsRepository = SettingsRepository(database.settingsDao())

        // Initialize default settings
        lifecycleScope.launch {
            settingsRepository.initializeDefaultSettings()
        }

        // Create ViewModelFactory
        viewModelFactory = ViewModelFactory(
            patternRepository,
            notificationRepository,
            settingsRepository
        )

        setContent {
            NotiGlyphTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModelFactory = viewModelFactory
                    )
                }
            }
        }
    }
}