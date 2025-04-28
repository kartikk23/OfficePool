package com.agile.officepool.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartupScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OfficePool",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.align(Alignment.Start)
                .padding(bottom = 15.dp)


        )

        // Search Bar
        var searchText by remember { mutableStateOf("") }

        WhereToGoTextField(navController)



        Spacer(modifier = Modifier.height(16.dp))

        // Recent Destinations
        Column(
            modifier = Modifier.fillMaxWidth()
            ,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecentChip("Workplace HQ")
            RecentChip("Tech Park 4")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureCard("Travel Together", Color(0xFF2196F3), R.drawable.card_img,modifier = Modifier.weight(1f))
            FeatureCard("Connect", Color(0xFF4CAF50), R.drawable.handshake,modifier = Modifier.weight(1f))
            FeatureCard("Grow", Color(0xFFFFC107), R.drawable.grow,modifier = Modifier.weight(1f))
        }



        Spacer(modifier = Modifier.height(24.dp))

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
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Save fuel • Reduce traffic • Save nature 🌍",
                    color = Color(0xFF004D40),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Corporate Booking Card
        CorporateRideCard(R.drawable.card_img)
        }
    }

@Composable
fun FeatureCard(
    title: String,
    bgColor: Color,
    imageResId: Int, // pass drawable resource ID
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(150.dp),

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
                    .background(Color.Black.copy(alpha = 0.4f)) // optional overlay for better text contrast
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}




@Composable
fun RecentChip(text: String) {
    Surface(
        shape = RoundedCornerShape(10),
        color = Color(0xFFE0F7FA),
        modifier = Modifier.padding(end = 4.dp)
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF006064),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CorporateRideCard(imageResId: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(16.dp)
//        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Use transparent to show image
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
//                    .background(Color(0xFFE1BEE7).copy(alpha = 0.6f)) // Optional tint overlay
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
//                Text(
//                    text = "🤝 Corporate Ride",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF4A148C)
//                )
//                Text(
//                    text = "You're booking a ride with your colleague. Travel smart, travel together!",
//                    fontSize = 14.sp,
//                    color = Color(0xFF6A1B9A),
//                    modifier = Modifier.padding(top = 8.dp)
//                )
            }
        }
    }
}
    @Composable
    fun WhereToGoTextField(navController: NavController) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clickable {
                    navController.navigate("searchScreen")  // 👈 navigate to your search screen
                }
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Where to go?",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }




@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
    val navController = rememberNavController()
    StartupScreen(navController)
}