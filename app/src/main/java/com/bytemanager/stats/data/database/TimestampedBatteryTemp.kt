package com.bytemanager.stats.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "timestampedBatteryTemp",
)
data class TimestampedBatteryTemp(
    @PrimaryKey val timestamp: Long,
    @ColumnInfo(name = "temperature") val temperature: Float
)