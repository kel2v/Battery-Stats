package com.bytemanager.stats.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Transaction
    suspend fun replaceAllItems(newItems: List<TimestampedBatteryTemp>) {
        deleteAll()
        insertAll(newItems)
    }

    @Query("SELECT MIN(temperature) FROM timestampedBatteryTemp")
    fun minTemperature(): Flow<Float?>

    @Query("SELECT MAX(temperature) FROM timestampedBatteryTemp")
    fun maxTemperature(): Flow<Float?>

    fun getAllByLocalDate(localDate: LocalDate): Flow<List<TimestampedBatteryTemp>> {
        val timestampInterval = StatsTime().localDayToTimestampInterval(localDate)
        return getByTimestampRange(timestampInterval.startTimestamp, timestampInterval.endTimestamp)
    }
}