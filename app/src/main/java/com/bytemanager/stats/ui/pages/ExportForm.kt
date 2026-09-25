package com.bytemanager.stats.ui.pages

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ExportForm(
    exportFormViewModel: ExportFormViewModel = hiltViewModel()
) {
    val exportFormUiState by exportFormViewModel.exportFormUiState.collectAsState()
    val dateRangePickerState = rememberDateRangePickerState()

    LaunchedEffect(exportFormUiState.exportPendingAction) {
        if(exportFormUiState.exportPendingAction != null) {
            if(exportFormUiState.exportPendingAction!!.timestampInterval == null) {
                exportFormViewModel.exportAll()
            } else {
                exportFormViewModel.exportDataFromTimestampInterval(exportFormUiState.exportPendingAction!!.timestampInterval!!)
            }

            exportFormViewModel.resetExportFormUiState()
        }
    }

    LaunchedEffect(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        if(exportFormViewModel.isValidDateRange(dateRangePickerState)) {
            exportFormViewModel.enableDatePickerDialogConfirmButton()
        } else {
            exportFormViewModel.disableDatePickerDialogConfirmButton()
        }
    }

    Button(
        enabled = !exportFormUiState.exportButtonClickedIn,
        onClick = {
            exportFormViewModel.clickExportButton()
        },
    ) {
        Text("Export battery-temp history")
    }


    DropdownMenu(
        expanded = exportFormUiState.dropdownMenuExpanded,
        onDismissRequest = {
            exportFormViewModel.processDropdownMenuDismissRequest()
        },
        scrollState = rememberScrollState()
    ) {
        DropdownMenuItem(
            text = {
                Text("Export all data")
            },
            onClick = {
                exportFormViewModel.clickExportAllOption()
            }
        )

        DropdownMenuItem(
            text = {
                Text("Export from date range")
            },
            onClick = {
                exportFormViewModel.clickExportFromDateRangeOption()
            },
        )
    }

    if(exportFormUiState.datePickerDialogEnabled) {
        DatePickerDialog(
            onDismissRequest = {
                exportFormViewModel.processDatePickerDialogDismissRequest()
            },

            dismissButton = {
                Button(
                    enabled = true,
                    onClick = {
                        exportFormViewModel.processDatePickerDialogDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            },

            confirmButton = {
                Button(
                    enabled = exportFormUiState.datePickerDialogConfirmButtonEnabled,
                    onClick = {
                        exportFormViewModel.clickDatePickerDialogConfirmButton(dateRangePickerState)
                    },
                ) {
                    Text("Confirm")
                }
            },
        ) {
            DateRangePicker(
                state = dateRangePickerState
            )
        }
    }
}