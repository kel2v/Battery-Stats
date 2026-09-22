package com.bytemanager.stats.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.bytemanager.stats.data.testdata.DbFillData
import com.bytemanager.stats.data.database.TimestampedBatteryTemp
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    @Inject
    lateinit var batteryTempHistoryRepository: BatteryTempHistoryRepositoryInterface
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    fun getAllDBData(): List<TimestampedBatteryTemp> {
        val allDBData = runBlocking { batteryTempHistoryRepository.dbDao.getAll() }
        return allDBData
    }

    fun getBuffer(): List<TimestampedBatteryTemp> {
        val buffer = runBlocking{ batteryTempHistoryRepository.buffer.getBuffer() }
        return buffer
    }

    fun verifyWorkCompletion(request: PeriodicWorkRequest, workManager: WorkManager) {
        val timeout = System.currentTimeMillis() + 5_000
        var workInfo = workManager.getWorkInfoById(request.id).get()
        while (workInfo?.state != WorkInfo.State.ENQUEUED && System.currentTimeMillis() < timeout) {
            Thread.sleep(100)
            workInfo = workManager.getWorkInfoById(request.id).get()
        }

        assertEquals(WorkInfo.State.ENQUEUED, workInfo?.state)
    }

    fun assertEqualList(expected: List<TimestampedBatteryTemp>, actual: List<TimestampedBatteryTemp>) {
        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { i, item ->
            assertEquals(item.timestamp, actual[i].timestamp)
            assertEquals(item.temperature, actual[i].temperature)
        }
    }

    @Before
    fun setup() {
        hiltRule.inject()
        Log.d("DEBUGGING LOGS", "batteryTempHistoryRepository's class name = ${batteryTempHistoryRepository.className}")
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        runBlocking { batteryTempHistoryRepository.buffer.clearBuffer() }

        runBlocking{ batteryTempHistoryRepository.dbDao.insertAll(listOf(TimestampedBatteryTemp(timestamp = 0L, temperature =  123f))) }
        Log.d("DEBUGGING LOGS", "from SyncWorkerTest: Testing db working. DB size after test insertion: ${getAllDBData().size}")
        runBlocking { batteryTempHistoryRepository.dbDao.deleteAll() }
    }

    @After
    fun destroy() {
        batteryTempHistoryRepository.closeDB()
    }

    @Test
    fun testPeriodicWork_initialDelaySet15Minutes_validOutput() {
        val tempDataSource = DbFillData
        runBlocking {
            tempDataSource.jan1st1970List.forEach {
                batteryTempHistoryRepository.buffer.addItemToBuffer(it)
            }
        }
        assertEquals(tempDataSource.jan1st1970List.size, getBuffer().size)

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(15, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setInitialDelayMet(request.id)

        verifyWorkCompletion(request, workManager)

        assertEquals(getBuffer().size, 0)

        val actual = getAllDBData().sortedBy { it.timestamp }
        val expected = tempDataSource.jan1st1970List.sortedBy { it.timestamp }

        assertEqualList(expected, actual)
    }

    @Test
    fun testPeriodicWork_periodicDelaySet15Minutes_validOutput() {
        val tempDataSource = DbFillData
        runBlocking {
            tempDataSource.jan1st1970List.forEach {
                batteryTempHistoryRepository.buffer.addItemToBuffer(it)
            }
        }
        assertEquals(tempDataSource.jan1st1970List.size, getBuffer().size)

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setPeriodDelayMet(request.id)

        verifyWorkCompletion(request, workManager)

        assertEquals(getBuffer().size, 0)

        val actual = getAllDBData().sortedBy { it.timestamp }
        val expected = tempDataSource.jan1st1970List.sortedBy { it.timestamp }

        assertEqualList(expected, actual)
    }

    @Test
    fun testPeriodicWork_initialDelayAndPeriodicDelaySet15Minutes_validOutput() {
        val tempDataSource = DbFillData
        runBlocking {
            tempDataSource.jan1st1970List.forEach {
                batteryTempHistoryRepository.buffer.addItemToBuffer(it)
            }
        }

        assertEquals(tempDataSource.jan1st1970List.size, getBuffer().size)

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(15, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setInitialDelayMet(request.id)

        verifyWorkCompletion(request, workManager)

        assertEquals(getBuffer().size, 0)

        runBlocking {
            tempDataSource.jan2nd1970List.forEach {
                batteryTempHistoryRepository.buffer.addItemToBuffer(it)
            }
        }

        assertEquals(tempDataSource.jan2nd1970List.size, getBuffer().size)

        testDriver.setPeriodDelayMet(request.id)

        verifyWorkCompletion(request, workManager)

        assertEquals(getBuffer().size, 0)

        val actual = getAllDBData().sortedBy { it.timestamp }
        val expected = (tempDataSource.jan1st1970List + tempDataSource.jan2nd1970List).sortedBy { it.timestamp }

        assertEqualList(expected, actual)
    }
}