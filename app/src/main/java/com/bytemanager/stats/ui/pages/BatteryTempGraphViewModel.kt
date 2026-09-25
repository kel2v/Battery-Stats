package com.bytemanager.stats.ui.pages

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemanager.stats.data.testdata.DenseList_0_86400
import com.bytemanager.stats.data.testdata.DenseList_172800_259200
import com.bytemanager.stats.data.testdata.DenseList_86400_172800
import com.bytemanager.stats.data.types.IntervalWiseBatteryTempMinMax
import com.bytemanager.stats.data.types.GraphType
import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.utils.IntervalWiseMinMax
import com.bytemanager.stats.utils.StatsTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BatteryTempGraphViewModel  @Inject constructor(
    batteryTempHistoryRepository: BatteryTempHistoryRepositoryInterface
): ViewModel() {
    private val dbDao = batteryTempHistoryRepository.dbDao
    var intervalSize = mutableIntStateOf(60 * 60)
        private set

    private val _dateSelected = MutableStateFlow<LocalDate>(StatsTime().today())
    val dateSelected: StateFlow<LocalDate> = _dateSelected.asStateFlow()

//    init {
//        runBlocking {
//            dbDao.deleteAll()
//            dbDao.insertAll(DenseList_0_86400.list + DenseList_86400_172800.list + DenseList_172800_259200.list)
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val intervalWiseTempMinMaxStateFlow: StateFlow<List<IntervalWiseBatteryTempMinMax>> = dateSelected
        .flatMapLatest { date ->
            dbDao.getAllByLocalDate(date)
        }
        .map { dayHistoryList ->
            Log.d("DEBUGGING LOGS", "dayHistoryList size = ${dayHistoryList.size}")
            IntervalWiseMinMax.getIntervalWiseMinMaxDataList(
                list = dayHistoryList,
                dateSelected = dateSelected.value,
                intervalSize = intervalSize.intValue
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )







    var graphTypeSelected: MutableState<GraphType> = mutableStateOf(GraphType.DAY_GRAPH)
        private set
    var isSelectGraphTypeButtonClicked: MutableState<Boolean> = mutableStateOf(false)

    private fun toggleIsSelectGraphTypeButtonClicked() {
        isSelectGraphTypeButtonClicked.value = !isSelectGraphTypeButtonClicked.value
    }

    fun setIsSelectGraphTypeButtonClicked(value: Boolean){
        isSelectGraphTypeButtonClicked.value = value
    }

    fun onSelectGraphTypeButtonClicked() {
        toggleIsSelectGraphTypeButtonClicked()
    }

    fun onClickSelectGraphDismissRequest() {
        setIsSelectGraphTypeButtonClicked(false)
    }

    fun onClickLiveGraph() {
        graphTypeSelected.value = GraphType.LIVE_GRAPH
        setIsSelectGraphTypeButtonClicked(false)
    }

    fun onClickDayGraph() {
        graphTypeSelected.value = GraphType.DAY_GRAPH
        setIsSelectGraphTypeButtonClicked(false)
    }

    fun onClickWeekGraph() {
        graphTypeSelected.value = GraphType.WEEK_GRAPH
        setIsSelectGraphTypeButtonClicked(false)
    }

    fun onClickMonthGraph() {
        graphTypeSelected.value = GraphType.MONTH_GRAPH
        setIsSelectGraphTypeButtonClicked(false)
    }

    fun onClickYearGraph() {
        graphTypeSelected.value = GraphType.YEAR_GRAPH
        setIsSelectGraphTypeButtonClicked(false)
    }













    var isSelectDateButtonClicked = mutableStateOf(false)
    private fun toggleSelectDateButton() {
        isSelectDateButtonClicked.value = !isSelectDateButtonClicked.value
        Log.d("DEBUGGING LOGS", "toggleSelectDateButton(): isSelectDateClicked = ${isSelectDateButtonClicked.value}")
    }

    fun setIsSelectDateClicked(value: Boolean) {
        isSelectDateButtonClicked.value = value
        Log.d("DEBUGGING LOGS", "setSelectDate(): isSelectDateClicked = ${isSelectDateButtonClicked.value}")
    }

    fun onSelectDateButtonClicked() {
        toggleSelectDateButton()
    }



    fun onDatePickerDismissRequest() {
        setIsSelectDateClicked(false)
    }

    fun onDatePickerDismissButtonClick() {
        setIsSelectDateClicked(false)
    }

    fun onDatePickerConfirmButtonClick(millis: Long?) {
        setIsSelectDateClicked(false)
        extractSelectedDate(millis)
    }



    private fun extractSelectedDate(millis: Long?) {
        _dateSelected.update {
            StatsTime().millisToLocalDate(millis) ?: StatsTime().today()
        }
        Log.d("DEBUGGING LOGS", "millis: $millis, selectedDate = ${dateSelected.value}")
    }


    val minTemperature = batteryTempHistoryRepository.dbDao.minTemperature().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Float.POSITIVE_INFINITY
    )

    val maxTemperature = batteryTempHistoryRepository.dbDao.maxTemperature().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Float.NEGATIVE_INFINITY
    )
}