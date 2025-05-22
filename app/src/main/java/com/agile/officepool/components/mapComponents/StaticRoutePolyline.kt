package com.agile.officepool.components.mapComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.agile.officepool.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline

@Composable
fun StaticRoutePolyline(polyline: List<LatLng>) {
    if (polyline.isNotEmpty()) {
        val startCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.start), 25f)
        val endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.dest), 25f)

        Polyline(
            points = polyline,
            color = Color.Black,
            width = 12f,
            startCap = startCap,
            endCap = endCap
        )
    }
}
