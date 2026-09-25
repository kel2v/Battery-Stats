package com.bytemanager.stats.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bytemanager.stats.services.StatsLoggingNotificationService

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, StatsLoggingNotificationService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}