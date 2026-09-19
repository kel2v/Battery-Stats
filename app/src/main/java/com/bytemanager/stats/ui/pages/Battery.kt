package com.bytemanager.stats.ui.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.bytemanager.stats.models.BatteryViewModel
import com.bytemanager.stats.ui.listitem.ListItem
import com.bytemanager.stats.utils.StatsNotificationServiceController

@SuppressLint("UnrememberedMutableState")
@Composable
fun Battery(batteryViewModel: BatteryViewModel = hiltViewModel(checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
})
) {
    val batteryState by batteryViewModel.batteryStateStateFlow.collectAsState()
    val isLoggingEnabled by BatteryViewModel.isLoggingEnabled
    val appContext = LocalContext.current.applicationContext

    Column(
        Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ListItem(parameterName = "battery type", parameterValue = batteryState.technology)
        ListItem(parameterName = "Battery level", parameterValue = "${batteryState.level}%")
        ListItem(parameterName = "Battery voltage", parameterValue = "${batteryState.voltage} V")
        ListItem(parameterName = "Battery temperature", parameterValue = "${batteryState.temperature} Celsius")
        ListItem(parameterName = "Battery health", parameterValue = batteryState.health)
        ListItem(parameterName = "Charging status", parameterValue = batteryState.chargingStatus)

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { BatteryViewModel.toggleLogging(StatsNotificationServiceController(appContext)) },
            modifier = Modifier.height(60.dp).fillMaxWidth(0.8f)
        ) {
            Text(if(isLoggingEnabled) "Logging is ON" else "Logging is OFF")
        }

        Spacer(Modifier.height(40.dp))

        BatteryTempGraphScreen()
    }
}