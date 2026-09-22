package com.bytemanager.stats.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.bytemanager.stats.R
import com.bytemanager.stats.data.types.BatteryState
import com.bytemanager.stats.hilt.ApplicationCoroutineScope
import com.bytemanager.stats.utils.BatteryStateRepositoryUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryStateRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @ApplicationCoroutineScope private val scope: CoroutineScope
) {
    val batteryStateFlow = callbackFlow {
        Log.d("DEBUGGING LOGS", "callbackFlow is started executing")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent == null) return

                val batteryStateRepositoryUtils = BatteryStateRepositoryUtils
                val state = BatteryState(
                    timestamp = Instant.now().atZone(ZoneId.systemDefault()).toEpochSecond(),
                    level = batteryStateRepositoryUtils.getBatteryLevelPercentage(
                        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                        scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    ),
                    temperature = batteryStateRepositoryUtils.getTemperatureInCelsius(
                        intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                    ),
                    voltage = batteryStateRepositoryUtils.getVoltage(
                        intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                    ),
                    technology = batteryStateRepositoryUtils.getTechnology(
                        intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                    ),

                    health = batteryStateRepositoryUtils.getHealth(
                        intent.getIntExtra(
                            BatteryManager.EXTRA_HEALTH,
                            BatteryManager.BATTERY_HEALTH_UNKNOWN
                        )
                    ),

                    chargingStatus = batteryStateRepositoryUtils.getChargingStatus(
                        intent.getIntExtra(
                            BatteryManager.EXTRA_STATUS,
                            BatteryManager.BATTERY_STATUS_UNKNOWN
                        )
                    )
                )

                trySend(state)
            }
        }

        appContext.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        Log.d("DEBUGGING LOGS", "Registered BroadcastReceiver.")

        awaitClose {
            appContext.unregisterReceiver(receiver)
            Log.d("DEBUGGING LOGS", "Unregistered BroadcastReceiver.")
        }
    }

    val batteryStateSharedFlow = batteryStateFlow.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
    )

    val batteryStateStateFlow = batteryStateSharedFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = BatteryState(
            timestamp = 0,
            level = Int.MIN_VALUE,
            voltage = Float.MIN_VALUE,
            chargingStatus = appContext.getString(R.string.not_available),
            temperature = Float.MIN_VALUE,
            technology = appContext.getString(R.string.not_available),
            health = appContext.getString(R.string.not_available)
        )
    )
}