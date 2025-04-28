package com.agile.officepool.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.agile.officepool.model.LoginRequest
import com.agile.officepool.network.FcmTokenRequest
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    var error = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),


    ) { innerPadding ->
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
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp),
                )


                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (error.value.isNotEmpty()) {
                    Text(
                        text = error.value,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.value.isEmpty() || password.value.isEmpty()) {
                            error.value = "Please fill all fields"
                        } else {
                            isLoading = true

                            error.value = ""

                            CoroutineScope(Dispatchers.IO).launch {
                                loginUser(email.value, password.value, context, {
                                    isLoading = false

                                    navController.navigate("startUp") {
                                        popUpTo("login") { inclusive = true } // Clear login screen from back stack
                                    }
                                }, {
                                    isLoading = false
                                    error.value = it
                                })
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading
                ) {

                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))

                    } else {
                        Text("Login",
                            Modifier.padding(0.dp,5.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,

                                )

                        )
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                ClickableText(
                    text = buildAnnotatedString  {withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.inverseSurface)){
                        append("Don't have an account? ")
                    }

                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Register here")
                        }
                    },
                    onClick = { navController.navigate("register")
                    {
                        popUpTo("login") { inclusive = true } // Remove LoginScreen from the back stack
                    }}
                )


            }
        }
    }


}



suspend fun loginUser(
    email: String,
    password: String,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val sessionManager = SessionManager(context)
    withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.loginUser(LoginRequest(email, password))
            Log.d("API_RESPONSE", "Raw Response: ${response.body()}")

            if (response.isSuccessful && response.body() != null ) {

                val userId = response.body()!!.user.id.toLong()
                sessionManager.setUserId(userId)
                sessionManager.setUsername(response.body()!!.user.name)
                sessionManager.setUserPhone(response.body()!!.user.phone)
                sessionManager.saveUserSession(email) // ✅ Save session
                sessionManager.setCompanyName(response.body()!!.user.companyName)
                sessionManager.setLinkedInId(response.body()!!.user.linkedInId)



                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                    updateFcmTokenAfterLogin(context)
                }

            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Invalid email or password"
                    else -> "Login failed: ${response.message()}"
                }
                withContext(Dispatchers.Main) {
                    onError(errorMessage)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message}")
            }
        }
    }
}

fun updateFcmTokenAfterLogin(context: Context) {

    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_TOKEN_DEBUG", "Fetched FCM Token: $token")
            // use newToken here
            CoroutineScope(Dispatchers.IO).launch {
                val sessionManager = SessionManager(context)
                val userId = sessionManager.getUserId()
                Log.d("FCM_TOKEN_DEBUG", "SessionManager userId: $userId, FCM token: $token")
                if (userId != null && token != null) {
                    val response = RetrofitClient.instance.updateFcmToken(
                        FcmTokenRequest(userId = userId, token = token)
                    )
                    if (response.isSuccessful) {
                        Log.d("FCM_TOKEN_DEBUG", "FCM token updated post-login/registration: $token")
                    } else {
                        Log.e("FCM_TOKEN_DEBUG", "Failed to update token: ${response.errorBody()?.string()}")
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreenPreviewWrapper() {
    val navController = rememberNavController()
    OfficePoolTheme {
        LoginScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreenPreviewWrapper() // Use the wrapper function
}