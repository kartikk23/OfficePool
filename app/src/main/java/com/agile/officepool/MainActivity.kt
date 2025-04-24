package com.agile.officepool

// MainActivity.kt

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NavHostController
import androidx.navigation.NavType

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.agile.officepool.network.FcmTokenRequest
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.agile.officepool.rider.RideRequestScreen
import com.agile.officepool.screens.AvailableRidesScreen
import com.agile.officepool.screens.HomeScreen
import com.agile.officepool.screens.LoginScreen
import com.agile.officepool.screens.ProfileScreen
import com.agile.officepool.screens.RegisterScreen

import com.agile.officepool.screens.SearchScreen
import com.agile.officepool.screens.UpdateProfileScreen
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.google.firebase.messaging.FirebaseMessaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sessionManager = SessionManager(this)
        val startDestination = if (sessionManager.isUserLoggedIn()) "home" else "login"
        val rideId = intent?.getStringExtra("rideId")



        // Fetch and send FCM token at app startup
        uploadFcmTokenIfNeeded(this)



        setContent {


            OfficePoolTheme {
                val navController = rememberNavController()
                if (rideId != null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("rideRequest/$rideId")
                    }
                }
                Box(modifier = Modifier
                    .fillMaxSize()


                ) {

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
        composable("rideRequests") { RideRequestsScreen(navController) }

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

        composable("updateProfile") { UpdateProfileScreen(navController) }
        composable("rideRequest/{rideId}") { backStackEntry ->
            val rideIdParam = backStackEntry.arguments?.getString("rideId")
            RideRequestScreen(rideIdParam)
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
//        composable("dashboard") { RiderDashboardScreen(navController) }
//        composable("riderScreen") { RiderScreen(navController) }

        composable("profile") { ProfileScreen(navController, context = LocalContext.current) }
//        composable("rideRequest") {RideRequestScreen(navController)  }
//        composable("currentRide"){ CurrentRideScreen(navController) }
    }



}

fun uploadFcmTokenIfNeeded(context: Context) {
    val sessionManager = SessionManager(context)
    val userId = sessionManager.getUserId()

    if (userId != null) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "📲 Current token from Firebase: $token")

                // Upload to backend
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.updateFcmToken(
                            FcmTokenRequest(userId = userId, token = token)
                        )
                        if (response.isSuccessful) {
                            Log.d("FCM", "✅ Token updated at startup!")
                        } else {
                            Log.e("FCM", "❌ Failed to update token: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM", "🔥 Exception sending token", e)
                    }
                }
            } else {
                Log.e("FCM", "❌ Failed to get FCM token", task.exception)
            }
        }
    } else {
        Log.e("FCM", "🚫 User ID not found — skipping FCM token upload")
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






