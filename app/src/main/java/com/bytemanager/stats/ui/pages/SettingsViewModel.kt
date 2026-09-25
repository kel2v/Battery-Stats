package com.bytemanager.stats.ui.pages

import androidx.lifecycle.ViewModel
import com.bytemanager.stats.data.repository.BatteryTempHistoryRepository
import com.bytemanager.stats.importexport.ExportCsv
import com.bytemanager.stats.importexport.ImportCsv
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val importCsv: ImportCsv,
    private val exportCsv: ExportCsv,
    private val batteryTempHistoryRepository: BatteryTempHistoryRepository
): ViewModel() {
}