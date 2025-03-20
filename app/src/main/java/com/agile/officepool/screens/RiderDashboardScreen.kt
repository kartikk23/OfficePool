package com.agile.officepool.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.agile.officepool.components.RideCard
import com.agile.officepool.components.TransparentStatusBar
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RiderDashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var rideList by remember { mutableStateOf<List<RideInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    TransparentStatusBar()
    // Fetch Ride History from API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.getAllRides()
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
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp,20.dp,16.dp,16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rider Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        DashboardCard("Add New Ride", Icons.Default.Add, Color.Blue) {
            navController.navigate("riderScreen")
        }

        DashboardCard("Ongoing Ride", Icons.Default.CheckCircle, Color.Red) {}
        DashboardCard("More Options", Icons.Default.Settings, Color.Gray) {}
        DashboardCard("Ride Requests", Icons.Default.List, Color.Green) {
            navController.navigate("rideRequest")
        }


        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(rideList) { ride ->
                    RideCard(ride)
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}