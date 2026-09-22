package com.bytemanager.stats.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timestampedBatteryTemp",
    indices = [Index(value = ["timestamp"])]
)
data class TimestampedBatteryTemp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "temperature") val temperature: Float
)