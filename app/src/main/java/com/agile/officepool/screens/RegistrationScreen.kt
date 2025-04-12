package com.agile.officepool.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.R
import com.agile.officepool.ViewModel.UserViewModel
import com.agile.officepool.components.TextWithLines
import com.agile.officepool.model.RegisterRequest
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current


    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Create New Account",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                                error = "Please fill all fields"
                            }
                            password != confirmPassword -> {
                                error = "Passwords don't match"
                            }
                            else -> {
                                isLoading = true
                                error = ""

                                CoroutineScope(Dispatchers.IO).launch {
                                    registerUser(fullName, email, password, context, {
                                        isLoading = false
                                        navController.navigate("home") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }, {
                                        isLoading = false
                                        error = it
                                    })
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            "Register",
                            Modifier.padding(0.dp, 4.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ClickableText(
                    text = buildAnnotatedString {withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.inverseSurface)){
                        append("Already have an account? ")
                    }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Login here")
                        }
                    },
                    onClick = { navController.navigate("login"){
                        popUpTo("register") { inclusive = true }
                    } }
                )
                Spacer(modifier = Modifier.height(12.dp))

//                TextWithLines("Or")

//                Spacer(modifier = Modifier.height(12.dp))
                // LinkedIn OAuth Login Button

            }
        }
    }
}


suspend fun registerUser(
    name: String,
    email: String,
    password: String,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {  // Run API call in the background
        try {
            val sessionManager = SessionManager(context)
            val response = RetrofitClient.instance.register(RegisterRequest(name, email, password))

            Log.d("API_RESPONSE", "Raw Response: ${response.body()}")  // ✅ Log the response

            if (response.isSuccessful && response.body() != null) {

                val userId = response.body()!!.user.id.toLong()
                sessionManager.setUserId(userId)
                sessionManager.setUsername(response.body()!!.user.name)
                sessionManager.saveUserSession(email) // ✅ Save session
//                sessionManager.setUserPhone(response.body()!!.user.phone ?: "")




                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                    updateFcmTokenAfterLogin(context)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_ERROR", "Error Body: $errorBody")  // ✅ Log error response

                val errorMessage = when (response.code()) {
                    409 -> "User already exists"
                    500 -> "Server error. Please try again later."
                    else -> "Registration failed: ${response.message()} \nError Body: $errorBody"
                }

                withContext(Dispatchers.Main) {
                    onError(errorMessage)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("API_EXCEPTION", "Exception: ${e.message}")
                onError("Network error: ${e.message}")
            }
        }
    }
}



// Function to handle LinkedIn OAuth flow

fun startLinkedInOAuth(context: Context) {
    val viewModel=  UserViewModel()

    // Implement LinkedIn OAuth logic here
    // This typically involves:
    // 1. Redirecting to LinkedIn's OAuth authorization URL
    // 2. Handling the callback with the authorization code
    // 3. Exchanging the code for an access token
    // 4. Navigating to the home screen or handling errors

    // Step 1: Generate the authorization URL
    val authUrl = viewModel.getAuthorizationUrl()


    // Step 2: Open the authorization URL in a browser
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
    context.startActivity(intent)

    // Step 3: Capture the redirect URL and extract the code




}


@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController)
}