package com.fnt.notiglyph.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fnt.notiglyph.domain.model.IconType
import com.fnt.notiglyph.domain.model.NotificationPattern
import com.fnt.notiglyph.domain.model.PatternType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternLibraryScreen(
    onNavigateBack: () -> Unit,
    onInstallPattern: (NotificationPattern) -> Unit
) {
    val samplePatterns = remember { getSamplePatterns() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pattern Library") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Popular Patterns",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(samplePatterns) { pattern ->
                LibraryPatternCard(
                    pattern = pattern,
                    onInstall = { onInstallPattern(pattern) }
                )
            }
        }
    }
}

@Composable
fun LibraryPatternCard(
    pattern: NotificationPattern,
    onInstall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.appDisplayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pattern.appPackageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onInstall) {
                    Icon(Icons.Default.Add, contentDescription = "Install")
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pattern:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pattern.patternString,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Display:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pattern.displayTemplate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AssistChip(
                onClick = { },
                label = { Text(pattern.patternType.name) }
            )
        }
    }
}

private fun getSamplePatterns(): List<NotificationPattern> {
    return listOf(
        NotificationPattern(
            appPackageName = "com.fnt.notiglyph",
            appDisplayName = "NotiGlyph",
            patternType = PatternType.KEYWORD,
            patternString = "Example notification",
            extractedVariables = emptyList(),
            displayTemplate = "✅ Test works!",
            priority = 5,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 5,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        ),
        NotificationPattern(
            appPackageName = "com.ubercab.eats",
            appDisplayName = "Uber Eats",
            patternType = PatternType.TEMPLATE,
            patternString = "arriving in {minutes} min",
            extractedVariables = listOf("minutes"),
            displayTemplate = "🍔 {minutes}m",
            priority = 8,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 30,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        ),
        NotificationPattern(
            appPackageName = "com.amazon.mShop.android.shopping",
            appDisplayName = "Amazon",
            patternType = PatternType.KEYWORD,
            patternString = "out for delivery",
            extractedVariables = emptyList(),
            displayTemplate = "📦 Arriving today",
            priority = 7,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 30,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        ),
        NotificationPattern(
            appPackageName = "com.ubercab",
            appDisplayName = "Uber",
            patternType = PatternType.TEMPLATE,
            patternString = "{driver} is {distance} away",
            extractedVariables = listOf("driver", "distance"),
            displayTemplate = "🚗 {distance}",
            priority = 9,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 30,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        ),
        NotificationPattern(
            appPackageName = "com.doordash.drive.customer",
            appDisplayName = "DoorDash",
            patternType = PatternType.TEMPLATE,
            patternString = "arriving in {minutes} minutes",
            extractedVariables = listOf("minutes"),
            displayTemplate = "🍕 {minutes}min",
            priority = 8,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 30,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        ),
        NotificationPattern(
            appPackageName = "com.grubhub.android",
            appDisplayName = "Grubhub",
            patternType = PatternType.KEYWORD,
            patternString = "delivered OR arrived",
            extractedVariables = emptyList(),
            displayTemplate = "🥡 Delivered!",
            priority = 7,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 30,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        ),
        NotificationPattern(
            appPackageName = "com.whatsapp",
            appDisplayName = "WhatsApp",
            patternType = PatternType.TEMPLATE,
            patternString = "{sender}: {message}",
            extractedVariables = listOf("sender", "message"),
            displayTemplate = "💬 {sender}",
            priority = 6,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 20,
            delaySeconds = 0,
            createdAt = System.currentTimeMillis()
        )
    )
}
