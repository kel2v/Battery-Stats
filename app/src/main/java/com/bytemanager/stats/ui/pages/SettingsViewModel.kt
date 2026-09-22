package com.bytemanager.stats.ui.pages

import android.util.Log
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.getSelectedEndDate
import androidx.compose.material3.getSelectedStartDate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemanager.stats.data.repository.BatteryTempHistoryRepository
import com.bytemanager.stats.data.types.TimestampInterval
import com.bytemanager.stats.export.ExportCsv
import com.bytemanager.stats.utils.StatsTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportCsv: ExportCsv,
    private val batteryTempHistoryRepository: BatteryTempHistoryRepository
): ViewModel() {
    private val _exportDataButtonClicked: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
    val exportDataButtonClicked = _exportDataButtonClicked.asStateFlow()

    private val _exportFromDateRangeOptionClicked: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
    val exportFromDateRangeOptionClicked = _exportFromDateRangeOptionClicked.asStateFlow()

    private val _exportAllDataOptionClicked: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
    val exportAllDataOptionClicked = _exportAllDataOptionClicked.asStateFlow()

    private val _timestampInterval: MutableStateFlow<TimestampInterval> = MutableStateFlow<TimestampInterval>(TimestampInterval(0, 0))
    val timestampInterval = _timestampInterval.asStateFlow()

    fun unclickExportDataButton() {
        _exportDataButtonClicked.update {
            false
        }
    }

    fun clickExportDataButton() {
        _exportDataButtonClicked.update {
            true
        }
    }

    fun clickExportAllDataOption() {
        _exportAllDataOptionClicked.update {
            true
        }
    }

    fun unclickExportAllDataOption() {
        _exportAllDataOptionClicked.update {
            false
        }
    }

    fun clickExportFromDateRangeOption() {
        _exportFromDateRangeOptionClicked.update {
            true
        }
    }

    fun unclickExportFromDateRangeOption() {
        _exportFromDateRangeOptionClicked.update {
            false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun extractTimestampInterval(
        dateRangePickerState: DateRangePickerState
    ) {
        val startLocalDate = dateRangePickerState.getSelectedStartDate()
        val endLocalDate = dateRangePickerState.getSelectedEndDate()

        Log.d("DEBUGGING LOGS", "Extracted date range = $startLocalDate and $endLocalDate")

        if(startLocalDate != null && endLocalDate != null) {
            val statsTime = StatsTime()
            val extractedTimestampInterval = TimestampInterval(
                statsTime.localDateToStartOfTheDayInEpochSeconds(startLocalDate),
                statsTime.localDateToStartOfTheDayInEpochSeconds(endLocalDate) + 86400
            )

            _timestampInterval.update {
                extractedTimestampInterval
            }

            Log.d("DEBUGGING LOGS", "extractedTimestampInterval = $extractedTimestampInterval")
        } else {
            _timestampInterval.update {
                TimestampInterval(0, 0)
            }
            Log.d("DEBUGGING LOGS", "Failed to extract timestampInterval.")
        }
    }

    suspend fun exportAll() {

        val tempHistoryList = viewModelScope.async { batteryTempHistoryRepository.dbDao.getAll() }

        val uri = exportCsv.createUri() ?: throw (Exception("exportAll(): null URI"))
        exportCsv.exportBatteryTempHistoryToCsv(
            uri,
            tempHistoryList.await()
        )
    }

    suspend fun exportDataFromTimestampInterval(
        timestampInterval: TimestampInterval
    ) {
        val tempHistoryListFlow = batteryTempHistoryRepository.dbDao.getByTimestampRange(timestampInterval.startTimestamp, timestampInterval.endTimestamp)
        val tempHistoryList = tempHistoryListFlow.first()

        val statsTime = StatsTime()
        val startLocalDate = statsTime.millisToLocalDate(timestampInterval.startTimestamp * 1000)
        val endLocalDate = statsTime.millisToLocalDate(((timestampInterval.endTimestamp - 1) * 1000))

        val uri = exportCsv.createUri("${startLocalDate}__${endLocalDate}__${Instant.now().epochSecond}__BatTempHistory") ?: throw (Exception("exportAll(): null URI"))
        exportCsv.exportBatteryTempHistoryToCsv(
            uri,
            tempHistoryList
        )
    }
}