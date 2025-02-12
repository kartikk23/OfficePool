package com.agile.officepool.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker

import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var source by remember { mutableStateOf(TextFieldValue("")) }
    var destination by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Set the background color
                    titleContentColor = MaterialTheme.colorScheme.onBackground, // Set the title text color
                    navigationIconContentColor = Color.White // Set the navigation icon color
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding for better spacing
                        verticalAlignment = Alignment.CenterVertically, // Align children vertically in the center
                        horizontalArrangement = Arrangement.SpaceBetween, // Space out children horizontally


                    ) {


                        Column(
                            modifier = Modifier.weight(1f) // Take up remaining space in the Row
                        )
                        {
                            BasicTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (name.text.isEmpty()) {
                                        Text("Tejas Katke",
                                            style = TextStyle(
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.W600

                                            ), )
                                    }
                                    innerTextField()
                                },
                                enabled = false // Disable editing
                            )
                            BasicTextField(
                                value = location,
                                onValueChange = { location = it },
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (location.text.isEmpty()) {
                                        Text("Current Location", style = TextStyle(

                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.W400

                                        ), )
                                    }
                                    innerTextField()
                                },
                                enabled = false // Disable editing
                            )
                        }
                        IconButton(onClick = {navController.navigate("login") }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile",Modifier.size(34.dp))
                        }
                    }
                },
//                navigationIcon = {
//
//                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("newScreen") },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Navigate")
            }
        },
        modifier = Modifier
            .fillMaxSize()



    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Placeholder for Map (Replace with your map implementation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ){
                GoogleMapView()
            }

            Box(
                modifier = Modifier
                    .wrapContentHeight()

                    .background(Color.Transparent,
                        shape = RoundedCornerShape(0.dp,0.dp,12.dp,12.dp)
                    ),


            ){
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(16.dp,20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // OutlinedTextField for Source
                    OutlinedTextField(
                        textStyle= TextStyle(

                        ),
                        value = source,
                        onValueChange = { source = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 5.dp),
                        label = { Text("Enter Source",)}, // Placeholder label
                        placeholder = {
                            Text(
                                "Enter Destination",
                                color = Color.Gray
                            )
                        }, // Gray placeholder text
                        singleLine = true // Restrict to single line
                    )

                    // OutlinedTextField for Destination
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter Destination") }, // Placeholder label
                        placeholder = {
                            Text(
                                "Enter Destination",
                                color = Color.Gray
                            )
                        }, // Gray placeholder text
                        singleLine = true // Restrict to single line
                    )
                }
            }

        }
    }
}

@Composable
fun NewScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("New Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun GoogleMapView() {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    // State to store the clicked location
    val clickedLocation = remember { mutableStateOf<LatLng?>(null) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            // Update the clicked location
            clickedLocation.value = latLng
        }
    ) {
        // Add a marker at the clicked location
        clickedLocation.value?.let { location ->
            Marker(

                title = "Clicked Location",
                snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}"
            )
        }
    }
}