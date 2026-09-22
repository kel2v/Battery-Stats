package com.bytemanager.stats.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bytemanager.stats.R
import com.bytemanager.stats.enums.GraphType
import com.bytemanager.stats.ui.graphs.DayWiseBatteryTempGraph
import java.time.LocalDate


@Composable
fun BatteryTempGraph(batteryTempGraphViewModel: BatteryTempGraphViewModel = hiltViewModel()) {
    val datePickerState = rememberDatePickerState()

    val isSelectGraphTypeButtonClicked by remember { batteryTempGraphViewModel.isSelectGraphTypeButtonClicked }
    val isSelectDateButtonClicked by remember { batteryTempGraphViewModel.isSelectDateButtonClicked }


    val date by batteryTempGraphViewModel.dateSelected.collectAsState()
    val graphType by batteryTempGraphViewModel.graphTypeSelected
    val minMaxList by batteryTempGraphViewModel.intervalWiseTempMinMaxStateFlow.collectAsState()
    val intervalSize by batteryTempGraphViewModel.intervalSize

    Column {
        GraphTypeSelectorMenu(
            graphType = graphType,

            isSelectGraphTypeButtonClicked = isSelectGraphTypeButtonClicked,

            onClickSelectGraphType = {
                batteryTempGraphViewModel.onSelectGraphTypeButtonClicked()
            },

            onClickDismissRequest = {
                batteryTempGraphViewModel.onClickSelectGraphDismissRequest()
            },


            onClickLiveGraph = {
                batteryTempGraphViewModel.onClickLiveGraph()
            },
            onClickDayGraph = {
                batteryTempGraphViewModel.onClickDayGraph()
            },
            onClickWeekGraph = {
                batteryTempGraphViewModel.onClickWeekGraph()
            },
            onClickMonthGraph = {
                batteryTempGraphViewModel.onClickMonthGraph()
            },
            onClickYearGraph = {
                batteryTempGraphViewModel.onClickYearGraph()
            }
        )

        if(graphType == GraphType.DAY_GRAPH) {
            DateSelectorMenu(
                datePickerState = datePickerState,
                isSelectDateButtonClicked = isSelectDateButtonClicked,
                date = date,
                onSelectDateButtonClick =  {
                    batteryTempGraphViewModel.onSelectDateButtonClicked()
                },
                onDismissRequest = {
                    batteryTempGraphViewModel.onDatePickerDismissRequest()
                },
                onDismissButtonClick = {
                    batteryTempGraphViewModel.onDatePickerDismissButtonClick()
                },
                onConfirmButtonClick = {
                    batteryTempGraphViewModel.onDatePickerConfirmButtonClick(datePickerState.selectedDateMillis)
                }
            )

            DayWiseBatteryTempGraph(
                intervalSize,
                minMaxList
            )
        }
    }
}

@Composable
fun GraphTypeSelectorMenu(
    graphType: GraphType,

    isSelectGraphTypeButtonClicked: Boolean,
    onClickSelectGraphType: () -> Unit,

    onClickDismissRequest: () -> Unit,


    onClickLiveGraph: () -> Unit,
    onClickDayGraph: () -> Unit,
    onClickWeekGraph: () -> Unit,
    onClickMonthGraph: () -> Unit,
    onClickYearGraph: () -> Unit,
) {
    Row {
        Text(
            text = stringResource(R.string.selected_graph)
        )

        Text(
            text = ": $graphType"
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Button(
            onClick = onClickSelectGraphType
        ) {
            Text(
                text = stringResource(R.string.select_graph_type)
            )
        }

        DropdownMenu(
            expanded = isSelectGraphTypeButtonClicked,
            onDismissRequest = onClickDismissRequest,
            scrollState = rememberScrollState(),
        ) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.live_graph))
                },
                onClick = onClickLiveGraph
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.day_graph))
                },
                onClick = onClickDayGraph
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.week_graph))
                },
                onClick = onClickWeekGraph
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.month_graph))
                },
                onClick = onClickMonthGraph
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.year_graph))
                },
                onClick = onClickYearGraph
            )
        }
    }
}

@Composable
private fun DateSelectorMenu(
    datePickerState: DatePickerState,
    isSelectDateButtonClicked: Boolean,
    date: LocalDate,
    onSelectDateButtonClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onDismissButtonClick: () -> Unit,
    onConfirmButtonClick: () -> Unit
    ) {
    if(!isSelectDateButtonClicked) {
        SelectedDateLabel(
            date = date,
            modifier = Modifier.width(20.dp),
            onSelectDateButtonClick = onSelectDateButtonClick
        )
    } else {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                Button(
                    onClick = onConfirmButtonClick
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissButtonClick
                ) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SelectedDateLabel(
    date: LocalDate,
    modifier: Modifier,
    onSelectDateButtonClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("${stringResource(R.string.date_selected)}: $date")
        Spacer(modifier = modifier)
        Button(
            onClick = onSelectDateButtonClick
        ) {
            Text(stringResource(R.string.select_date))
        }
    }
}