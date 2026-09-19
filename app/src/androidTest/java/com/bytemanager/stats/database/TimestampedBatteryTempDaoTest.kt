package com.bytemanager.stats.database

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bytemanager.stats.data.DbFillData
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.utils.StatsTime
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TimestampedBatteryTempDaoTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    @Inject
    lateinit var batteryTempHistoryRepository: BatteryTempHistoryRepositoryInterface
    private lateinit var dbDao: TimestampedBatteryTempDao

    private val dbFillData = DbFillData
    private val jan1st1970List = dbFillData.jan1st1970List
    private val jan2nd1970List = dbFillData.jan2nd1970List
    private val jan3rd1970List = dbFillData.jan3rd1970List
    private val jan4th1970List = dbFillData.jan4th1970List



    @Before
    fun setup() {
        hiltRule.inject()
        Log.d("DEBUGGING LOGS", "batteryTempHistoryRepository's class name = ${batteryTempHistoryRepository.className}")

        dbDao = batteryTempHistoryRepository.dbDao
        runBlocking { dbDao.deleteAll() }
    }

    @After
    fun tearDown() {
        batteryTempHistoryRepository.closeDB()
    }

    fun insertItems(
        dao: TimestampedBatteryTempDao,
        inputList: List<TimestampedBatteryTemp>
    ) {
        runBlocking {
            dao.insertAll(inputList)
        }
    }

    fun getFirstValueOfFlow(listFlow: Flow<List<TimestampedBatteryTemp>>): List<TimestampedBatteryTemp> {
        var result: List<TimestampedBatteryTemp>
        runBlocking {
            result = listFlow.first()
        }

        return result
    }

    fun listAssertion(
        expectedList: List<TimestampedBatteryTemp>,
        resultList: List<TimestampedBatteryTemp>
    ) {
        val expectedListFinal = expectedList.sortedBy { it -> it.timestamp }
        val resultListFinal = resultList.sortedBy { it -> it.timestamp }

        assertEquals(expectedListFinal.size, resultListFinal.size)
        for(i in 0 .. expectedList.size - 1) {
            assertEquals(expectedListFinal[i].timestamp, resultListFinal[i].timestamp)
            assertEquals(expectedListFinal[i].temperature, resultListFinal[i].temperature)
        }
    }

    fun dayAssertion(
        dao: TimestampedBatteryTempDao,
        expectedListFull: List<TimestampedBatteryTemp>,
        year: Int, month: Int, day: Int,
        zoneId: ZoneId
    ) {
        val localDate = LocalDate.of(year, month, day)
        val timestampRange = StatsTime().localDayToTimestampInterval(localDate)
        val resultList = getFirstValueOfFlow(
            dao.getAllByLocalDate(localDate)
        )
        val expectedList = expectedListFull.filter {
            it.timestamp >= timestampRange.startTimestamp && it.timestamp <= timestampRange.endTimestamp
        }

        listAssertion(expectedList, resultList)
    }


    @Test
    fun getCount___emptyDatabase___zeroAsOutput() {
        val getCountQueryResult = runBlocking { dbDao.getCount() }
        val getAllQueryResult = runBlocking { dbDao.getAll() }

        assertEquals(true, getAllQueryResult.isEmpty())
        assertEquals(getCountQueryResult.toInt(), getAllQueryResult.size)
    }

    @Test
    fun getCount___prefilledDatabase___correctOutput() {
        val getAllQueryBeforePrefillResult = runBlocking { dbDao.getAll() }
        assertEquals(true, getAllQueryBeforePrefillResult.isEmpty())

        val insertList = jan1st1970List
        insertItems(dbDao, insertList)

        val getAllQueryAfterPrefillResult = runBlocking { dbDao.getAll() }
        assertEquals(true, getAllQueryAfterPrefillResult.isNotEmpty())

        val getCountQueryResult = runBlocking { dbDao.getCount() }

        assertEquals(getCountQueryResult.toInt(), getAllQueryAfterPrefillResult.size)
    }

    @Test
    fun getAll___validInputToFreshDatabase___correctOutput() {
        val insertList = jan1st1970List
        insertItems(dbDao, insertList)

        val resultList = runBlocking { dbDao.getAll() }.sortedBy { it -> it.timestamp }
        val expectedList = insertList.sortedBy { it -> it.timestamp }

        listAssertion(expectedList, resultList)
    }


    @Test
    fun getAll___validInputToPrefilledDatabase___correctOutput() {
        val prefilledList = jan1st1970List
        val newInsertList = jan2nd1970List + jan3rd1970List + jan4th1970List

        insertItems(dbDao, prefilledList)
        insertItems(dbDao, newInsertList)

        val resultList = runBlocking { dbDao.getAll() }.sortedBy { it -> it.timestamp }
        val expectedList = (prefilledList + newInsertList).sortedBy { it -> it.timestamp }

        listAssertion(expectedList, resultList)
    }


    @Test
    fun getAllByDate___validInputToFreshDatabase___correctOutput() {
        val insertList = jan1st1970List + jan2nd1970List + jan3rd1970List + jan4th1970List
        insertItems(dbDao, insertList)

        val expectedListFull = insertList

        dayAssertion(dbDao, expectedListFull, 1970, 1, 1, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 1, 2, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 1, 3, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 1, 4, ZoneId.of("UTC"))
    }


    @Test
    fun getAllByDate___validInputToPrefilledDatabase___correctOutput() {
        val prefilledList = jan1st1970List
        val newInsertList = jan2nd1970List + jan3rd1970List + jan4th1970List
        insertItems(dbDao, prefilledList)
        insertItems(dbDao, newInsertList)

        val expectedListFull = prefilledList + newInsertList

        dayAssertion(dbDao, expectedListFull, 1970, 1, 1, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 1, 2, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 1, 3, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 1, 4, ZoneId.of("UTC"))
    }

    @Test
    fun getAllByDate___nonAvailableValidDateQuery___nullListOutput() {
        val insertList = jan1st1970List + jan2nd1970List + jan3rd1970List + jan4th1970List
        insertItems(dbDao, insertList)

        val expectedListFull = insertList

        dayAssertion(dbDao, expectedListFull, 1970, 1, 15, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1970, 2, 1, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 2000, 1, 1, ZoneId.of("UTC"))
        dayAssertion(dbDao, expectedListFull, 1910, 1, 1, ZoneId.of("UTC"))
    }


    @Test
    fun getAllByDate___invalidDateQuery___throwException() {
        val insertList = jan1st1970List + jan2nd1970List + jan3rd1970List + jan4th1970List
        insertItems(dbDao, insertList)

        val expectedListFull = insertList

        listOf(
            { dayAssertion(dbDao, expectedListFull, 1970, 1, 33, ZoneId.of("UTC")) },
            { dayAssertion(dbDao, expectedListFull, 1969, 2, 29, ZoneId.of("UTC")) },
            { dayAssertion(dbDao, expectedListFull, -1, -1, -1, ZoneId.of("UTC")) },
            { dayAssertion(dbDao, expectedListFull, 0, 0, 0, ZoneId.of("UTC")) }
        ).forEach {
            var isExceptionThrown = false
            try {
                it()
            } catch(_: Exception) {
                isExceptionThrown = true
            }
            assert(isExceptionThrown)
        }
    }
}