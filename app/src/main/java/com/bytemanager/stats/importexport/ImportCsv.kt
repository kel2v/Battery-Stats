package com.bytemanager.stats.importexport

import com.bytemanager.stats.data.database.TimestampedBatteryTemp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

class ImportCsv @Inject constructor() {
    suspend fun extractImportList(input: InputStream): List<TimestampedBatteryTemp> = withContext(Dispatchers.IO) {
        val items = input.bufferedReader().useLines { lines ->
            lines
                .filter {
                    line -> line.isNotBlank()
                }
                .mapNotNull { line ->
                    val columns = line.split(',')

                    if (columns.size != 2) {
                        null
                    } else {
                        val timestamp = columns[0].trim().toLongOrNull()
                        val temperature = columns[1].trim().toFloatOrNull()

                        if (timestamp != null && temperature != null) {
                            TimestampedBatteryTemp(
                                // Leave id as 0 so Room generates it
                                timestamp = timestamp,
                                temperature = temperature
                            )
                        } else {
                            null
                        }
                    }
                }
                .toList()
        }

        items
    }
}