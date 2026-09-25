package com.bytemanager.stats.data.repository

import android.content.Context
import androidx.room.Room
import com.bytemanager.stats.data.database.TimestampedBatteryTempDao
import com.bytemanager.stats.data.database.TimestampedBatteryTempDatabase
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.utils.TimestampedBatteryTempBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryTempHistoryRepository @Inject constructor(@ApplicationContext private val appContext: Context): BatteryTempHistoryRepositoryInterface {
    override val className = "BatteryTempHistoryRepository"
    private val db = Room.databaseBuilder(
        appContext,
        TimestampedBatteryTempDatabase::class.java,
        "timeStampedBatteryTemp"
    )
        .addMigrations(TimestampedBatteryTempDatabase.Migration_1_TO_2)
        .build()
    override val dbDao: TimestampedBatteryTempDao = db.timeStampedBatteryTempDao()

    override fun closeDB() {
        db.close()
    }


    override val buffer = TimestampedBatteryTempBuffer(dbDao)
}