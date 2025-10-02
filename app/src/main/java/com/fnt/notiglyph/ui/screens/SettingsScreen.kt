package com.fnt.notiglyph.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.fnt.notiglyph.R
import com.fnt.notiglyph.data.database.entity.AppSettingsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsEntity,
    onNavigateBack: () -> Unit,
    onUpdateRetentionDays: (Int) -> Unit,
    onUpdateVoiceAlerts: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Notification Access Section
            SettingsSection(title = "Permissions") {
                SettingsItem(
                    title = "Notification Access",
                    description = "Grant permission to read notifications",
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    }
                )

                // Battery Optimization Setting
                val powerManager = context.getSystemService(PowerManager::class.java)
                val packageName = context.packageName
                val isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false

                SettingsItem(
                    title = "Battery Optimization",
                    description = if (isIgnoringBatteryOptimizations) {
                        "Not optimized - background operation enabled"
                    } else {
                        "Tap to disable optimization for reliable background operation"
                    },
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to battery settings
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore if not available
                            }
                        }
                    }
                )
            }

            Divider()

            // Notification Settings
            SettingsSection(title = "Notifications") {
                Column {
                    Text(
                        text = "History Retention: ${settings.notificationRetentionDays} days",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Slider(
                        value = settings.notificationRetentionDays.toFloat(),
                        onValueChange = { onUpdateRetentionDays(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    title = "Voice Alerts",
                    description = "Enable voice notifications (future feature)",
                    trailing = {
                        Switch(
                            checked = settings.enableVoiceAlerts,
                            onCheckedChange = onUpdateVoiceAlerts
                        )
                    }
                )
            }

            Divider()

            // Glyph Settings
            SettingsSection(title = "Glyph Display") {
                SettingsItem(
                    title = "Glyph Toy Settings",
                    description = "Configure Glyph Toy in system settings",
                    onClick = {
                        // Open Glyph settings (if available on Nothing OS)
                        try {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to general settings
                        }
                    }
                )
            }

            Divider()

            // Testing Section
            SettingsSection(title = "Testing") {
                SettingsItem(
                    title = "Send Test Notification",
                    description = "Generate a sample notification for testing patterns",
                    onClick = {
                        sendTestNotification(context)
                    }
                )
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    description = "1.0.0"
                )
                SettingsItem(
                    title = "NotiGlyph",
                    description = "Display notifications on Nothing Glyph Matrix"
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Send a test notification for testing pattern matching
 */
private fun sendTestNotification(context: Context) {
    try {
        Log.d("SettingsScreen", "Attempting to send test notification")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notiglyph_test",
                "Test Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for testing NotiGlyph patterns"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("SettingsScreen", "Notification channel created")
        }

        // Use system icon to ensure it works
        val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.R.drawable.ic_dialog_info
        } else {
            android.R.drawable.ic_menu_info_details
        }

        // Build the notification
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val notification = NotificationCompat.Builder(context, "notiglyph_test")
            .setContentTitle("NotiGlyph Test")
            .setContentText("Example notification for notiglyph")
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // Post the notification
        notificationManager.notify(notificationId, notification)
        Log.d("SettingsScreen", "Test notification posted with ID: $notificationId")

        Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("SettingsScreen", "Error sending test notification", e)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
