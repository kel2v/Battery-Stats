package com.bytemanager.stats.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TimestampedBatteryTemp::class],
    version = 1,
    exportSchema = true
)
abstract class TimestampedBatteryTempDatabase : RoomDatabase() {
    abstract fun timeStampedBatteryTempDao(): TimestampedBatteryTempDao
}