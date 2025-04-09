package com.agile.officepool.screens

import android.util.Log
import com.agile.officepool.network.RetrofitClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.model.ProfileRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.agile.officepool.network.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(navController: NavController) {
    val context = LocalContext.current
//    var email by remember { mutableStateOf(TextFieldValue()) }
    val sessionManager = remember { SessionManager(context) }
    var email by remember { mutableStateOf(TextFieldValue(sessionManager.getUserEmail() ?: "")) }
    var name by remember { mutableStateOf(TextFieldValue()) }
    var phone by remember { mutableStateOf(TextFieldValue()) }
    var companyName by remember { mutableStateOf(TextFieldValue()) }
    var linkedInId by remember { mutableStateOf(TextFieldValue()) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF161e33),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.surface
                )
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = Color.Gray
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = Color.Gray
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = Color.Gray
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = linkedInId,
                onValueChange = { linkedInId = it },
                label = { Text("Linkedin id") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = Color.Gray
                ),
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val apiService = RetrofitClient.instance
                            val response = apiService.updateProfile(
                                ProfileRequest(
                                    email.text,
                                    name.text,
                                    phone.text,
                                    companyName.text,
                                    linkedInId.text
                                )
                            )
                            if (response.isSuccessful && response.body() != null) {
                                sessionManager.saveUserEmail(email.text)
                                sessionManager.saveUserName(name.text)
                                sessionManager.saveUserPhone(phone.text)
                                sessionManager.saveCompanyName(companyName.text)
                                sessionManager.saveLinkedInId(linkedInId.text)
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                val errorResponse = response.errorBody()?.string();
                                if (errorResponse != null) {
                                    Log.e("error",errorResponse)
                                };
                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                        } catch (e: HttpException) {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161e33))
            ) {
                Text("Update Profile", color = Color.White)
            }
        }
    }
}
@Composable
fun UpdateProfileScreenPreviewWrapper() {
    val navController = rememberNavController()
    OfficePoolTheme {
        UpdateProfileScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUpdateProfileScreen() {
    UpdateProfileScreenPreviewWrapper()  // Use the wrapper function
}