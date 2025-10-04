package com.fnt.notiglyph.domain.model

import com.fnt.notiglyph.data.database.entity.PatternEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Domain model for a notification pattern
 */
data class NotificationPattern(
    val id: Long = 0,
    val appPackageName: String,
    val appDisplayName: String,
    val patternType: PatternType,
    val patternString: String,
    val extractedVariables: List<String>,
    val displayTemplate: String,
    val priority: Int,
    val isEnabled: Boolean,
    val iconType: IconType,
    val displayDurationSeconds: Int,
    val delaySeconds: Int = 0,
    val createdAt: Long
) {
    /**
     * Convert domain model to entity for database storage
     */
    fun toEntity(): PatternEntity {
        val gson = Gson()
        return PatternEntity(
            id = id,
            appPackageName = appPackageName,
            appDisplayName = appDisplayName,
            patternType = patternType.name,
            patternString = patternString,
            extractedVariables = gson.toJson(extractedVariables),
            displayTemplate = displayTemplate,
            priority = priority,
            isEnabled = isEnabled,
            iconType = iconType.name,
            displayDurationSeconds = displayDurationSeconds,
            delaySeconds = delaySeconds,
            createdAt = createdAt
        )
    }

    companion object {
        /**
         * Convert entity to domain model
         */
        fun fromEntity(entity: PatternEntity): NotificationPattern {
            val gson = Gson()
            val variableListType = object : TypeToken<List<String>>() {}.type
            val variables: List<String> = try {
                gson.fromJson(entity.extractedVariables, variableListType)
            } catch (e: Exception) {
                emptyList()
            }

            return NotificationPattern(
                id = entity.id,
                appPackageName = entity.appPackageName,
                appDisplayName = entity.appDisplayName,
                patternType = PatternType.valueOf(entity.patternType),
                patternString = entity.patternString,
                extractedVariables = variables,
                displayTemplate = entity.displayTemplate,
                priority = entity.priority,
                isEnabled = entity.isEnabled,
                iconType = IconType.valueOf(entity.iconType),
                displayDurationSeconds = entity.displayDurationSeconds,
                delaySeconds = entity.delaySeconds,
                createdAt = entity.createdAt
            )
        }
    }
}
