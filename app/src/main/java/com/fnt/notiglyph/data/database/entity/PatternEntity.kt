package com.fnt.notiglyph.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patterns")
data class PatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appPackageName: String,
    val appDisplayName: String,
    val patternType: String, // TEMPLATE, REGEX, KEYWORD
    val patternString: String,
    val extractedVariables: String, // JSON array of variable names
    val displayTemplate: String,
    val priority: Int,
    val isEnabled: Boolean,
    val iconType: String, // EMOJI, CUSTOM, NONE
    val displayDurationSeconds: Int,
    val delaySeconds: Int = 0, // Delay before showing on Glyph
    val createdAt: Long
)
