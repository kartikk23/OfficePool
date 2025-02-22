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

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.ViewModel.UserViewModel
import com.agile.officepool.network.SessionManager
import com.agile.officepool.screens.HomeScreen
import com.agile.officepool.screens.LoginScreen
import com.agile.officepool.screens.ProfileScreen
import com.agile.officepool.screens.RegisterScreen
import com.agile.officepool.screens.RiderDashboardScreen
import com.agile.officepool.screens.RiderScreen
import com.agile.officepool.ui.theme.OfficePoolTheme



class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve Rider Mode state
        val sessionManager = SessionManager(this)
        val startDestination = if (sessionManager.isRiderMode()) "riderScreen" else "home"


        setContent {
            OfficePoolTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                ) {
                    val navController = rememberNavController()
                    val viewModel: UserViewModel = viewModel()

                    Navigation(navController, viewModel, intent, startDestination, this@MainActivity)
                }
            }
        }
    }





}

@Composable
fun Navigation(navController: NavHostController, viewModel: UserViewModel, intent: Intent?,startDestination: String, context: Context) {
    // Handle deep link when Composable is first loaded
    LaunchedEffect(intent) {
        handleDeepLink(intent, viewModel, navController)
    }
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("dashboard") { RiderDashboardScreen(navController) }
        composable("riderScreen") { RiderScreen(navController) }
        composable("profile") { ProfileScreen(navController, context = LocalContext.current) }


    }
}

private fun handleDeepLink(intent: Intent?, viewModel: UserViewModel, navController: NavHostController) {
    intent?.data?.let { uri ->
        Log.d("LinkedInLogin", "Received URI: $uri")

        if (uri.scheme == "myapp" && uri.host == "oauth") {
            val authCode = uri.getQueryParameter("code")
            Log.d("LinkedInLogin", "Authorization Code: $authCode")

            if (!authCode.isNullOrEmpty()) {
                viewModel.loginWithLinkedIn(authCode,
                    onSuccess = {
                        Log.d("LinkedInLogin", "Login successful!")
                        navController.navigate("home") // Redirect to HomeScreen
                    },
                    onError = { error ->
                        Log.e("LinkedInLogin", "Error: ${error.message}")
                    }
                )
            }
        }
    }
}






