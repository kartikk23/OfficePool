package com.agile.officepool.helper

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun isLocationPermissionGranted(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
