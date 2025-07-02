package com.agile.officepool

import LiveTrackingMapScreen
import RideRequestsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agile.officepool.ViewModel.RideRequestsViewModel
import com.agile.officepool.ViewModel.SharedRideViewModel
import com.agile.officepool.ViewModel.StartupViewModel
import com.agile.officepool.helper.ApplicationHelper.isLocationPermissionGranted
import com.agile.officepool.network.SessionManager
import com.agile.officepool.screens.AvailableRidesScreen
import com.agile.officepool.screens.LiveTrackingForPassenger
import com.agile.officepool.screens.LocationRequestScreen
import com.agile.officepool.screens.LoginScreen
import com.agile.officepool.screens.ProfileScreen
import com.agile.officepool.screens.RegisterScreen
import com.agile.officepool.screens.RiderPaymentScreen
import com.agile.officepool.screens.SearchScreen
import com.agile.officepool.screens.StartupScreen
import com.agile.officepool.screens.UpdateProfileScreen
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        val status = FirebaseApp.getApps(this)
        Log.d("FCM_DEBUG", "Firebase initialized apps: $status")
        sessionManager = SessionManager(this)
        val hasLocationPermission = isLocationPermissionGranted(this)
        val isLoggedIn = sessionManager.isUserLoggedIn()

        val rideId = intent?.getStringExtra("rideId")

        val targetRoute = intent.getStringExtra("target_route")


        val startDestination = when {
            !isLoggedIn -> "login"
            targetRoute != null -> targetRoute
            rideId != null -> "rideRequests"
            hasLocationPermission -> "startUp"
            else -> "locationPermission"
        }

        Log.d("StartDestination", "hasLocationPermission: $hasLocationPermission")
        Log.d("StartDestination", "Start Destination: $startDestination")


        setContent {
            OfficePoolTheme {
                navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0,0,0,0), // Proper handling of insets
                ) { _ ->
                    Box() {
                        Navigation(
                            navController = navController,
                            context = this@MainActivity,
                            startDestination = startDestination
                        )
                    }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent.let {
            val rideId = it.getStringExtra("rideId")
            val targetRoute = it.getStringExtra("target_route")

            Log.d("onNewIntent", "rideId: $rideId, targetRoute: $targetRoute")

            // Navigate dynamically to the desired route if navController is initialized
            if (::navController.isInitialized && !targetRoute.isNullOrEmpty()) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
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
    val sharedRideViewModel: SharedRideViewModel = viewModel()
    val startupViewModel : StartupViewModel = viewModel()
    val rideRequestsViewModel : RideRequestsViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("locationPermission") { LocationRequestScreen(navController) }
        composable("searchScreen") { SearchScreen(navController) }
        composable("rideRequests") { RideRequestsScreen(navController,rideRequestsViewModel ) }
        composable("startUp"){ StartupScreen(navController,rideViewModel = sharedRideViewModel,startupViewModel=startupViewModel) }
        composable("profile") { ProfileScreen(navController, context = LocalContext.current) }
        composable("updateProfile") { UpdateProfileScreen(navController) }

        composable("riderPayment") { RiderPaymentScreen(navController = navController,
            rideViewModel = sharedRideViewModel,
            onPaymentConfirmed = {
                navController.popBackStack()
            }
        )}

        //for rider
        composable(
            route = "liveTrackingMap/{rideId}/{requestId}",
            arguments = listOf(
                navArgument("rideId") { type = NavType.StringType },
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            LiveTrackingMapScreen(navController,rideId = rideId, requestId = requestId, sharedRideViewModel = sharedRideViewModel)
        }

        //for passenger
        composable("liveTrackingForPassenger/{rideId}"
        ) { backStackEntry ->
                val rideId = backStackEntry.arguments?.getString("rideId")
                LiveTrackingForPassenger(navHostController=navController,rideId = rideId!!,rideViewModel = sharedRideViewModel)
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













