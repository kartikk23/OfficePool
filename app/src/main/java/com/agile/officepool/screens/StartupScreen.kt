package com.agile.officepool.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.R
import com.agile.officepool.ViewModel.SharedRideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartupScreen(
    navController: NavController,
    rideViewModel: SharedRideViewModel = SharedRideViewModel()) {

    // Mock rideId for testing — replace with your actual logic later
    val mockRideId = "1"
    val rideId = remember { mutableStateOf<String?>(mockRideId) }

    // 👀 Observe ride status and navigate when started
    rideId.value?.let { id ->
        LaunchedEffect(id) {
            rideViewModel.observePassengerRideStatus(id) {
                navController.navigate("liveTrackingForPassenger/$id") {
                    popUpTo("startUp") { inclusive = true }
                }
            }
        }

        DisposableEffect(id) {
            onDispose {
                rideViewModel.removeRideStatusListener(id)
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
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
                    RecentChip("Workplace HQ")
                    RecentChip("Tech Park 4")
                }
                Spacer(modifier = Modifier.height(10.dp))
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
fun RecentChip(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray,RoundedCornerShape(8.dp)), // ⬅️ Light grey border added
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
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