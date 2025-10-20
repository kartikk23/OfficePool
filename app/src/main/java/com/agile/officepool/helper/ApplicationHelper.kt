package com.agile.officepool.helper

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.agile.officepool.model.*
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object ApplicationHelper {

    suspend fun loginUser(
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.loginUser(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                saveUserSession(context, responseBody.user, responseBody.tokenOrMessage)
                updateFcmToken(context)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Login failed: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message}")
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
    ) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.register(RegisterRequest(name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                saveUserSession(context, responseBody.user, responseBody.token) // ⬅️ Save token
                updateFcmToken(context)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Registration failed: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message}")
            }
        }
    }

    fun saveUserSession(context: Context, user: User, jwtToken: String) {
        val session = SessionManager(context)
        session.saveUserSession(user, jwtToken)
    }

    suspend fun logoutUser(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val session = SessionManager(context)
            val userId = session.getUserId()

            // Optional: clear FCM token on server side
            userId?.let {
                try {
                    RetrofitClient.instance.updateFcmToken(FcmTokenRequest(it, ""))
                } catch (_: Exception) {
                    // Log silently or handle if needed
                }
            }

            // Delete local FCM token
            FirebaseMessaging.getInstance().deleteToken().await()

            // Just clear local session — no need to call backend for logout
            session.clearSession()

            withContext(Dispatchers.Main) { onSuccess() }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onError("Logout failed: ${e.message}") }
        }
    }


    private suspend fun updateFcmToken(context: Context) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val session = SessionManager(context)
            session.getUserId()?.let {
                RetrofitClient.instance.updateFcmToken(FcmTokenRequest(it, token))
            }
        } catch (e: Exception) {
            Log.e("FCM_UPDATE", "Failed to update FCM token", e)
        }
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun checkLocationEnabled(
        activity: Activity,
        navController: NavHostController,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        onGpsAlreadyEnabled: () -> Unit
    ) {
        val request = LocationRequest.create().apply { priority = Priority.PRIORITY_HIGH_ACCURACY }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val task = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        task.addOnSuccessListener { onGpsAlreadyEnabled() }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    launcher.launch(IntentSenderRequest.Builder(it.resolution).build())
                } catch (_: IntentSender.SendIntentException) {}
            }
        }
    }

    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 100,
            pendingIntent
        )
        if (context is Activity) context.finishAffinity()
        Runtime.getRuntime().exit(0)
    }


}



