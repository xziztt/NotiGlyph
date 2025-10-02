package com.fnt.notiglyph.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fnt.notiglyph.domain.model.NotificationPattern
import com.fnt.notiglyph.ui.components.PatternListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    patterns: List<NotificationPattern>,
    onPatternClick: (Long) -> Unit,
    onPatternToggle: (Long, Boolean) -> Unit,
    onPatternDelete: (NotificationPattern) -> Unit,
    onAddPattern: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onStopGlyph: () -> Unit
) {
    Log.d("test","Loaded all patterns: $patterns")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NotiGlyph") },
                actions = {
                    IconButton(onClick = onStopGlyph) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop Glyph",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onNavigateToLibrary) {
                        Icon(Icons.Default.LibraryBooks, contentDescription = "Pattern Library")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPattern) {
                Icon(Icons.Default.Add, contentDescription = "Add Pattern")
            }
        }
    ) { paddingValues ->
        if (patterns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No patterns yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add a pattern or browse the library",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(patterns, key = { it.id }) { pattern ->
                    PatternListItem(
                        pattern = pattern,
                        onClick = { onPatternClick(pattern.id) },
                        onToggle = { enabled -> onPatternToggle(pattern.id, enabled) },
                        onDelete = { onPatternDelete(pattern) }
                    )
                }
            }
        }
    }
}
