package com.fnt.notiglyph.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fnt.notiglyph.data.database.dao.NotificationHistoryDao
import com.fnt.notiglyph.data.database.dao.PatternDao
import com.fnt.notiglyph.data.database.dao.SettingsDao
import com.fnt.notiglyph.data.database.entity.AppSettingsEntity
import com.fnt.notiglyph.data.database.entity.NotificationHistoryEntity
import com.fnt.notiglyph.data.database.entity.PatternEntity

@Database(
    entities = [
        PatternEntity::class,
        NotificationHistoryEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NotiGlyphDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: NotiGlyphDatabase? = null

        fun getInstance(context: Context): NotiGlyphDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotiGlyphDatabase::class.java,
                    "notiglyph_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
