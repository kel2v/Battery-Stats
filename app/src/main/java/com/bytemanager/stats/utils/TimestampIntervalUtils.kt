package com.bytemanager.stats.utils

import com.bytemanager.stats.data.types.TimestampInterval
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

class StatsTime(val zoneId: ZoneId = ZoneId.systemDefault()) {
    fun localDayToTimestampInterval(localDate: LocalDate): TimestampInterval {
        val startTimestamp = localDate.atStartOfDay(zoneId).toEpochSecond()
        val endTimestamp = localDate.plusDays(1).atStartOfDay(zoneId).toEpochSecond()

        return TimestampInterval(startTimestamp, endTimestamp)
    }

    fun today(): LocalDate {
        val now = Instant.now().atZone(zoneId)
        val year = now.year
        val month = now.month
        val day = now.dayOfMonth

        return LocalDate.of(year, month+1, day)
    }

    fun millisToLocalDate(millis: Long?): LocalDate? {
        if(millis == null) {
            return null
        }

        val calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId))
        calendar.timeInMillis = millis

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        return LocalDate.of(year, month+1, dayOfMonth)
    }

    fun localDateToStartOfTheDayInEpochSeconds(localDate: LocalDate): Long {
        val result = localDate.atStartOfDay(zoneId).toEpochSecond()

        return result
    }
}