package com.bytemanager.stats.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

@Composable
fun Settings(){
    Column {
        ExportForm()
        ImportForm()
    }
}