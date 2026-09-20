package com.bytemanager.stats

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bytemanager.stats.models.BatteryViewModel
import com.bytemanager.stats.notification.StatsNotificationManager
import com.bytemanager.stats.ui.theme.StatsTheme
import com.bytemanager.stats.utils.StatsNotificationServiceController
import com.bytemanager.stats.worker.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUGGING LOGS", "running onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("DEBUGGING LOGS", "Permission granted")
                    StatsNotificationManager.notificationPermissionGranted = true
                } else {
                    Log.d("DEBUGGING LOGS", "Permission NOT granted")
                    StatsNotificationManager.notificationPermissionGranted = false
                }
            }
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Log.d("DEBUGGING LOGS", "Old API, no permission request needed")
            StatsNotificationManager.notificationPermissionGranted = true
        }

        if(BatteryViewModel.isLoggingEnabled()) {
            StatsNotificationServiceController(this.applicationContext).startForegroundService()
        }

        val immediateRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag("one time buffer flushing")
            .build()
        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(repeatInterval = 15, repeatIntervalTimeUnit = TimeUnit.MINUTES)
            .addTag("periodic buffer flushing")
            .build()

        val workManager = WorkManager.getInstance(this.applicationContext)
        workManager.apply {
            enqueue(immediateRequest)
            enqueueUniquePeriodicWork(
                "buffer flushing",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }


        val powerManager = getSystemService(PowerManager::class.java)
        val exempt = powerManager.isIgnoringBatteryOptimizations(packageName)

        Log.d("DEBUGGING LOGS", "exempt: $exempt")


        if (!exempt) {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                "package:$packageName".toUri()
            )
            startActivity(intent)
        }


        setContent {
            StatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StatsApp()
                }
            }
        }
        Log.d("DEBUGGING LOGS", "exiting onCreate")
    }

    override fun onResume() {
        Log.d("DEBUGGING LOGS", "running onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("DEBUGGING LOGS", "running onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("DEBUGGING LOGS", "running onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("DEBUGGING LOGS", "running onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DEBUGGING LOGS", "running onDestroy")
    }
}