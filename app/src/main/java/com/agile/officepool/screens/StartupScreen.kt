package com.agile.officepool.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.agile.OfficePool.utils.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.agile.officepool.ui.theme.RobotoCondensed
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

    LaunchedEffect(Unit) {
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


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OfficePool",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("profile") }
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate("rideRequests") }
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)

        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                item {
                    // 🔍 Search Bar with scroll-in effect
                    WhereToGoTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("searchScreen")}
                            .clip(RoundedCornerShape(25.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                // ⬇ Add other `item {}` blocks for isRideActive, RecentRides, etc.
                item {
                    if (isRideActive == true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal =1.dp)
                                .clickable {
                                    navController.navigate("liveTrackingForPassenger/${rideId.value}")
                                },
                            colors = cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Active Ride in Progress",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RobotoCondensed,
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
                }

                // Continue adding item { RecentRides }, item { Zones }, etc...
                item {
                    // Section title
                    Text(
                        text = "Recent Rides",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }

                item {
                    when {
                        isRecentRidesLoading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(2) {
                                    ShimmerRideCard()
                                }
                            }
                        }

                        errorMessage != null -> {
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
                            }
                        }

                        recentRides.isEmpty() -> {
                            Text(
                                text = "No recent rides available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

// Show all recent rides in a loop
                items(recentRides) { ride ->
                    RecentRideCard(
                        date = ride.rideDate,
                        time = ride.rideStartTime,
                        fromLocation = ride.source,
                        toLocation = ride.destination,
                        riderId = ride.riderId
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Nearest Business Zones",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

// Horizontal business zone cards
                item {
                    val zones = listOf(
                        BusinessZone("Amar Tech Park", R.drawable.amar, isPopular = true),
                        BusinessZone("Icon Tower", R.drawable.icon),
                        BusinessZone("Cognizant CDC", R.drawable.cogni)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        items(zones) { zone ->
                            BusinessZoneCard(
                                zone = zone,
                                imagePainter = painterResource(id = zone.imageRes)
                            ) {
                                // Optional: navController.navigate("zoneDetails/${zone.name}")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    FeatureCard(
                        title = "",
                        bgColor = Color(0xFF2196F3),
                        imageResId = R.drawable.carpool,
                        modifier = Modifier.width(400.dp),
                        navController = navController
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = cardColors(containerColor = Color(0xFFB3E5FC))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
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
                }




            }
        }


    }
}



@Composable
fun BusinessZoneCard(
    zone: BusinessZone,
    imagePainter: Painter,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "CardScale"
    )

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(160.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = imagePainter,
                contentDescription = "Zone Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Text and icon at bottom
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = zone.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Travel to your IT park",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Optional badge
            if (zone.isPopular) {
                Text(
                    text = "Popular",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xFFEC407A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}


data class BusinessZone(
    val name: String,
    val imageRes: Int,
    val isPopular: Boolean = false
)


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
        colors = cardColors(containerColor = bgColor)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = RobotoCondensed
                )
                Text(
                    text = displayDateTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = RobotoCondensed
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
fun WhereToGoTextField(modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon",
            tint = MaterialTheme.colorScheme.inverseOnSurface
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = "Find your Ride buddy..",
            color = MaterialTheme.colorScheme.inverseOnSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.W500
        )
    }

}

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun StartupScreenPreview() {
    OfficePoolTheme {
        val navController = rememberNavController()
        StartupScreen(navController)
    }
}



//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(WindowInsets.statusBars.asPaddingValues())
//            .background(color = MaterialTheme.colorScheme.surface),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        // Top Row with OfficePool Text and Profile Icon
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp   ),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//            // Left-aligned text
//            Text(
//                modifier = Modifier.weight(7f),
//                text = "OfficePool",
//                fontSize = 25.sp,
//                fontWeight = FontWeight.Bold,
//                style = MaterialTheme.typography.headlineMedium.copy(
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//
//            )
//
//            IconButton(
//                modifier = Modifier.weight(1.5f).clip(shape = RoundedCornerShape(10.dp)),
//                onClick = {
//                    navController.navigate("profile")
//                }
//            ){
//                Icon(
//                    imageVector = Icons.Default.Person,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier.padding(4.dp).size(30.dp)
//                )
//            }
//
//            IconButton(
//                modifier = Modifier.weight(1.5f).clip(shape = RoundedCornerShape(10.dp)),
//                onClick = {
//                    navController.navigate("rideRequests")
//                }
//            ){
//                Icon(
//                    imageVector = Icons.Default.Notifications,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier.padding(4.dp).size(30.dp)
//                )
//            }
//
//
//
//
//          }
//
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 14.dp, vertical = 5.dp)
//                .clip(RoundedCornerShape(25.dp))
//                .background(Color(0xFF161e33))
//                .clickable { navController.navigate("searchScreen") }
//        ){
//            // Search Bar
//            WhereToGoTextField()
//        }
//
//
//}