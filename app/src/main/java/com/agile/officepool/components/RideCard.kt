package com.agile.officepool.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agile.officepool.model.RideInfo


@Composable
fun RideCard(ride: RideInfo) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ride ID: ${ride.riderId}", style = MaterialTheme.typography.bodyLarge)
            Text("Source: ${ride.source}")
            Text("Destination: ${ride.destination}")
            Text("Route: ${ride.route}")
            Text("Status: ${ride.status}", color = when (ride.status) {
                "Active" -> MaterialTheme.colorScheme.primary
                "Completed" -> MaterialTheme.colorScheme.secondary
                "Cancelled" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            })
//            Text("Date & Time: ${ride.dateTime}")
//            Text("Passenger ID: ${ride.passengerId}")
        }
    }
}