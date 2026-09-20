package com.bytemanager.stats.ui.graphs

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.bytemanager.stats.R
import com.bytemanager.stats.data_structure.IntervalWiseBatteryTempMinMax
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.candlestickModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState

@Composable
fun DayWiseBatteryTempGraph(
    intervalSize: Int,
    minMaxList: List<IntervalWiseBatteryTempMinMax>
) {
    Log.d("DEBUGGING LOGS", "Recomposing DayWiseBatteryTempGraph")

    if(minMaxList.isNotEmpty()) {
        Log.d("DEBUGGING LOGS", "$minMaxList")

        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(minMaxList) {
            modelProducer.runTransaction {
                candlestickModel(
                    x = minMaxList.map { it.intervalIndex },
                    opening = minMaxList.map { it.minTemperature },
                    closing = minMaxList.map{ it.maxTemperature },
                    low = minMaxList.map { it.minTemperature },
                    high = minMaxList.map{ it.maxTemperature },
                )
            }
        }

        val minY = minMaxList.minBy { it.minTemperature }.minTemperature
        val maxY = minMaxList.maxBy { it.maxTemperature }.maxTemperature

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberCandlestickCartesianLayer(
                    rangeProvider = CartesianLayerRangeProvider.fixed(
                        minX = 0.0,
                        maxX = 24.0,
                        minY = minY - 3.0,
                        maxY = maxY + 3.0
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                endAxis = VerticalAxis.rememberEnd(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = {_, value, _ ->
                        "${value.toInt()}h"
                    }
                ),
                getXStep = { _, _, _ ->
                    1.0
                }
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(),
            modifier = Modifier.testTag("graph_img")
        )
    } else {
        Text(
            text = stringResource(R.string.no_data_found),
            modifier = Modifier.testTag("no_data_found_label")
        )
    }
}