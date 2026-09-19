package com.bytemanager.stats.utils

import com.bytemanager.stats.data_structure.IntervalWiseBatteryTempMinMax
import com.bytemanager.stats.database.TimestampedBatteryTemp
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

object IntervalWiseMinMax {
    fun getIntervalWiseMinMaxDataList(
        list: List<TimestampedBatteryTemp>,
        dateSelected: LocalDate,
        intervalSize: Int
    ): List<IntervalWiseBatteryTempMinMax> {
        val startEpochSeconds = StatsTime().localDateToStartOfTheDayInEpochSeconds(dateSelected)

        val dataList = list
            .sortedBy {
                it.timestamp
            }
            .map {
                TimestampedBatteryTemp(timestamp = it.timestamp-startEpochSeconds, temperature = it.temperature)
            }

        val result: MutableList<IntervalWiseBatteryTempMinMax> = mutableListOf()
        var minTemp = Float.POSITIVE_INFINITY
        var maxTemp = Float.NEGATIVE_INFINITY

        var i = intervalSize
        var j = 0
        while (true) {
            if(j < dataList.size && dataList[j].timestamp < i) {
                minTemp = min(dataList[j].temperature, minTemp)
                maxTemp = max(dataList[j].temperature, maxTemp)
                j++
            } else {
                if(minTemp < Float.POSITIVE_INFINITY && maxTemp > Float.NEGATIVE_INFINITY) {
                    result.add(
                        IntervalWiseBatteryTempMinMax(
                            intervalIndex = i/intervalSize - 1,
                            minTemperature = minTemp,
                            maxTemperature = maxTemp
                        )
                    )
                }

                minTemp = Float.POSITIVE_INFINITY
                maxTemp = Float.NEGATIVE_INFINITY

                i += intervalSize

                if(j >= dataList.size) {
                    break
                }
            }
        }

        return result
    }
}