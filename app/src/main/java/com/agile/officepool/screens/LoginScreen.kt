package com.agile.officepool.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.agile.officepool.helper.ApplicationHelper.isLocationPermissionGranted
import com.agile.officepool.helper.ApplicationHelper.loginUser
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.agile.officepool.ui.theme.RobotoCondensed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                modifier = Modifier.padding(24.dp).fillMaxWidth().imePadding() // 👈 This handles keyboard appearance
            ) {
                Image(
                    painter = painterResource(R.drawable.car),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(70.dp),
                )


                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email", fontSize = 14.sp,
                        fontFamily = RobotoCondensed) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password", fontSize = 14.sp,
                        fontFamily = RobotoCondensed) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(15.dp))

                if (error.value.isNotEmpty()) {
                    Text(
                        text = error.value,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp),
                        fontSize = 14.sp,
                        fontFamily = RobotoCondensed
                    )
                }


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

                                    if(!isLocationPermissionGranted(context)){
                                        navController.navigate("locationPermission") {
                                            popUpTo("login") { inclusive = true } // Clear login screen from back stack
                                        }
                                        return@loginUser
                                    }else{
                                        navController.navigate("startUp") {
                                            popUpTo("login") { inclusive = true } // Clear login screen from back stack
                                        }
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
                                fontFamily = RobotoCondensed

                                )

                        )
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                ClickableText(
                    text = buildAnnotatedString  {withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.inverseSurface,
                        fontFamily = RobotoCondensed)){
                        append("Don't have an account? ")
                    }

                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary,
                            fontFamily = RobotoCondensed)) {
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