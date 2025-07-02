package com.agile.officepool.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun ShowDatePicker(context: Context, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(pickedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // You can use a clickable component to show the DatePicker
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { datePickerDialog.show() }
    ) {
        Text(
            text = "Select Date",  // Or your label here
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(8.5f),
                text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))?: "", // Display the formatted date or a default string
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                modifier = Modifier.weight(1.5f),
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
