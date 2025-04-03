package com.agile.officepool

// MainActivity.kt
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.ViewModel.UserViewModel
import com.agile.officepool.network.SessionManager
import com.agile.officepool.screens.AvailableRidesScreen
import com.agile.officepool.screens.HomeScreen
import com.agile.officepool.screens.LoginScreen
import com.agile.officepool.screens.RegisterScreen
import com.agile.officepool.screens.SearchScreen

import com.agile.officepool.ui.theme.OfficePoolTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val sessionManager = SessionManager(this)
        val startDestination = if (sessionManager.isUserLoggedIn()) "home" else "login"

        setContent {

            OfficePoolTheme {
                Box(modifier = Modifier
                    .fillMaxSize()


                ) {

                    val navController = rememberNavController()
                    Navigation(navController, this@MainActivity, startDestination)
                }
            }
        }
    }





}

@Composable
fun Navigation(navController: NavHostController, context: Context, startDestination: String) {
    // Handle deep link when Composable is first loaded
//    LaunchedEffect(intent) {
//        handleDeepLink(intent, viewModel, navController)
//    }


    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("searchScreen") { SearchScreen(navController) }
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
//        composable("dashboard") { RiderDashboardScreen(navController) }
//        composable("riderScreen") { RiderScreen(navController) }

//        composable("profile") { ProfileScreen(navController, context = LocalContext.current) }
//        composable("rideRequest") {RideRequestScreen(navController)  }
//        composable("currentRide"){ CurrentRideScreen(navController) }
    }
}

//private fun handleDeepLink(intent: Intent?, viewModel: UserViewModel, navController: NavHostController) {
//    intent?.data?.let { uri ->
//        Log.d("LinkedInLogin", "Received URI: $uri")
//
//        if (uri.scheme == "myapp" && uri.host == "oauth") {
//            val authCode = uri.getQueryParameter("code")
//            Log.d("LinkedInLogin", "Authorization Code: $authCode")
//
//            if (!authCode.isNullOrEmpty()) {
//                viewModel.loginWithLinkedIn(authCode,
//                    onSuccess = {
//                        Log.d("LinkedInLogin", "Login successful!")
//                        navController.navigate("home") // Redirect to HomeScreen
//                    },
//                    onError = { error ->
//                        Log.e("LinkedInLogin", "Error: ${error.message}")
//                    }
//                )
//            }
//        }
//    }
//}






