package com.bytemanager.stats.utils

import com.bytemanager.stats.data.types.TimestampInterval
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals

class BatteryTempRepositoryUtilsTest {
    companion object {
        @JvmStatic
        fun dateProvider(): List<Arguments> {
            val zoneIdOfUTC = ZoneId.of("UTC")
            val zoneIdOfAsiaKolkata = ZoneId.of("Asia/Kolkata")
            val zoneIdOfAmericaNewYork = ZoneId.of("America/New_York")
            val zoneIdOfAsiaTokyo = ZoneId.of("Asia/Tokyo")
            val zoneIdOfEuropeMoscow = ZoneId.of("Europe/Moscow")

            val americaNewYorkDSTOffset = 14400L
            val americaNewYorkOffset = 18000L
            val asiaKolkataOffset = -19800L
            val asiaTokyoOffset = -32400L
            val europeMoscowOffset = -10800L

            return listOf(
                // NON_DST
                    // UTC
                    Arguments.of(
                        2028, 2, 29,
                        zoneIdOfUTC,
                        TimestampInterval(1835395200, 1835481599)
                    ),

                    Arguments.of(
                        2040, 2, 29,
                        zoneIdOfUTC,
                        TimestampInterval(2214086400, 2214172799)
                    ),




                    // Asia/Kolkata
                    Arguments.of(
                        2028, 2, 29,
                        zoneIdOfAsiaKolkata,
                        TimestampInterval(1835395200 + asiaKolkataOffset, 1835481599 + asiaKolkataOffset)
                    ),

                    Arguments.of(
                        2040, 2, 29,
                        zoneIdOfAsiaKolkata,
                        TimestampInterval(2214086400 + asiaKolkataOffset, 2214172799 + asiaKolkataOffset)
                    ),




                    // Asia/Tokyo
                    Arguments.of(
                        2028, 2, 29,
                        zoneIdOfAsiaTokyo,
                        TimestampInterval(1835395200 + asiaTokyoOffset, 1835481599 + asiaTokyoOffset)
                    ),

                    Arguments.of(
                        2040, 2, 29,
                        zoneIdOfAsiaTokyo,
                        TimestampInterval(2214086400 + asiaTokyoOffset, 2214172799 + asiaTokyoOffset)
                    ),


                    // Europe/Moscow
                    Arguments.of(
                        2028, 2, 29,
                        zoneIdOfEuropeMoscow,
                        TimestampInterval(1835395200 + europeMoscowOffset, 1835481599 + europeMoscowOffset)
                    ),

                    Arguments.of(
                        2040, 2, 29,
                        zoneIdOfEuropeMoscow,
                        TimestampInterval(2214086400 + europeMoscowOffset, 2214172799 + europeMoscowOffset)
                    ),





                // DST
                    // America/New_York
//                    Arguments.of(
//                        2028, 2, 29,
//                        zoneIdOfAmericaNewYork,
//                        TimestampBasedTimeInterval(1835395200 + americaNewYorkOffset, 1835481599 + americaNewYorkOffset)
//                    ),
//
//                    Arguments.of(
//                        2028, 7, 29,
//                        zoneIdOfAmericaNewYork,
//                        TimestampBasedTimeInterval(1848441600 + americaNewYorkDSTOffset, 1848527999 + americaNewYorkDSTOffset)
//                    ),
//
//                    Arguments.of(
//                        2040, 2, 29,
//                        zoneIdOfAmericaNewYork,
//                        TimestampBasedTimeInterval(2214086400 + americaNewYorkOffset, 2214172799 + americaNewYorkOffset)
//                    ),
            )
        }
    }
    @ParameterizedTest
    @MethodSource("dateProvider")
    fun convertDayIntoTimestampBasedTimeRange_validInput_correctOutput(
        year: Int,
        month: Int,
        day: Int,
        zoneId: ZoneId,
        expected: TimestampInterval
    ) {
        val statsTime = StatsTime(zoneId)
        assertEquals(
            expected.copy(
                startTimestamp = expected.startTimestamp,
                endTimestamp = expected.endTimestamp+1
            ),
            statsTime.localDayToTimestampInterval(LocalDate.of(year, month, day)))
    }
}