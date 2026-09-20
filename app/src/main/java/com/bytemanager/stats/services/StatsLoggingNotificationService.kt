package com.bytemanager.stats.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.notification.StatsLogger
import com.bytemanager.stats.notification.StatsNotificationManager
import com.bytemanager.stats.repository.BatteryStateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StatsLoggingNotificationService: Service() {
    @Inject lateinit var batteryStateRepository: BatteryStateRepository
    @Inject lateinit var batteryTempHistoryRepository: BatteryTempHistoryRepositoryInterface
    @Inject lateinit var statsLogger: StatsLogger
    private var scope  = CoroutineScope(SupervisorJob() + Dispatchers.Default) // there's no chance of accessing scope after scope.cancel() in onDestroy() as OS destroy instance on onDestroy is called


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("StatsLoggingNotificationService.onCreate", "Service onCreate called")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StatsLoggingNotificationService.onStartCommand", "Running onStartCommand")

        if (intent == null) {
            Log.d("StatsLoggingNotificationService.onStartCommand", "Restarted by OS with null intent, stopping.")
            stopSelf()
            return START_NOT_STICKY
        }

        StatsNotificationManager.createStatsNotificationChannel(applicationContext)
        startForeground(
            1,
            StatsNotificationManager.buildStatsNotification(
                applicationContext,
                batteryStateRepository.batteryStateStateFlow.value
            )
        )

        Log.d("StatsLoggingNotificationService.onStartCommand", "'postLoggingNotifications' started.")

        scope.launch {
            try {
                statsLogger.startStatsLogger()
            } catch (e: Exception) {
                Log.d("StatsLoggingNotificationService.onStartCommand", "Exception occurred: $e")
            } finally {
                stopSelf(startId)
            }
        }

        Log.d("StatsLoggingNotificationService.onStartCommand", "Exiting onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("StatsLoggingNotificationService.onDestroy", "Running StatsNotificationService.onDestroy")
        scope.cancel()
        StatsNotificationManager.closeStatsNotificationChannel(applicationContext)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}