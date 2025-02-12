package com.agile.officepool.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextWithLines(text:String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Line before the text
        Divider(
            color = Color.Gray,
            modifier = Modifier
                .weight(1f) // Take up remaining space
                .padding(end = 8.dp), // Add spacing between line and text
            thickness = 1.dp
        )

        // Text
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.inverseSurface
        )

        // Line after the text
        Divider(
            color = Color.Gray,
            modifier = Modifier
                .weight(1f) // Take up remaining space
                .padding(start = 8.dp), // Add spacing between line and text
            thickness = 1.dp
        )
    }
}