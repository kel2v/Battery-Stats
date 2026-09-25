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
import com.bytemanager.stats.importexport.ExportCsv
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
class ExportFormViewModel @Inject constructor(
    private val exportCsv: ExportCsv,
    private val batteryTempHistoryRepository: BatteryTempHistoryRepository
): ViewModel() {
    private val _exportFormUiState: MutableStateFlow<ExportFormUiState> = MutableStateFlow(ExportFormUiState())
    val exportFormUiState = _exportFormUiState.asStateFlow()


    // ============= Reset Export Form Ui State ===========

    fun resetExportFormUiState() {
        _exportFormUiState.update {
            ExportFormUiState()
        }
    }



    // ===== Actions performing state change: (F, F, F) -> (T, T, F) =====

    fun clickExportButton() {
        _exportFormUiState.update { current ->
            current.copy(
                exportButtonClickedIn = true,
                dropdownMenuExpanded = true,
                datePickerDialogEnabled = false
            )
        }
    }




    // ===== Actions performing state change: (T, T, F) -> (F, F, F) =====

    fun processDropdownMenuDismissRequest() {
        _exportFormUiState.update { current ->
            current.copy(
                exportButtonClickedIn = false,
                dropdownMenuExpanded = false,
                datePickerDialogEnabled = false
            )
        }
    }

    fun clickExportAllOption() {
        _exportFormUiState.update { current ->
            current.copy(
                exportButtonClickedIn = false,
                dropdownMenuExpanded = false,
                datePickerDialogEnabled = false,

                exportPendingAction = ExportPendingAction()
            )
        }
    }



    // ===== Actions performing state change: (T, T, F) -> (T, F, T) =====

    fun clickExportFromDateRangeOption() {
        _exportFormUiState.update { current ->
            current.copy(
                exportButtonClickedIn = true,
                dropdownMenuExpanded = false,
                datePickerDialogEnabled = true,
            )
        }
    }




    // ===== Actions performing state change: (T, F, T) -> (T, T, F) =====

    fun processDatePickerDialogDismissRequest() {
        _exportFormUiState.update { current ->
            current.copy(
                exportButtonClickedIn = true,
                dropdownMenuExpanded = true,
                datePickerDialogEnabled = false,
            )
        }
    }




    // ===== Actions performing state change: (T, F, T) -> (F, F, F) =====

    fun clickDatePickerDialogConfirmButton(
        dateRangePickerState: DateRangePickerState
    ) {
        val extractedTimestampInterval = extractTimestampInterval(dateRangePickerState)

        _exportFormUiState.update { current ->
            current.copy(
                exportButtonClickedIn = false,
                dropdownMenuExpanded = false,
                datePickerDialogEnabled = false,

                exportPendingAction = ExportPendingAction(extractedTimestampInterval),
            )
        }
    }



    // =================== DatePickerDialogConfirmButton ================

    fun enableDatePickerDialogConfirmButton() {
        _exportFormUiState.update { current ->
            current.copy(
                datePickerDialogConfirmButtonEnabled = true
            )
        }
    }

    fun disableDatePickerDialogConfirmButton() {
        _exportFormUiState.update { current ->
            current.copy(
                datePickerDialogConfirmButtonEnabled = false
            )
        }
    }





    // =============== Utility Functions ==================

    @OptIn(ExperimentalMaterial3Api::class)
    fun isValidDateRange(
        dateRangePickerState: DateRangePickerState
    ): Boolean {
        val startLocalDate = dateRangePickerState.getSelectedStartDate()
        val endLocalDate = dateRangePickerState.getSelectedEndDate()

        return (startLocalDate != null && endLocalDate != null)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun extractTimestampInterval(
        dateRangePickerState: DateRangePickerState
    ): TimestampInterval? {
        val startLocalDate = dateRangePickerState.getSelectedStartDate()
        val endLocalDate = dateRangePickerState.getSelectedEndDate()

        Log.d("DEBUGGING LOGS", "Extracted date range = $startLocalDate and $endLocalDate")

        var extractedTimestampInterval: TimestampInterval? = null
        if(startLocalDate != null && endLocalDate != null) {
            val statsTime = StatsTime()
            extractedTimestampInterval = TimestampInterval(
                statsTime.localDateToStartOfTheDayInEpochSeconds(startLocalDate),
                statsTime.localDateToStartOfTheDayInEpochSeconds(endLocalDate) + 86400
            )

            Log.d("DEBUGGING LOGS", "extractedTimestampInterval = $extractedTimestampInterval")
        } else {
            Log.d("DEBUGGING LOGS", "Failed to extract timestampInterval.")
        }

        return extractedTimestampInterval
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

data class ExportFormUiState(
    val exportButtonClickedIn: Boolean = false,
    val dropdownMenuExpanded: Boolean = false,
    val datePickerDialogEnabled: Boolean = false,
    val datePickerDialogConfirmButtonEnabled: Boolean = false,
    val exportPendingAction: ExportPendingAction? = null,
)

data class ExportPendingAction(
    val timestampInterval: TimestampInterval? = null
)