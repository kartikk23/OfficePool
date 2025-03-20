package com.agile.officepool.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agile.officepool.ViewModel.UserViewModel
import com.agile.officepool.network.ApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailCard(
    rideId: String,
    title: String,
    date: String,
    time: String,
    location: String,
    status: String,
    viewModel: ApiService
) {
    var currentStatus =  listOf("Active", "Completed", "Cancelled", "Yet To Start")
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Ride Icon",
                    tint = Color.Blue,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("$date | $time", fontSize = 14.sp, color = Color.Gray)
                    Text("Location: $location", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Text(
                    text = "Status: $currentStatus",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (currentStatus) {
                        listOf("Active") -> Color.Yellow
                        listOf("Completed") -> Color.Yellow
                        listOf("Cancelled") -> Color.Green
                        listOf("Yet To Start") -> Color.Blue
                        else -> Color.Gray
                    },
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Row {
                    // Update FAB
                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        containerColor = Color.Green,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Update", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Delete FAB
                    FloatingActionButton(
                        onClick = { showDeleteDialog = true },
                        containerColor = Color.Red,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }
        }
    }

    // Bottom Sheet for Status Update
    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Update Ride Status", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                listOf("Pending", "Confirmed", "Completed").forEach { option ->
                    TextButton(
                        onClick = {
//                            viewModel.updateRide(rideId, option) { success, _ ->
//                                if (success) {
//                                    currentStatus = option
//                                    showBottomSheet = false
//                                }
//                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Delete
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ride") },
            text = { Text("Are you sure you want to delete this ride? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
//                    viewModel.deleteRide(rideId) { success, _ ->
//                        if (success) {
//                            showDeleteDialog = false
//                        }
//                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
