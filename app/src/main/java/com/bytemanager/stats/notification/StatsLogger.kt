package com.bytemanager.stats.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.bytemanager.stats.data_structure.BatteryState
import com.bytemanager.stats.database.TimestampedBatteryTemp
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.repository.BatteryStateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StatsLogger @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val batteryStateRepository: BatteryStateRepository,
    private val batteryTempHistoryRepository: BatteryTempHistoryRepositoryInterface
) {
    suspend fun startStatsLogger() {
        logger()
    }

    private suspend fun logger() {
        val notificationManager = appContext.getSystemService(NotificationManager::class.java)

        batteryStateRepository.batteryStateFlow.collect { value ->
            logTemperature(value)
            notifyLogging(value, notificationManager)
        }
    }

    private suspend fun logTemperature(value: BatteryState) {
        val newRecord = TimestampedBatteryTemp(timestamp = value.timestamp, temperature = value.temperature)
        Log.d("DEBUGGING LOGS", "Adding new batteryTemp to buffer: $value")
        batteryTempHistoryRepository.buffer.addItemToBuffer(newRecord)
    }

    private fun notifyLogging(value: BatteryState, notificationManager: NotificationManager) {
        if(StatsNotificationManager.notificationPermissionGranted) {
            val notification = StatsNotificationManager.buildStatsNotification(appContext, value)
            notificationManager.notify(1, notification)
            Log.d(
                "DEBUGGING LOGS",
                "${
                    notification.extras.getString(
                        Notification.EXTRA_TITLE,
                        "NA"
                    )
                }\n${notification.extras.getString(Notification.EXTRA_TEXT, "NA")}"
            )
        }
    }
}