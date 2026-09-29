package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HabitEntity::class, DailyCompletionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mono_habits.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialHabits(database.habitDao())
                    }
                }
            }

            suspend fun populateInitialHabits(dao: HabitDao) {
                val initialHabits = listOf(
                    HabitEntity(
                        id = 1,
                        name = "Morning Exercise",
                        category = "Health",
                        iconName = "activity",
                        goal = "30 mins",
                        preferredTime = "Morning",
                        streak = 3,
                        bestStreak = 5,
                        lastCompletedDate = ""
                    ),
                    HabitEntity(
                        id = 2,
                        name = "Read 20 Minutes",
                        category = "Mind",
                        iconName = "book",
                        goal = "20 mins",
                        preferredTime = "Evening",
                        streak = 7,
                        bestStreak = 12,
                        lastCompletedDate = ""
                    ),
                    HabitEntity(
                        id = 3,
                        name = "Drink Water",
                        category = "Health",
                        iconName = "water",
                        goal = "2.5 Liters",
                        preferredTime = "Anytime",
                        streak = 14,
                        bestStreak = 14,
                        lastCompletedDate = ""
                    ),
                    HabitEntity(
                        id = 4,
                        name = "Study Electronics",
                        category = "Learning",
                        iconName = "study",
                        goal = "45 mins",
                        preferredTime = "Afternoon",
                        streak = 2,
                        bestStreak = 4,
                        lastCompletedDate = ""
                    ),
                    HabitEntity(
                        id = 5,
                        name = "Practice Guitar",
                        category = "Hobby",
                        iconName = "guitar",
                        goal = "25 mins",
                        preferredTime = "Evening",
                        streak = 5,
                        bestStreak = 9,
                        lastCompletedDate = ""
                    )
                )
                dao.insertHabits(initialHabits)
            }
        }
    }
}
