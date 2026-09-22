package com.bytemanager.stats.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytemanager.stats.utils.StatsTime
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimestampedBatteryTempDao {
    @Query("SELECT COUNT(*) FROM timestampedBatteryTemp")
    suspend fun getCount(): Long

    @Query("SELECT * FROM timestampedBatteryTemp")
    suspend fun getAll(): List<TimestampedBatteryTemp>

    @Query("SELECT * FROM timestampedBatteryTemp WHERE timestamp >= :startTimestamp AND timestamp < :endTimestamp")
    fun getByTimestampRange(startTimestamp: Long, endTimestamp: Long): Flow<List<TimestampedBatteryTemp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newInsertList: List<TimestampedBatteryTemp>)

    @Query("DELETE FROM timestampedBatteryTemp")
    suspend fun deleteAll()

    fun getAllByLocalDate(localDate: LocalDate): Flow<List<TimestampedBatteryTemp>> {
        val timestampInterval = StatsTime().localDayToTimestampInterval(localDate)
        return getByTimestampRange(timestampInterval.startTimestamp, timestampInterval.endTimestamp)
    }
}