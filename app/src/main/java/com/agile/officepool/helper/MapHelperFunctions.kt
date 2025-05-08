package com.agile.officepool.helper

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.agile.officepool.BuildConfig
import com.google.android.gms.maps.model.Cap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object MapHelperFunctions {

    data class RouteInfo(
        val polyline: List<LatLng>,
        val durationText: String,
        val distanceText: String,
        val steps: List<String>
    )

    // Initialize the Places Client
    fun initializePlacesClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }

    suspend fun getRoutePolylineWithInfo(source: LatLng, destination: LatLng, apiKey: String): RouteInfo {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${source.latitude},${source.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&key=$apiKey"

                val response = URL(url).readText()
                Log.d("LiveTrackingMapp", "Response: $response")
                val json = JSONObject(response)
                val routes = json.getJSONArray("routes")

                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val polyline = route
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    val leg = route.getJSONArray("legs").getJSONObject(0)
                    val durationText = leg.getJSONObject("duration").getString("text")
                    val distanceText = leg.getJSONObject("distance").getString("text")
                    val stepsJson = leg.getJSONArray("steps")
                    val steps = mutableListOf<String>()
                    for (i in 0 until stepsJson.length()) {
                        val step = stepsJson.getJSONObject(i)
                        val htmlInstruction = step.getString("html_instructions")
                        val cleanText = htmlInstruction.replace(Regex("<[^>]*>"), "")
                        steps.add(cleanText)
                    }

                    RouteInfo(
                        polyline = decodePolyline(polyline),
                        durationText = durationText,
                        distanceText = distanceText,
                        steps = steps,

                        )
                } else {
                    RouteInfo(emptyList(), "","", emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                RouteInfo(emptyList(), "", "",emptyList())
            }
        }
    }

    // Helper composable to draw polylines
    @Composable
    fun DrawPolyline(points: List<LatLng>, color: Color, width: Float, startCap: Cap?, endCap: Cap?) {
        if (points.isNotEmpty()) {
            Polyline(
                points = points,
                color = color,
                width = width,
                startCap = startCap!!,
                endCap = endCap!!,
                jointType = JointType.DEFAULT
            )
        }
    }

    suspend fun fetchRoute(source: LatLng, destination: LatLng): List<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.MAPS_API_KEY
                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=${source.latitude},${source.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&key=$apiKey"

                val response = URL(url).readText()
                parseRoute(response)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun parseRoute(response: String): List<LatLng> {
        val json = JSONObject(response)
        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) return emptyList()

        val overviewPolyline = routes.getJSONObject(0)
            .getJSONObject("overview_polyline")
            .getString("points")

        return decodePolyline(overviewPolyline)
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        Log.d("LiveTrackingMap", "Decoding polyline")
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }

        return poly
    }




}