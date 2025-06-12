package com.agile.officepool.components.mapComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.agile.officepool.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline

@Composable
fun StaticRoutePolyline(polyline: List<LatLng>) {
    if (polyline.isNotEmpty()) {
        val startCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.start), 50f)
        val endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.dest), 50f)

        Polyline(
            points = polyline,
            color = Color.Black,
            width = 20f,
            startCap = startCap,
            endCap = endCap,

        )
    }
}
