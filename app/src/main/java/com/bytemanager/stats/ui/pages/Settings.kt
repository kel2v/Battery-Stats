package com.bytemanager.stats.ui.pages

import androidx.compose.foundation.layout.Column
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
import com.bytemanager.stats.data.types.TimestampInterval

@Composable
fun Settings(
    settingsViewModel: SettingsViewModel = hiltViewModel()
){
    val exportButtonClicked by settingsViewModel.exportDataButtonClicked.collectAsState()
    val exportFromDateRangeOptionClicked by settingsViewModel.exportFromDateRangeOptionClicked.collectAsState()
    val exportAllDataOptionClicked by settingsViewModel.exportAllDataOptionClicked.collectAsState()
    val timestampInterval by settingsViewModel.timestampInterval.collectAsState()

    LaunchedEffect(exportAllDataOptionClicked, timestampInterval) {
        if(exportAllDataOptionClicked) {
            settingsViewModel.exportAll()
            settingsViewModel.unclickExportAllDataOption()
            settingsViewModel.unclickExportDataButton()
        }

        if(timestampInterval != TimestampInterval(0, 0)) {
            settingsViewModel.exportDataFromTimestampInterval(timestampInterval)
            settingsViewModel.unclickExportFromDateRangeOption()
            settingsViewModel.unclickExportDataButton()
        }
    }

    val dateRangePickerState = rememberDateRangePickerState()

    Column {
        Button(
            enabled = !exportButtonClicked,
            onClick = {
                settingsViewModel.clickExportDataButton()
            }
        ) {
            Text("Export battery-temp history")
        }

        DropdownMenu(
            expanded = exportButtonClicked,
            onDismissRequest = {
                settingsViewModel.unclickExportDataButton()
            },
            scrollState = rememberScrollState()
        ) {
            DropdownMenuItem(
                text = {
                    Text("Export all data")
                },
                onClick = {
                    settingsViewModel.clickExportAllDataOption()
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Export from date range")
                },
                onClick = {
                    settingsViewModel.clickExportFromDateRangeOption()
                }
            )
        }

        if(exportFromDateRangeOptionClicked) {
            DatePickerDialog(
                onDismissRequest = {
                    settingsViewModel.unclickExportFromDateRangeOption()
                },

                confirmButton = {
                    Button(
                        enabled = true,
                        onClick = {
                            settingsViewModel.extractTimestampInterval(dateRangePickerState)
                        }
                    ) {
                        Text("Confirm")
                    }
                },

                dismissButton = {
                    Button(
                        enabled = true,
                        onClick = {
                            settingsViewModel.unclickExportFromDateRangeOption()
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState
                )
            }
        }

    }
}