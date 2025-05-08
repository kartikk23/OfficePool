package com.agile.officepool.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.components.TopAppBarWithTitle
import com.agile.officepool.helper.ApplicationHelper.logoutUser
import com.agile.officepool.helper.ApplicationHelper.restartApp
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, context: Context) {
    val sessionManager = remember { SessionManager(context) }

    // States to hold user profile values
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var linkedInId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load profile data once when screen is composed
    LaunchedEffect(Unit) {
        name = sessionManager.getUsername() ?: ""
        email = sessionManager.getUserEmail() ?: ""
        phone = sessionManager.getUserPhone() ?: ""
        companyName = sessionManager.getCompanyName() ?: ""
        linkedInId = sessionManager.getLinkedInId() ?: ""
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 15.dp)
    ) {
        TopAppBarWithTitle(
            title = "Profile",
            onBackClick = { navController.popBackStack() },
            showTrailingIcon = true,
            trailingIcon = Icons.Default.Create,
            onTrailingIconClick = {
                navController.navigate("updateProfile")
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Profile Name
            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            // Mobile Number
            Text(phone, fontSize = 16.sp, color = Color.Gray)

            // Email Address
            Text(email, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // Profile options
            ProfileOption(Icons.Default.Star, "Rider Rating", "4.8 ⭐")
            ProfileOption(Icons.Default.Phone, "Help", "Get support")
            ProfileOption(Icons.Default.LocationOn, "My Rides", "View ride history")
            ProfileOption(Icons.Default.Info, "Safety", "Safety preferences")
            ProfileOption(Icons.Default.Settings, "Settings", "Account settings")

            Spacer(modifier = Modifier.height(10.dp))

            Row {
                Button(
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            logoutUser(
                                context = context,
                                onSuccess = {
                                    sessionManager.clearSession() // ✅ Clear session token
                                    navController.navigate("login") {
                                        popUpTo("startUp") { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
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


// Profile Option with Icon
@Composable
fun ProfileOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color=MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileScreenPreviewWrapper() {
    val navController = rememberNavController()
    OfficePoolTheme {
        ProfileScreen(navController = navController, context = LocalContext.current)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreenPreviewWrapper() // Use the wrapper function
}