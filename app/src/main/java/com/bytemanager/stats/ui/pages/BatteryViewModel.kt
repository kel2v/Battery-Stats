package com.bytemanager.stats.ui.pages

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bytemanager.stats.data.types.BatteryState
import com.bytemanager.stats.data.repository.BatteryStateRepository
import com.bytemanager.stats.utils.StatsNotificationServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BatteryViewModel @Inject constructor(batteryStateRepository: BatteryStateRepository) : ViewModel() {
    val batteryStateStateFlow: StateFlow<BatteryState> = batteryStateRepository.batteryStateStateFlow


    companion object {
        val isLoggingEnabled = mutableStateOf(true)

        fun toggleLogging(statsNotificationServiceController: StatsNotificationServiceController) {
            isLoggingEnabled.value = !isLoggingEnabled.value

            if(isLoggingEnabled()) {
                statsNotificationServiceController.startForegroundService()
            } else {
                statsNotificationServiceController.stopService()
            }
        }

        fun isLoggingEnabled(): Boolean {
            return isLoggingEnabled.value
        }
    }
}