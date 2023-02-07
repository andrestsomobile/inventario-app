package com.koba.inventario.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [IncomeEntity::class,PickupEntity::class,PositionEntity::class,RelocationEntity::class,ValidateEntity::class,TrafficEntity::class],
    version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun pickupDao(): PickupDao
    abstract fun positionDao(): PositionDao
    abstract fun relocationDao(): RelocationDao
    abstract fun validateDao(): ValidateDao
    abstract fun trafficDao(): TrafficDao

    companion object {

        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val builder = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "database-name"
                ).fallbackToDestructiveMigration()
                val instance = builder.build()
                INSTANCE = instance
                return instance
            }
        }
    }

}
