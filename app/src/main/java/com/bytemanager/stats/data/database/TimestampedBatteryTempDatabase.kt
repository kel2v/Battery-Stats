package com.bytemanager.stats.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TimestampedBatteryTemp::class],
    version = 2,
    exportSchema = true
)
abstract class TimestampedBatteryTempDatabase : RoomDatabase() {
    abstract fun timeStampedBatteryTempDao(): TimestampedBatteryTempDao

    companion object {
        val Migration_1_TO_2 = object: Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // Create new table
                db.execSQL(
                    """
                        CREATE TABLE timestampedBatteryTempV2 (
                            timestamp INTEGER NOT NULL PRIMARY KEY,
                            temperature REAL NOT NULL
                        )
                    """.trimIndent()
                )


                // Copy data into new table
                db.execSQL(
                    """
                        INSERT INTO timestampedBatteryTempV2(timestamp, temperature)
                        SELECT timestamp, temperature FROM timestampedBatteryTemp GROUP BY timestamp ORDER BY id DESC
                    """.trimIndent()
                )

                // deleting the old table
                db.execSQL(
                    """
                        DROP TABLE timestampedBatteryTemp
                    """.trimIndent()
                )

                // rename the new table with old table's name
                db.execSQL(
                    """
                        ALTER TABLE timestampedBatteryTempV2
                        RENAME TO timestampedBatteryTemp
                    """.trimIndent()
                )
            }
        }
    }
}