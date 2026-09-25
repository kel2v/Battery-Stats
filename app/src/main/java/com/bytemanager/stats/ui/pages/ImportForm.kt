package com.bytemanager.stats.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ImportForm(
    importFormViewModel: ImportFormViewModel = hiltViewModel()
) {
    val importFormUiState by importFormViewModel.importFormUiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if(uri == null) {
            importFormViewModel.onInvalidUriReturn()
        } else {
            importFormViewModel.onValidUriReturn(uri)
        }
    }

    LaunchedEffect(importFormUiState.pendingImportAction) {
        if(importFormUiState.pendingImportAction != null && importFormUiState.pendingImportAction!!.uri != null) {
            importFormViewModel.processPendingImportAction(importFormUiState.pendingImportAction!!)
            importFormViewModel.resetUiState()
        }
    }
    Button(
        enabled = !importFormUiState.importButtonClickedIn,
        onClick = {
            importFormViewModel.clickImportButton()
        }
    ) {
        Text("Import battery-temp history")
    }

    DropdownMenu(
        expanded = importFormUiState.importButtonClickedIn,

        onDismissRequest = {
            importFormViewModel.processDropdownMenuDismissRequest()
        },

        scrollState = rememberScrollState()
    ) {
        DropdownMenuItem(
            text = {
                Text("Merge into existing database")
            },

            onClick = {
                importFormViewModel.clickMergeOption()
            }
        )

        DropdownMenuItem(
            text = {
                Text("Replace existing database")
            },

            onClick = {
                importFormViewModel.clickReplaceOption()
            }
        )
    }

    LaunchedEffect(importFormUiState.filePickerDialogEnabled) {
        if(importFormUiState.filePickerDialogEnabled) {
            launcher.launch("*/*")
        }
    }
}