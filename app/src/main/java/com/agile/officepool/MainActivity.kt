package com.agile.officepool

import LiveTrackingMapScreen
import RideRequestsScreen
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agile.officepool.helper.ApplicationHelper.isLocationPermissionGranted
import com.agile.officepool.network.SessionManager
import com.agile.officepool.screens.AvailableRidesScreen
import com.agile.officepool.screens.LocationRequestScreen
import com.agile.officepool.screens.LoginScreen
import com.agile.officepool.screens.ProfileScreen
import com.agile.officepool.screens.RegisterScreen
import com.agile.officepool.screens.SearchScreen
import com.agile.officepool.screens.StartupScreen
import com.agile.officepool.screens.UpdateProfileScreen
import com.agile.officepool.ui.theme.OfficePoolTheme



class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        val hasLocationPermission = isLocationPermissionGranted(this)
        val isLoggedIn = sessionManager.isUserLoggedIn()

        val rideId = intent?.getStringExtra("rideId")

        val startDestination = when {
            rideId != null && isLoggedIn -> "rideRequests"
            !isLoggedIn -> "login"
            hasLocationPermission -> "startUp"
            else -> "locationPermission"
        }


        Log.d("StartDestination", "hasLocationPermission: $hasLocationPermission")
        Log.d("StartDestination", "Start Destination: $startDestination")



        setContent {


            OfficePoolTheme {
                navController = rememberNavController()

                Box(modifier = Modifier
                    .fillMaxSize()
                ) {
                    Navigation(navController, this@MainActivity, startDestination)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationPermissionGranted(this)){
            checkLocationOnStart()
        }
    }

    private fun checkLocationOnStart() {
        if (!isLocationPermissionGranted(this)) {

            Log.d("LocationCheck", "Location/GPS not available. Navigating to permission screen.")
            try {
                navController.navigate("locationPermission") {
                    popUpTo("startUp") { inclusive = true } // Clear backstack if needed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d("LocationCheck", "Location permission and GPS are available.")
            // ✅ Everything okay, stay where you are
        }
    }

}

@Composable
fun Navigation(navController: NavHostController, context: Context, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("locationPermission") { LocationRequestScreen(navController) }
        composable("searchScreen") { SearchScreen(navController) }
        composable("rideRequests") { RideRequestsScreen(navController) }
        composable("startUp"){ StartupScreen(navController) }
        composable("profile") { ProfileScreen(navController, context = LocalContext.current) }
        composable("updateProfile") { UpdateProfileScreen(navController) }
        composable(
            route = "liveTrackingMap/{rideId}/{requestId}",
            arguments = listOf(
                navArgument("rideId") { type = NavType.StringType },
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            LiveTrackingMapScreen(navController,rideId = rideId, requestId = requestId)
        }
        // ✅ Define availableRides with route parameters
        composable(
            "availableRides/{source}/{sourceLat}/{sourceLng}/{destination}/{destinationLat}/{destinationLng}"
        ) { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: ""
            val sourceLat = backStackEntry.arguments?.getString("sourceLat")?.toDoubleOrNull() ?: 0.0
            val sourceLng = backStackEntry.arguments?.getString("sourceLng")?.toDoubleOrNull() ?: 0.0
            val destination = backStackEntry.arguments?.getString("destination") ?: ""
            val destinationLat = backStackEntry.arguments?.getString("destinationLat")?.toDoubleOrNull() ?: 0.0
            val destinationLng = backStackEntry.arguments?.getString("destinationLng")?.toDoubleOrNull() ?: 0.0
            AvailableRidesScreen(navController, source, sourceLat, sourceLng, destination, destinationLat, destinationLng)
        }
    }

}











