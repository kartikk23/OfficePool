package com.agile.officepool.screens

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.MainActivity
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var name by remember { mutableStateOf(TextFieldValue("")) }

    var source by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var sourceLat by remember { mutableStateOf<Double?>(null) }
    var sourceLng by remember { mutableStateOf<Double?>(null) }
    var selectedDestination by remember { mutableStateOf<String?>(null) }
    var destinationLat by remember { mutableStateOf<Double?>(null) }
    var destinationLng by remember { mutableStateOf<Double?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    var location by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    var showModal by remember { mutableStateOf(false)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showSearchbar by remember { mutableStateOf(true)}
    var isLoading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    val sessionManager = remember { SessionManager(context) }

    val userEmail = sessionManager.getUserEmail()
    val isUserLoggedIn = sessionManager.isUserLoggedIn()



    // Initialize the Places Client
    fun initializePlacesClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }
    val placesClient = remember { initializePlacesClient(context) }

    var placePredictions by remember { mutableStateOf(emptyList<AutocompletePrediction>()) }

//    TransparentStatusBar()

    fun fetchPlaces(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                placePredictions = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                placePredictions = emptyList()
            }
    }






    // Request location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            hasLocationPermission = true
            val (loc, address) = fetchUserLocation(context, fusedLocationClient)
            if (loc != null) {
                userLocation = loc
                location = address ?: "Unknown Location"
            }
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        if (!userEmail.isNullOrEmpty()) {
            Toast.makeText(context, "Logged in as: $userEmail", Toast.LENGTH_LONG).show()
        }

        // Check for incomplete profile and navigate
//        val sessionManager = SessionManager(context)
//        val userName = sessionManager.getUserName() // Or any field you store
//        val mobile = sessionManager.getMobile()
//            val apiService = RetrofitClient.instance;
//            val resp = apiService.
//        if (userName.isNullOrEmpty() || mobile.isNullOrEmpty()) {
//            navController.navigate("updateProfile") // 👈 Navigate to update profile screen
//        }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Box( modifier = Modifier.padding(paddingValues)) {
            // Placeholder for Map (Replace with your map implementation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ){
                GoogleMapView(userLocation, source, destination, polylinePoints)

            }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//                    .align(Alignment.TopCenter)
//            )
//            {
//                // 🧑‍💼 Update Profile Button
//                Button(
//                    onClick = { navController.navigate("updateProfile") },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
//                ) {
//                    Text("Update Profile", color = Color.White)
//                }
//            }

            Spacer(modifier = Modifier.height(10.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 30.dp, 16.dp, 0.dp)
            ) {
                // 🔍 Ride Buddy Button
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .background(Color(0xFF161e33), shape = RoundedCornerShape(25.dp))
                        .clickable { navController.navigate("searchScreen") }
                ) {
                    Text(
                        text = "Find your Ride buddy..",
                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500),
                        modifier = Modifier.padding(16.dp, 14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 👤 Profile Button
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .background(Color(0xFF161e33), shape = RoundedCornerShape(50.dp))
                        .clickable { navController.navigate("profile") }
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(10.dp, 12.dp)
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 🧑‍💼 Update Profile Button
                Button(
                    onClick = { navController.navigate("updateProfile") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                    modifier = Modifier
                        .weight(0.4f)
                        .height(48.dp)
                ) {
                    Text("Update", color = Color.White, fontSize = 12.sp)
                }
            }

            /*
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                        .background(Color(0xFF161e33), shape = RoundedCornerShape(25.dp)) // ✅ Light BG & Rounded Corners
                        .clickable {
                            navController.navigate("searchScreen")
                        } // ✅ Navigate on click

                ) {
                    Text(
                        text = "Find your Ride buddy..",
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        ),
                        modifier = Modifier.padding(16.dp,14.dp)
                    )

                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .background(Color(0xFF161e33), shape = RoundedCornerShape(50.dp)) // ✅ Light BG & Rounded Corners
                        .clickable {
                            navController.navigate("profile")
                        } // ✅ Navigate on click

                ) {
                    Icon(
                        Icons.Default.Person, contentDescription = null, tint = Color.White,modifier = Modifier
                            .padding(10.dp, 12.dp)
                            .align(Alignment.Center))

                }


            }
*/
            Row(
               modifier =  Modifier
                   .align(Alignment.BottomCenter)
                   .padding(16.dp, 10.dp)
            ){
                Button(
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch{
                            logoutUser(
                                context = context,
                                onSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true } // Clear back stack
                                    }
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Logout", color = Color.White, fontSize = 16.sp)
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Black, modifier = Modifier.padding(10.dp))
                }

            }



        }
    }
}


@Composable
fun GoogleMapView(
    userLocation: LatLng?,
    source: LatLng?,
    destination: LatLng?,
    polylinePoints: List<LatLng>,
) {
    val cameraPositionState = rememberCameraPositionState()

    val sourceMarkerState = rememberMarkerState()
    val destinationMarkerState = rememberMarkerState()

    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }

    // Move camera to user location initially
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    // Update source marker dynamically
    LaunchedEffect(source) {
        source?.let {
            sourceMarkerState.position = it
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    // Update destination marker dynamically
    LaunchedEffect(destination) {
        destination?.let {
            destinationMarkerState.position = it
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    // Auto-zoom to fit polyline when route updates
    LaunchedEffect(polylinePoints) {
        if (polylinePoints.isNotEmpty()) {
            val bounds = LatLngBounds.builder().apply {
                polylinePoints.forEach { include(it) }
            }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,  // ✅ Show user's location, // ✅ Enable blue dot
            mapType = MapType.NORMAL, // ✅ Change map type
            isTrafficEnabled = true      // ✅ Show traffic
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,  // ✅ Show zoom controls
            compassEnabled = false,       // ✅ Show compass
            myLocationButtonEnabled = false,
            tiltGesturesEnabled = true,  // ✅ Enable tilting
            scrollGesturesEnabled = true // ✅ Enable scrolling
        )

    ) {



        // Source Marker
        if (source != null) {
            Marker(state = sourceMarkerState, title = "Source")

        }

        // Destination Marker
        if (destination != null) {
            Marker(state = destinationMarkerState, title = "Destination")
        }

        // Draw polyline route dynamically
        if (polylinePoints.isNotEmpty()) {
            Polyline(points = polylinePoints, color = Color.Blue, width = 13f, startCap = RoundCap(),
                endCap = RoundCap(),
                jointType = JointType.ROUND)

        }
    }
}








suspend fun fetchLatLngFromPlaceName(context: Context, placeName: String): LatLng? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(placeName, 1)
            val location = addresses?.firstOrNull()
            location?.let {
                LatLng(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            null
        }
    }
}


suspend fun fetchRoute(source: LatLng, destination: LatLng): List<LatLng> {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${source.latitude},${source.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            parseRoute(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun parseRoute(response: String): List<LatLng> {
    val json = JSONObject(response)
    val routes = json.getJSONArray("routes")
    if (routes.length() == 0) return emptyList()

    val overviewPolyline = routes.getJSONObject(0)
        .getJSONObject("overview_polyline")
        .getString("points")

    return decodePolyline(overviewPolyline)
}

fun decodePolyline(encoded: String): List<LatLng> {
    val polyline = mutableListOf<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var shift = 0
        var result = 0
        do {
            val b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

        shift = 0
        result = 0
        do {
            val b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

        polyline.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return polyline
}

// Fetch user's current location and convert to an address
@SuppressLint("MissingPermission")
suspend fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient): Pair<LatLng?, String?> {
    return withContext(Dispatchers.IO) {
        try {
            val locationTask = fusedLocationClient.lastLocation
            val location: Location? = Tasks.await(locationTask)
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                val address = getAddressFromLatLng(context, latLng)
                Pair(latLng, address)
            } ?: Pair(null, null)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
}

// Convert LatLng to a readable address using Geocoder
fun getAddressFromLatLng(context: Context, latLng: LatLng): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.getAddressLine(0)
    } catch (e: Exception) {
        null
    }
}


suspend fun logoutUser(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.logout()

            Log.d("LOGOUT_RESPONSE", "Response Code: ${response.code()} | Body: ${response.body()}")

            if (response.isSuccessful) {
                val jsonResponse = response.body()?.toString()  // Read response as string
                val message = JSONObject(jsonResponse.toString()).optString("message", "Logout successful")

                withContext(Dispatchers.Main) {
                    val sessionManager = SessionManager(context)
                    sessionManager.clearSession() // ✅ Clear session token
                    restartApp(context) // ✅ Restart the app
                    onSuccess()
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                withContext(Dispatchers.Main) {
                    onError("Logout failed: $errorBody")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message}")
            }
        }
    }
}

fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
}

@Composable
fun HomeScreenPreviewWrapper() {
    val navController = rememberNavController()
    OfficePoolTheme {
        HomeScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreenPreviewWrapper()  // Use the wrapper function
}






