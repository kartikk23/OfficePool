package com.agile.officepool.screens

import android.util.Log
import com.agile.officepool.network.RetrofitClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.components.TopAppBarWithTitle
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
    val sessionManager = remember { SessionManager(context) }
    var email by remember { mutableStateOf(TextFieldValue(sessionManager.getUserEmail() ?: "")) }
    var name by remember { mutableStateOf(TextFieldValue(sessionManager.getUsername() ?: "")) }
    var phone by remember { mutableStateOf(TextFieldValue(sessionManager.getUserPhone() ?: "")) }
    var companyName by remember { mutableStateOf(TextFieldValue(sessionManager.getCompanyName() ?: "")) }
    var linkedInId by remember { mutableStateOf(TextFieldValue(sessionManager.getLinkedInId() ?: "")) }
    var upiId by remember { mutableStateOf(TextFieldValue(sessionManager.getUserUpiId() ?: "")) }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 15.dp)
    ) {
        TopAppBarWithTitle(
            title = "Update Profile",
            onBackClick = { navController.popBackStack() },
            showTrailingIcon = false,
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
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

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("Upi id") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                        isLoading = true
                        try {
                            val apiService = RetrofitClient.instance
                            val response = apiService.updateProfile(
                                ProfileRequest(
                                    email.text,
                                    name.text,
                                    phone.text,
                                    companyName.text,
                                    linkedInId.text,
                                    upiId.text
                                )
                            )
                            if (response.isSuccessful && response.body() != null) {
                                sessionManager.setUserEmail(email.text)
                                sessionManager.setUsername(name.text)
                                sessionManager.setUserPhone(phone.text)
                                sessionManager.setCompanyName(companyName.text)
                                sessionManager.setLinkedInId(linkedInId.text)
                                sessionManager.setUserUpiId(upiId.text)
                                Toast.makeText(
                                    context,
                                    "Profile updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("startUp")
                            } else {
                                val errorResponse = response.errorBody()?.string()
                                errorResponse?.let { Log.e("error", it) }
                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                        } catch (e: HttpException) {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161e33))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Update Profile", color = Color.White)
                }
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