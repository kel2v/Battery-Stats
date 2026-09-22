package com.bytemanager.stats.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.bytemanager.stats.data.database.TimestampedBatteryTemp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ExportCsv @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    fun createUri(
        fileName: String = "users_${System.currentTimeMillis()}.csv"
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
        }

        val uri = appContext.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )

        return uri
    }

    fun exportBatteryTempHistoryToCsv(
        uri: Uri,
        tempHistoryList: List<TimestampedBatteryTemp>
    ){
        if(tempHistoryList.isEmpty()) {
            Toast.makeText(appContext, "No data available for export for the given range", Toast.LENGTH_LONG).show()
            Log.d("DEBUGGING LOGS", "No data available for export for the given range")
            return
        }

        appContext.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            tempHistoryList.forEach { entry ->
                writer.appendLine(
                    "${entry.timestamp},${escapeCsv(entry.temperature.toString())}"
                )
            }
        }

        Toast.makeText(appContext, "Exported successfully to ${uri.path}", Toast.LENGTH_LONG).show()
        Log.d("DEBUGGING LOGS", "Exported successfully to ${uri.path}")
    }

    private fun escapeCsv(value: String): String {
        return if (
            value.contains(",") ||
            value.contains("\"") ||
            value.contains("\n")
        ) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

}