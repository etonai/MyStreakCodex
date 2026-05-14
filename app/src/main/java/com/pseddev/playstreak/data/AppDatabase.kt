package com.pseddev.mystreak.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pseddev.mystreak.data.daos.AchievementDao
import com.pseddev.mystreak.data.daos.ActivityDao
import com.pseddev.mystreak.data.daos.DailyCalendarStateDao
import com.pseddev.mystreak.data.daos.PieceOrTechniqueDao
import com.pseddev.mystreak.data.entities.Achievement
import com.pseddev.mystreak.data.entities.AchievementType
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.data.entities.CalendarColorLevel
import com.pseddev.mystreak.data.entities.DailyCalendarState
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.SuccessLevel
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority

@Database(
    entities = [PieceOrTechnique::class, Activity::class, DailyCalendarState::class, Achievement::class],
    version = 9,
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
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN taskKind TEXT NOT NULL DEFAULT 'STANDARD'")
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
    fun fromTaskKind(kind: TaskKind): String = kind.name

    @TypeConverter
    fun toTaskKind(kind: String): TaskKind = TaskKind.valueOf(kind)

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
