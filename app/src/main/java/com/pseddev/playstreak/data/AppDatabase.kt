package com.pseddev.playstreak.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.pseddev.playstreak.data.daos.AchievementDao
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.DailyCalendarStateDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.entities.AchievementType
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.CalendarColorLevel
import com.pseddev.playstreak.data.entities.DailyCalendarState
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.entities.SuccessLevel
import com.pseddev.playstreak.data.entities.TaskPriority

@Database(
    entities = [PieceOrTechnique::class, Activity::class, DailyCalendarState::class, Achievement::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pieceOrTechniqueDao(): PieceOrTechniqueDao
    abstract fun activityDao(): ActivityDao
    abstract fun dailyCalendarStateDao(): DailyCalendarStateDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mystreak_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromItemType(type: ItemType): String = type.name

    @TypeConverter
    fun toItemType(type: String): ItemType = ItemType.valueOf(type)

    @TypeConverter
    fun fromActivityType(type: ActivityType): String = type.name

    @TypeConverter
    fun toActivityType(type: String): ActivityType = ActivityType.valueOf(type)

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority = TaskPriority.valueOf(priority)

    @TypeConverter
    fun fromSuccessLevel(successLevel: SuccessLevel): String = successLevel.name

    @TypeConverter
    fun toSuccessLevel(successLevel: String): SuccessLevel = SuccessLevel.valueOf(successLevel)

    @TypeConverter
    fun fromCalendarColorLevel(colorLevel: CalendarColorLevel): String = colorLevel.name

    @TypeConverter
    fun toCalendarColorLevel(colorLevel: String): CalendarColorLevel = CalendarColorLevel.valueOf(colorLevel)

    @TypeConverter
    fun fromAchievementType(type: AchievementType): String = type.name

    @TypeConverter
    fun toAchievementType(type: String): AchievementType {
        val migratedType = when (type) {
            "STREAK_91_DAYS" -> "STREAK_100_DAYS"
            else -> type
        }
        return AchievementType.valueOf(migratedType)
    }
}
