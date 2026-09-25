package com.bytemanager.stats.ui.pages

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bytemanager.stats.data.repository.BatteryTempHistoryRepository
import com.bytemanager.stats.importexport.ImportCsv
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImportFormViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val importCsv: ImportCsv,
    private val batteryTempHistoryRepository: BatteryTempHistoryRepository
): ViewModel() {
    private val _importFormUiState: MutableStateFlow<ImportFormUiState> = MutableStateFlow(ImportFormUiState())
    val importFormUiState = _importFormUiState.asStateFlow()


    // ========== reset ImportForm Ui State ============

    fun resetUiState() {
        _importFormUiState.update {
            ImportFormUiState()
        }
    }



    // ======== Actions performing state change (F, F) -> (T, F) =========

    fun clickImportButton() {
        _importFormUiState.update { current ->
            current.copy(
                importButtonClickedIn = true,
                filePickerDialogEnabled = false
            )
        }
    }



    // ===== Actions performing state change (T, F) -> (F, F) ========

    fun processDropdownMenuDismissRequest() {
        _importFormUiState.update { current ->
            current.copy(
                importButtonClickedIn = false,
                filePickerDialogEnabled = false
            )
        }
    }



    // ======= Actions performing state change (T, F) -> (T, T) ========

    fun clickMergeOption() {
        _importFormUiState.update { current ->
            current.copy(
                importButtonClickedIn = true,
                filePickerDialogEnabled = true,

                pendingImportAction = PendingImportAction(
                    importActionType = ImportActionType.MERGE,
                    uri = null
                )
            )
        }
    }

    fun clickReplaceOption() {
        _importFormUiState.update { current ->
            current.copy(
                importButtonClickedIn = true,
                filePickerDialogEnabled = true,

                pendingImportAction = PendingImportAction(
                    importActionType = ImportActionType.REPLACE,
                    uri = null
                )
            )
        }
    }



    // ======= Actions performing state change (T, T) -> (T, F) =======

    fun onInvalidUriReturn() {
        _importFormUiState.update { current ->
            current.copy(
                importButtonClickedIn = true,
                filePickerDialogEnabled = false,

                pendingImportAction = null
            )
        }
    }



    // ======= Actions performing state change (T, T) -> (F, F) =======

    fun onValidUriReturn(uri: Uri) {
        _importFormUiState.update { current ->
            current.copy(
                importButtonClickedIn = false,
                filePickerDialogEnabled = false,

                pendingImportAction = current.pendingImportAction!!.copy(uri = uri)
            )
        }
    }




    // ======== Utility Functions ========

    suspend fun processPendingImportAction(pendingImportAction: PendingImportAction) {
        Log.d("DEBUGGING LOGS", "before import db count = ${batteryTempHistoryRepository.dbDao.getCount()}")
        val inputStream = appContext.contentResolver.openInputStream(pendingImportAction.uri!!) ?: return

        val importingList = importCsv.extractImportList(inputStream)
        Log.d("DEBUGGING LOGS", "importingList count = ${importingList.size}")
        if(pendingImportAction.importActionType == ImportActionType.MERGE) {
            batteryTempHistoryRepository.dbDao.insertAll(importingList)
            Toast.makeText(appContext, "Merge successful", Toast.LENGTH_LONG).show()
        } else {
            batteryTempHistoryRepository.dbDao.replaceAllItems(importingList)
            Toast.makeText(appContext, "Replace successful", Toast.LENGTH_LONG).show()
        }
        Log.d("DEBUGGING LOGS", "after import db count = ${batteryTempHistoryRepository.dbDao.getCount()}")

        withContext(Dispatchers.IO) {
            inputStream.close()
        }
    }
}

data class ImportFormUiState(
    val importButtonClickedIn: Boolean = false,
    val filePickerDialogEnabled: Boolean = false,

    val pendingImportAction: PendingImportAction? = null,
)

data class PendingImportAction(
    val importActionType: ImportActionType,
    val uri: Uri? = null
)

enum class ImportActionType {
    MERGE,
    REPLACE
}