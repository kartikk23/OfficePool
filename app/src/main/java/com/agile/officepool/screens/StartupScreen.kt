package com.agile.officepool.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.R
import com.agile.officepool.ViewModel.SharedRideViewModel
import com.agile.officepool.ViewModel.StartupViewModel
import com.agile.officepool.components.ShimmerRideCard
import com.agile.officepool.network.SessionManager
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartupScreen(
    navController: NavController,
    rideViewModel: SharedRideViewModel = viewModel(),
    startupViewModel : StartupViewModel = viewModel()
) {

    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val rideId = remember { mutableStateOf<String?>(null) }
    val isRideActive by rideViewModel.isRideActive
    val lastObservedRideId = remember { mutableStateOf<String?>(null) }
    val passengerId = sessionManager.getUserId()
    val recentRides by startupViewModel.recentRides
    val isRecentRidesLoading by startupViewModel.isLoading
    val errorMessage by startupViewModel.errorMessage

    LaunchedEffect(passengerId) {
        while (true) {
            passengerId?.let {
                rideViewModel.getActiveRideForPassenger(passengerId.toLong()) { id ->
                    rideId.value = id?.toString()
                }
                delay(10000L)
            }

        }
    }

    LaunchedEffect(passengerId) {
        passengerId?.let {
            startupViewModel.fetchRecentRides(it.toLong())
        }
    }


    // 👀 Observe ride status and navigate when started
    if (rideId.value != null && lastObservedRideId.value != rideId.value) {
        LaunchedEffect(rideId.value) {
            lastObservedRideId.value = rideId.value
            rideViewModel.observePassengerRideStatus(
                rideId = rideId.value!!,
                onStarted = { rideViewModel.setRideActive(true) },
                onNotActive = { rideViewModel.setRideActive(false) }
            )
        }

        DisposableEffect(rideId.value) {
            onDispose {
                rideViewModel.removeRideStatusListener(rideId.value!!)
                lastObservedRideId.value = null
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top Row with OfficePool Text and Profile Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp   ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left-aligned text
            Text(
                modifier = Modifier.weight(7f),
                text = "OfficePool",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

            )

            IconButton(
                modifier = Modifier.weight(1.5f).clip(shape = RoundedCornerShape(10.dp)),
                onClick = {
                    navController.navigate("profile")
                }
            ){
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(4.dp).size(30.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1.5f).clip(shape = RoundedCornerShape(10.dp)),
                onClick = {
                    navController.navigate("rideRequests")
                }
            ){
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(4.dp).size(30.dp)
                )
            }




          }
      

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 5.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFF161e33))
                .clickable { navController.navigate("searchScreen") }
        ){
            // Search Bar
            WhereToGoTextField()
        }

        if (isRideActive==true) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        navController.navigate("liveTrackingForPassenger/${rideId.value}")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Active Ride in Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Text(
                        text = "Tap to view live tracking of your ride.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }



        Box(modifier = Modifier.padding(start = 14.dp,end = 14.dp, bottom = 20.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),

                ) {

                Spacer(modifier = Modifier.height(5.dp))

                // Recent Destinations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recent Rides",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    when {
                        isRecentRidesLoading -> {
                            // 👇 Shimmer placeholders while loading
                            repeat(2) {
                                ShimmerRideCard()
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        errorMessage != null -> {
                            // 👇 Show error with retry button
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = errorMessage ?: "An unexpected error occurred",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Button(onClick = { startupViewModel.fetchRecentRides(passengerId = YOUR_PASSENGER_ID) }) {
//                                    Text("Retry")
//                                }
                            }
                        }

                        recentRides.isNotEmpty() -> {
                            // 👇 Fade-in when data is available
                            AnimatedVisibility(visible = true, enter = fadeIn()) {
                                Column {
                                    recentRides.forEach { ride ->
                                        RecentRideCard(
                                            date = ride.rideDate ?: "N/A",
                                            time = ride.rideStartTime,
                                            fromLocation = ride.source ?: "Unknown",
                                            toLocation = ride.destination ?: "Unknown",
                                            riderId = ride.riderId.toString() ?: "Unknown"
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = "No recent rides available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))
                // Feature Cards

                FeatureCard("", Color(0xFF2196F3), R.drawable.carpool,modifier = Modifier.width(400.dp),navController)

                Spacer(modifier = Modifier.height(10.dp))
                // Motivational Full-width Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    ) {
                        Text(
                            text = "Save fuel • Reduce traffic • Save nature 🌍",
                            color = Color(0xFF004D40),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Corporate Booking Card
//                CorporateRideCard(R.drawable.card_img)
//                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Nearest Business Zones",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 5.dp)

                )
                // Horizontal Scrollable Cards
                LazyRow(
                    modifier = Modifier.padding(bottom = 5.dp),
                    contentPadding = PaddingValues(start = 1.dp,end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    item {
                        BusinessZoneCard(
                            zoneName = "Amar Tech Park",
                            imagePainter = painterResource(id = R.drawable.amar)
                        )
                    }

                    // You can add more items like this:
                    item {
                        BusinessZoneCard(
                            zoneName = "Icon Tower",
                            imagePainter = painterResource(id = R.drawable.icon)
                        )
                    }

                    item {
                        BusinessZoneCard(
                            zoneName = "Cognizant CDC",
                            imagePainter = painterResource(id = R.drawable.cogni)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessZoneCard(
    zoneName: String,
    imagePainter: Painter
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top image section
            Image(
                painter = imagePainter,
                contentDescription = "Zone Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Zone name
            Text(
                text = zoneName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF311B92),
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = TextAlign.Center

            )
            // Subtitle and arrow
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    modifier = Modifier.weight(8f),
                    text = "Travel to your IT park with your buddy",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Start,

                )
                Icon(
                    modifier = Modifier.weight(2f),
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go",
                    tint = Color(0xFF311B92),
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    bgColor: Color,
    imageResId: Int, // pass drawable resource ID
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Card(
//        shape = RoundedCornerShape(50.dp),
        modifier = modifier
            .clickable{
                navController.navigate("searchScreen")
            }
            .border(1.dp,Color.LightGray, RoundedCornerShape(10))
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(Color.Black) // optional overlay for better text contrast
            )
            Box(
                modifier = Modifier.fillMaxSize().padding(start = 10.dp, end = 5.dp, bottom = 5.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
fun RecentRideCard(
    date: String,
    time : String,
    fromLocation: String,
    toLocation: String,
    riderId: String
) {

    // Format date and time
    val displayDateTime = try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse("$date $time", formatter)

        val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd MMM"))
        val formattedTime = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

        "$formattedDate, $formattedTime"
    } catch (e: Exception) {
        Log.e("RecentRideCard", "Error parsing date and time: $e")
        "$date $time"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Date + Rider ID Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rider ID: $riderId",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = displayDateTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column (
                    modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fromLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column (
                    modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = toLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
fun WhereToGoTextField() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = "Find your Ride buddy..",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.W500
        )
    }

}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
    val navController = rememberNavController()
    StartupScreen(navController)
}