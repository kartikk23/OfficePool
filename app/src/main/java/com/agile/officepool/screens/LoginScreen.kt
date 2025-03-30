package com.agile.officepool.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agile.officepool.R
import com.agile.officepool.components.CustomInputField
import com.agile.officepool.model.LoginRequest
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
//
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
                                    navController.navigate("home")
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
                        popUpTo("LoginScreen") { inclusive = true } // Remove LoginScreen from the back stack
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
    onSuccess: (String) -> Unit,  // ✅ Return session cookie
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.login(LoginRequest(email, password))

            Log.d("API_RESPONSE", "Raw Response: ${response.raw()}")  // ✅ Log the response

            if (response.isSuccessful && response.body() != null && response.headers()["Set-Cookie"] != null) {
                val user = response.body()!!.user
                val sessionCookie = response.headers()["Set-Cookie"]!!  // ✅ Extract session cookie

                saveUserSession(context, sessionCookie)  // ✅ Save cookie in SharedPreferences

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    onSuccess(sessionCookie)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_ERROR", "Error Body: $errorBody")  // ✅ Log error response

                val errorMessage = when (response.code()) {
                    401 -> "Invalid email or password"
                    500 -> "Server error. Please try again later."
                    else -> "Login failed: ${response.message()} \nError Body: $errorBody"
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



private fun saveUserSession(context: Context, email: String) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("user_email", email).apply()
}

