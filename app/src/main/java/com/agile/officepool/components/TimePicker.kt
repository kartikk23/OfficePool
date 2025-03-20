package com.agile.officepool.components

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun TimePicker(
    label: String = "Select Time",
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTime by remember { mutableStateOf("") }

    fun showTimePicker(context: Context) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val isPM = calendar.get(Calendar.AM_PM) == Calendar.PM

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                selectedTime = String.format("%02d:%02d %s", formattedHour, selectedMinute, amPm)
                onTimeSelected(selectedTime)
            },
            if (hour == 0) 12 else hour, minute, false // Use false for 12-hour format
        ).show()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Thin,
            color = Color.LightGray,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Select Time",
            modifier = Modifier
                .size(25.dp)
                .clickable { showTimePicker(context) },
            tint = Color.LightGray
        )
    }
    if (selectedTime.isNotEmpty()) {
        Text(
            text = "Selected Time: $selectedTime",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
