package com.agile.officepool.components.mapComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.agile.officepool.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.Polyline

@Composable
fun DynamicRoutePolyline(polyline: List<LatLng>) {
    if (polyline.isNotEmpty()) {
        val movingCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.motorcyclet3), 50f)

        Polyline(
            points = polyline,
            color = Color.Blue,
            width = 20f,
            startCap = movingCap,
            endCap = RoundCap(),

        )
    }
}
