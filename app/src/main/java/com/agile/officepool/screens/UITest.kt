package com.agile.officepool.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController



@Composable
fun UITest(navController: NavController){


    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.surface)
        .padding(0.dp)) {






    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 30.dp, 16.dp, 0.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(0.70f)
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

        Box(
            modifier = Modifier
                .weight(0.16f)
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

        Box(
            modifier = Modifier
                .weight(0.16f)
                .background(Color(0xFF161e33), shape = RoundedCornerShape(50.dp))
                .clickable { navController.navigate("rideRequests") }
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(10.dp, 12.dp)
                    .align(Alignment.Center)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    )


}



@Preview(showBackground = true)
@Composable
fun UIPreview() {
    val navController = rememberNavController()
    UITest(navController)
}