package com.example.quckdraw

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


@Composable
fun DrawingListView (){
    @Composable
    fun DrawingListView(drawings: List<DrawingData>, onDrawingClick: (DrawingData) -> Unit) {
        LazyColumn {
            items(drawings) { drawing ->
                DrawingItem(drawing = drawing, onClick = { onDrawingClick(drawing) })
            }
        }
    }
}

@Composable
fun DrawingItem(drawing: DrawingData, onClick: () -> Unit) {
    Row(modifier = androidx.compose.ui.Modifier
        .padding(16.dp)
        .fillMaxWidth(),
    ) {
        Text(text = "text")
    }
}