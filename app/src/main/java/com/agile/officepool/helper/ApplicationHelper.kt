package com.agile.officepool.helper

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.agile.officepool.model.FcmTokenRequest
import com.agile.officepool.model.LoginRequest
import com.agile.officepool.model.RegisterRequest
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


object ApplicationHelper{

    //login user
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
                    sessionManager.setCompanyName(response.body()!!.user.companyName)
                    sessionManager.setLinkedInId(response.body()!!.user.linkedInId)
                    sessionManager.saveUserSession(email) // ✅ Save session

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        onSuccess()
//                    updateFcmTokenAfterLogin(context)
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

    // register user
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
                        updateFcmTokenAfterLoginOrRegister(context)
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

    //logout user
    suspend fun logoutUser(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.logout()

                Log.d("LOGOUT_RESPONSE", "Response Code: ${response.code()} | Body: ${response.body()}")

                if (response.isSuccessful) {
                    val jsonResponse = response.body()?.toString()  // Read response as string
                    val message = JSONObject(jsonResponse.toString()).optString("message", "Logout successful")

                    withContext(Dispatchers.Main) {
                       onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    withContext(Dispatchers.Main) {
                        onError("Logout failed: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Network error: ${e.message}")
                }
            }
        }
    }


    private fun updateFcmTokenAfterLoginOrRegister(context: Context) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful){
                val token = task.result
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
            else {
                Log.e("FCM", "Failed to fetch token", task.exception)
            }

        }
    }




    @Composable
    fun ShowDatePicker(context: Context, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
        val datePickerDialog = remember {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    onDateSelected(pickedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        // You can use a clickable component to show the DatePicker
        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
        ) {
            Text(
                text = "Select Date",  // Or your label here
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(8.5f),
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))?: "", // Display the formatted date or a default string
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    modifier = Modifier.weight(1.5f),
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    @Composable
    fun CustomTimePicker(
        label: String,
        selectedTime: LocalTime?,
        onTimeSelected: (LocalTime) -> Unit
    ) {
        val context = LocalContext.current
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val timePickerDialog = remember {
            TimePickerDialog(
                context,
                { _, hour: Int, minute: Int ->
                    val selected = LocalTime.of(hour, minute)
                    onTimeSelected(selected)
                },
                selectedTime?.hour ?: 12,
                selectedTime?.minute ?: 0,
                false
            )
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { timePickerDialog.show() }
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    modifier = Modifier.weight(8.5f),
                    text = selectedTime?.format(formatter) ?: "",
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    modifier = Modifier.weight(1.5f),
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return (fineLocation == PackageManager.PERMISSION_GRANTED ||
                coarseLocation == PackageManager.PERMISSION_GRANTED) &&
                (isGpsEnabled || isNetworkEnabled)
    }

    fun checkLocationEnabled(
        activity: Activity,
        navController: NavHostController,
        launcher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // 🎯 GPS is already ON
            navController.navigate("startUp") {
                popUpTo("locationPermission") { inclusive = true }
            }
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                    launcher.launch(intentSenderRequest)
                } catch (sendEx: Exception) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)

        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 100,
            pendingIntent
        )

        // Kill the current process
        if (context is Activity) {
            context.finishAffinity() // Optional: finish all activities
        }
        Runtime.getRuntime().exit(0)
    }

}

