package com.bytemanager.stats.interfaces

import com.bytemanager.stats.data.database.TimestampedBatteryTempDao
import com.bytemanager.stats.utils.TimestampedBatteryTempBuffer

interface BatteryTempHistoryRepositoryInterface {
    val className: String
    val dbDao: TimestampedBatteryTempDao
    fun closeDB()

    val buffer: TimestampedBatteryTempBuffer
}