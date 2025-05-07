package com.agile.officepool.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import com.agile.officepool.MainActivity
import com.agile.officepool.R
import com.agile.officepool.network.FcmTokenRequest
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to backend (see section below)
        Log.d("FCM", "New token: $token")

        val sessionManager = SessionManager(applicationContext)
        // Fetch user ID and auth token from SharedPreferences or a secure store
        val userId = sessionManager.getUserId()


        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.updateFcmToken(
                        FcmTokenRequest(userId = userId, token = token)
                    )
                    if (response.isSuccessful) {
                        Log.d("FCM", "FCM token updated successfully")
                    } else {
                        Log.e("FCM", "Failed to update token: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Error sending FCM token", e)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val rideId = remoteMessage.data["rideId"]
        val passengerName = remoteMessage.data["passengerName"]

        showNotification(
            title = "New Ride Request",
            body = "$passengerName requested a ride!",
            rideId = rideId
        )
    }

    private fun showNotification(title: String, body: String, rideId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rideId", rideId)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ride_channel",
                "Ride Notifications",
                NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "ride_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(0,notification)

    }


}
