package com.bytemanager.stats.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.bytemanager.stats.data.repository.BatteryStateRepository
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.notification.StatsLogger
import com.bytemanager.stats.notification.StatsNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class StatsLoggingNotificationService: Service() {
    private var job: Job? = null
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
        if(job?.isActive == true) {
            return START_STICKY
        }

        Log.d("StatsLoggingNotificationService.onStartCommand", "Running onStartCommand")

        StatsNotificationManager.createStatsNotificationChannel(applicationContext)

        startForeground(
            1,
            StatsNotificationManager.buildStatsNotification(
                applicationContext,
                batteryStateRepository.batteryStateStateFlow.value
            )
        )

        job = scope.launch {
            try {
                statsLogger.startStatsLogger()
            } catch (e: CancellationException) {
                // Expected when the service is destroyed
                throw e
            } catch (e: Exception) {
                Log.e("StatsLoggingNotificationService.onStartCommand", "Stats logger failed", e)
                stopSelf()
            }
        }

        Log.d("StatsLoggingNotificationService.onStartCommand", "Exiting onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("StatsLoggingNotificationService.onDestroy", "Running StatsNotificationService.onDestroy")

        job?.cancel()
        job = null
        scope.cancel()

        StatsNotificationManager.closeStatsNotificationChannel(applicationContext)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}