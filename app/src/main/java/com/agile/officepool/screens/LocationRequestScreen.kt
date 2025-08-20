package com.agile.officepool.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.helper.ApplicationHelper.checkLocationEnabled
import com.agile.officepool.network.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.agile.officepool.ui.theme.RobotoCondensed

@Composable
fun LocationRequestScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as Activity
    val sessionManager = SessionManager(context)


    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 🎯 Location (GPS) was turned ON
            if(sessionManager.isLoggedIn()){
                navController.navigate("startUp") {
                    popUpTo("locationPermission") { inclusive = true }
                }
            }else{
                navController.navigate("login") {
                    popUpTo("locationPermission") { inclusive = true }
                }
            }
        } else {
            // ❌ User cancelled
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkLocationEnabled(
                activity = activity,
                navController = navController,
                launcher = locationLauncher,
                onGpsAlreadyEnabled = {
                    // ✅ GPS already ON — navigate directly
                    if (sessionManager.isLoggedIn()) {
                        navController.navigate("startUp") {
                            popUpTo("locationPermission") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("locationPermission") { inclusive = true }
                        }
                    }
                }
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )

            Text(
                text = "Enable Location Access",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "We need your location to find nearby rides and provide better service.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Grant Permission", color = Color.White,
                        fontFamily = RobotoCondensed
                    )
                }

                TextButton(onClick = { activity.finish() }) {
                    Text("Maybe Later",
                        fontFamily = RobotoCondensed)
                }
            }
        }
    }
}


@Composable
fun LocationScreenPreviewWrapper() {
    val navController = rememberNavController()
    OfficePoolTheme {
        LocationRequestScreen(navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLocationScreen() {
    LocationScreenPreviewWrapper() // Use the wrapper function
}
