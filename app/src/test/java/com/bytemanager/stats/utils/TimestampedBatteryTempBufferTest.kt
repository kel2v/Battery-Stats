package com.bytemanager.stats.utils

import com.bytemanager.stats.data.testdata.DbFillData
import com.bytemanager.stats.data.database.TimestampedBatteryTemp
import com.bytemanager.stats.data.database.TimestampedBatteryTempDao
import junit.framework.TestCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class TimestampedBatteryTempBufferTest {
    class MockDao: TimestampedBatteryTempDao {
        private val dbList = mutableListOf<TimestampedBatteryTemp>()

        override suspend fun getCount(): Long {
            return dbList.size.toLong()
        }

        override suspend fun getAll(): List<TimestampedBatteryTemp> {
            return dbList.toList()
        }

        override suspend fun deleteAll() {
            dbList.clear()
        }

        override suspend fun insertAll(newInsertList: List<TimestampedBatteryTemp>) {
            newInsertList.forEach {
                dbList.add(it)
            }
        }

        override fun getByTimestampRange(
            startTimestamp: Long,
            endTimestamp: Long
        ): Flow<List<TimestampedBatteryTemp>> {
            return flow { emptyList<TimestampedBatteryTemp>() }
        }

        override fun getAllByLocalDate(localDate: LocalDate): Flow<List<TimestampedBatteryTemp>> {
            return flow { emptyList<TimestampedBatteryTemp>() }
        }
    }


    lateinit var buffer: TimestampedBatteryTempBuffer
    lateinit var mockDao: MockDao
    val conflictingBufferInsertionList = DbFillData.conflictingBufferInsertionList
    val expectedNonConflictingList = DbFillData.expectedNonConflictingList

    @Before
    fun setup() {
        mockDao = MockDao()
        buffer = TimestampedBatteryTempBuffer(mockDao)
    }

    fun listAssertion(
        expectedList: List<TimestampedBatteryTemp>,
        resultList: List<TimestampedBatteryTemp>
    ) {
        val expectedListFinal = expectedList.sortedBy { it.timestamp }
        val resultListFinal = resultList.sortedBy { it.timestamp }

        TestCase.assertEquals(expectedListFinal.size, resultListFinal.size)
        for(i in 0..<expectedList.size) {
            TestCase.assertEquals(expectedListFinal[i].timestamp, resultListFinal[i].timestamp)
            TestCase.assertEquals(expectedListFinal[i].temperature, resultListFinal[i].temperature)
        }
    }

    fun setBufferNonEmptyAndValidate(insertionList: List<TimestampedBatteryTemp>) {
        val beforeInsertionBuffer = runBlocking { buffer.getBuffer() }
        assert(beforeInsertionBuffer.isEmpty())

        insertToBuffer(insertionList)

        val bufferAfterInsertion = runBlocking { buffer.getBuffer() }
        assert(bufferAfterInsertion.isNotEmpty())

        val expectedList = insertionList
        val resultList = bufferAfterInsertion

        listAssertion(expectedList, resultList)
    }

    fun setDbNonEmptyAndValidate(insertionList: List<TimestampedBatteryTemp>) {
        assert(insertionList.isNotEmpty())

        runBlocking { mockDao.insertAll(insertionList) }

        val afterInsertDbb = runBlocking { mockDao.getAll() }
        assert(afterInsertDbb.isNotEmpty())

        val expectedList = insertionList
        val resultList = afterInsertDbb

        listAssertion(expectedList, resultList)
    }

    fun isListHaveConflictingMembers(list: List<TimestampedBatteryTemp>): Boolean {
        val listSorted = list.sortedBy { it.timestamp }

        var isConflicting = false
        for(i in 1 until list.size) {
            if(listSorted[i-1].timestamp == listSorted[i].timestamp) {
                isConflicting = true
                break
            }
        }

        return isConflicting
    }

    fun assertBufferEmpty() {
        val result = runBlocking { buffer.getBuffer() }
        assert(result.isEmpty())
    }

    fun assertDbEmpty() {
        val result = runBlocking { mockDao.getAll() }
        assert(result.isEmpty())
    }

    fun insertToBuffer(list: List<TimestampedBatteryTemp>) {
        runBlocking {
            list.forEach {
                buffer.addItemToBuffer(it)
            }
        }
    }





    // getBuffer

    @Test
    fun getBuffer___noInsertion___emptyListOutput() {
        assertBufferEmpty()
    }

    @Test
    fun getBuffer___someInsertions___correctOutput() {
        assertBufferEmpty()

        val inputList = DbFillData.jan1st1970List
        insertToBuffer(inputList)

        val bufferAfterInsertion = runBlocking { buffer.getBuffer() }
        assert(bufferAfterInsertion.isNotEmpty())

        val result = bufferAfterInsertion
        val expected = bufferAfterInsertion

        listAssertion(expected, result)
    }






    // clearBuffer

    @Test
    fun clearBuffer___emptyBuffer___noExceptionAndBufferRemainsEmpty() {
        assertBufferEmpty()

        runBlocking { buffer.clearBuffer() }

        assertBufferEmpty()
    }

    @Test
    fun clearBuffer___nonEmptyBuffer___emptyListOutput() {
        assertBufferEmpty()

        setBufferNonEmptyAndValidate(DbFillData.jan1st1970List)

        runBlocking { buffer.clearBuffer() }

        assertBufferEmpty()
    }






    // flushBuffer

    @Test
    fun flushBuffer___emptyBufferAndEmptyDb___emptyBufferAndEmptyDbAndNoException() {
        assertBufferEmpty()
        assertDbEmpty()

        runBlocking { buffer.flushBuffer() }

        assertBufferEmpty()
        assertDbEmpty()
    }

    @Test
    fun flushBuffer___nonEmptyBufferAndEmptyDb___emptyBufferAndNonEmptyDbAndNoException() {
        val bufferBeforeFlushInsertionList = DbFillData.jan1st1970List

        assertBufferEmpty()
        setBufferNonEmptyAndValidate(bufferBeforeFlushInsertionList)
        assertDbEmpty()
        runBlocking { buffer.flushBuffer() }
        assertBufferEmpty()

        val afterFlushDb = runBlocking { mockDao.getAll() }

        val expectedList = bufferBeforeFlushInsertionList
        val resultList = afterFlushDb

        listAssertion(expectedList, resultList)
    }


    @Test
    fun flushBuffer___emptyBufferAndNonEmptyDb___emptyBufferAndUnchangedDbAndNoException() {
        val dbBeforeFlushInsertionList = DbFillData.jan2nd1970List

        assertBufferEmpty()
        setDbNonEmptyAndValidate(dbBeforeFlushInsertionList)
        runBlocking { buffer.flushBuffer() }
        assertBufferEmpty()

        val afterFlushDb = runBlocking { mockDao.getAll() }
        assert(afterFlushDb.isNotEmpty())

        val resultList = afterFlushDb
        val expectedList = dbBeforeFlushInsertionList

        listAssertion(expectedList, resultList)
    }


    @Test
    fun flushBuffer___nonEmptyBufferAndNonEmptyDb___emptyBufferAndCorrectDbData() {
        val bufferBeforeFlushInsertionList = DbFillData.jan1st1970List
        val dbBeforeFlushInsertionList = DbFillData.jan2nd1970List


        assertBufferEmpty()
        setBufferNonEmptyAndValidate(bufferBeforeFlushInsertionList)
        assertDbEmpty()
        setDbNonEmptyAndValidate(dbBeforeFlushInsertionList)
        runBlocking { buffer.flushBuffer() }
        assertBufferEmpty()

        val afterFlushDb = runBlocking { mockDao.getAll() }
        assert(afterFlushDb.isNotEmpty())

        val expectedList = bufferBeforeFlushInsertionList + dbBeforeFlushInsertionList
        val resultList = afterFlushDb

        listAssertion(expectedList, resultList)
    }





    // addItemToBuffer

    @Test
    fun addItemToBuffer___emptyBufferAndNonConflictingInsertion___fullInsertionListAsBufferData() {
        val bufferNonConflictingInsertionList = DbFillData.jan2nd1970List


        assertBufferEmpty()
        assert(bufferNonConflictingInsertionList.isNotEmpty() && !isListHaveConflictingMembers((bufferNonConflictingInsertionList)))
        insertToBuffer(bufferNonConflictingInsertionList)


        val afterInsertBuffer = runBlocking { buffer.getBuffer() }
        assert(afterInsertBuffer.isNotEmpty())


        val expectedList = bufferNonConflictingInsertionList
        val resultList = afterInsertBuffer

        listAssertion(expectedList, resultList)
    }


    @Test
    fun addItemToBuffer___emptyBufferAndConflictingInsertion___sameTimestampsMergedIntoSingleValue() {
        val bufferConflictingInsertList = conflictingBufferInsertionList


        assertBufferEmpty()
        assert(bufferConflictingInsertList.isNotEmpty() && isListHaveConflictingMembers((bufferConflictingInsertList)))
        insertToBuffer(bufferConflictingInsertList)

        val bufferAfterInsert = runBlocking { buffer.getBuffer() }
        assert(bufferAfterInsert.isNotEmpty())

        val expectedList = expectedNonConflictingList
        val resultList = bufferAfterInsert

        listAssertion(expectedList, resultList)
    }


    @Test
    fun addItemToBuffer___nonEmptyBufferAndNonConflictingInsertion___fullInsertionListItemsAppendedToBuffer() {
        val bufferInsertionList = DbFillData.jan1st1970List
        val bufferNonConflictingInsertionList = DbFillData.jan2nd1970List


        assertBufferEmpty()
        setBufferNonEmptyAndValidate(bufferInsertionList)
        assert(bufferNonConflictingInsertionList.isNotEmpty() && !isListHaveConflictingMembers((bufferNonConflictingInsertionList)))
        insertToBuffer(bufferNonConflictingInsertionList)


        val afterNonConflictingInsertionBuffer = runBlocking { buffer.getBuffer() }
        assert(afterNonConflictingInsertionBuffer.isNotEmpty())

        val expectedList = bufferInsertionList + bufferNonConflictingInsertionList
        val resultList = afterNonConflictingInsertionBuffer

        listAssertion(expectedList, resultList)
    }

    @Test
    fun addItemToBuffer___nonEmptyBufferAndConflictingInsertion___sameTimestampMergedIntoSingleValueListAppendedToDbData() {
        val bufferInsertionList = DbFillData.jan1st1970List
        val bufferConflictingInsertList = conflictingBufferInsertionList


        assertBufferEmpty()
        setBufferNonEmptyAndValidate(bufferInsertionList)
        assert(bufferConflictingInsertList.isNotEmpty() && isListHaveConflictingMembers((bufferConflictingInsertList)))
        insertToBuffer(bufferConflictingInsertList)


        val bufferAfterConflictingInsertion = runBlocking { buffer.getBuffer() }
        assert(bufferAfterConflictingInsertion.isNotEmpty())

        val expectedList = bufferInsertionList + expectedNonConflictingList
        val resultList = bufferAfterConflictingInsertion

        listAssertion(expectedList, resultList)
    }
}