package com.agile.officepool

// MainActivity.kt
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.ViewModel.UserViewModel
import com.agile.officepool.screens.HomeScreen
import com.agile.officepool.screens.LoginScreen
import com.agile.officepool.screens.RegisterScreen
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfficePoolTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                ) {
                    val navController = rememberNavController()
                    val viewModel: UserViewModel = viewModel()

                    Navigation(navController, viewModel, intent)
                }
            }
        }
    }





}

@Composable
fun Navigation(navController: NavHostController, viewModel: UserViewModel, intent: Intent?) {
    // Handle deep link when Composable is first loaded
    LaunchedEffect(intent) {
        handleDeepLink(intent, viewModel, navController)
    }
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home" +
            "") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
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
//                        navController.navigate("home") // Redirect to HomeScreen
                    },
                    onError = { error ->
                        Log.e("LinkedInLogin", "Error: ${error.message}")
                    }
                )
            }
        }
    }
}






