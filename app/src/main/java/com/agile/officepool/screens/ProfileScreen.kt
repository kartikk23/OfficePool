package com.agile.officepool.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agile.officepool.network.SessionManager



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, context: Context) {
    val sessionManager = remember { SessionManager(context) }
    var isRiderMode by remember { mutableStateOf(sessionManager.isRiderMode()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.padding(10.dp,40.dp),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(60.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Profile Name
            Text("Tejas Katke", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // Mobile Number
            Text("+91 9876543210", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))

//            Divider(color = Color.LightGray, thickness = 1.dp)
//
//            Spacer(modifier = Modifier.height(10.dp))

            // Mode Switch (Rider / Passenger)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isRiderMode) "Rider Mode" else "Passenger Mode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isRiderMode,
                    onCheckedChange = {
                        isRiderMode = it
                        if (it) {
                            navController.navigate("dashboard") // Navigate to Rider screen
                        } else {
                            navController.navigate("home") // Navigate to Home screen
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileOption(Icons.Default.Star, "Rider Rating", "4.8 ⭐")
            ProfileOption(Icons.Default.Phone, "Help", "Get support")
            ProfileOption(Icons.Filled.Check, "Payment", "Manage payments")
            ProfileOption(Icons.Default.LocationOn, "My Rides", "View ride history")
            ProfileOption(Icons.Default.Info, "Safety", "Safety preferences")
            ProfileOption(Icons.Default.Notifications, "Notifications", "Manage alerts")
            ProfileOption(Icons.Default.Settings, "Settings", "Account settings")

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { /* Logout Functionality */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Log Out")
            }
        }
    }
}

// Profile Option with Icon
@Composable
fun ProfileOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        }
    }
}
