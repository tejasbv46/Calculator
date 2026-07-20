package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: CalculatorDatabase? = null

        fun getDatabase(context: Context): CalculatorDatabase {
            return INSTANCE ?: synchronized(this) { //this allows only one db
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalculatorDatabase::class.java,
                    "calculator_database"
                )
                .fallbackToDestructiveMigration()//If Room detects a version mismatch
                //  it deletes the old database and creates a new one
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
//This class defines my Room database. Using the @Database annotation, I specify the HistoryEntity and UserEntity tables. The class extends RoomDatabase and provides DAO access through historyDao() and userDao(). I use the Singleton pattern with a companion object, @Volatile, and synchronized to ensure only one database instance exists across the application. The database is created using Room.databaseBuilder(), and if the schema version changes without a migration, fallbackToDestructiveMigration() recreates the database. This class is the central access point for all database operations in the app."