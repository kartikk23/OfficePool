package com.agile.officepool.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.agile.OfficePool.utils.SessionManager
import com.agile.officepool.MainActivity
import com.agile.officepool.R
import com.agile.officepool.model.FcmTokenRequest
import com.agile.officepool.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "OfficePool_Channel"
        private const val CHANNEL_NAME = "OfficePool Notifications"
    }

    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to backend (see section below)
        Log.d("FCM", "New token: $token")


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
        val data = remoteMessage.data
        val type = data["type"] // "ride_request" or "ride_accepted"

        when (type) {
            "ride_request" -> {
                val rideId = data["rideId"]
                val passengerName = data["passengerName"]
                showNotification(
                    title = "New Ride Request",
                    body = "$passengerName requested a ride for rideId $rideId!",
                    rideId = rideId,
                )
            }

            "request_response" -> {
                val passengerId = data["passengerId"]
                reqResNotification(
                    title = data["title"],
                    msg = data["msg"],
                )
            }


            "ride_started" -> {
                val rideId = data["rideId"]

                sessionManager.setHasRideStarted(true)
                // Optionally show notification
                rideStartedNotification(rideId = rideId)
                // Navigate user to live tracking screen if app is open
                sendBroadcast(Intent("RIDE_STARTED_EVENT").apply {
                    putExtra("rideId", rideId.toString())
                })
            }

            "ride_ended" -> {
                val rideId = data["rideId"]
                rideEndedNotification(rideId = rideId)

            }


            else -> {
                // Default notification if no type specified
                remoteMessage.notification?.let {
                    showNotification(it.title ?: "OfficePool", it.body ?: "", null, )
                }
            }

        }


    }




    private fun showNotification(title: String, body: String, rideId: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rideId", rideId)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.car)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1,notification)

    }

    private fun reqResNotification(title: String?, msg: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.car)
            .setContentTitle(title)
            .setContentText(msg)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }

    private fun rideStartedNotification(rideId: String?) {
        if (rideId == null) return

        createNotificationChannel()

        val route = "liveTrackingForPassenger/${rideId}" // Customize as per your navigation
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rideId", rideId)
            putExtra("target_route", route)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.car)
            .setContentTitle("Ride Started")
            .setContentText("Your ride has started!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(3, notification)
    }

    private fun rideEndedNotification(rideId: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rideId", rideId)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.car)
            .setContentTitle("Ride Ended...")
            .setContentText("Your ride has ended!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(4,notification)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
    }

}
