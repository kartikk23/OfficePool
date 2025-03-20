package com.agile.officepool.rider

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agile.officepool.model.PassengerRideViewModel
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var rideList by remember { mutableStateOf<List<RideInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var rideListForPassenger by remember { mutableStateOf<List<RideInfo>>(emptyList()) }


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.getRideByStatus("Yet To Start")
                if (response.isSuccessful) {
                    rideList = response.body() ?: emptyList()

                     

                } else {
                    Toast.makeText(context, "Failed to fetch rides", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ride Requests") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
//            items(rideListForPassenger) { request ->
//                RideRequestCard(request) {
//                    navController.navigate("currentRide")
//                }
//            }
        }
    }
}



//@Composable
//fun RideRequestCard(request: RideRequest, onAccept: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = "Passenger: ${request.passengerName}")
//            Text(text = "From: ${request.source} To: ${request.destination}")
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Button(onClick = onAccept) {
//                    Text("Accept")
//                }
//                Button(onClick = { /* Handle Reject */ }) {
//                    Text("Reject")
//                }
//            }
//        }
//    }
//}
