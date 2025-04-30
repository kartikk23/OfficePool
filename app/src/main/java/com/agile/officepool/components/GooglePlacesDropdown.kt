import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationDefaults.windowInsets

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import com.agile.officepool.BuildConfig
import com.agile.officepool.screens.fetchUserLocation
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlacesDropdown(
    label: String,
    placePredictions: List<AutocompletePrediction>,
    onPlaceSelected: (String, Double, Double) -> Unit, // Now returns name, lat, and lng
    onSearch: (String) -> Unit,
    currentLocation: String,
    currentLat: Double?,
    currentLng: Double?
) {
    var selectedPlace by remember { mutableStateOf(TextFieldValue("")) } // Stores text with cursor control
    var expanded by remember { mutableStateOf(false) } // Controls dropdown visibility
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun initializePlacesClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }
    val placesClient = remember { initializePlacesClient(context) }

    // Always include "Use Current Location" as the first option
    val modifiedPredictions = listOf<AutocompletePrediction?>(null) + placePredictions

    fun convertMetersToKm(meters: String): String {
        return if (meters.isNotEmpty()) {
            val km = meters.toDoubleOrNull()?.div(1000) ?: 0.0
            "%.2f km".format(km)
        } else {
            ""
        }
    }

    fun fetchPlaceDetails(placeId: String, onResult: (Double, Double) -> Unit) {
        val request = FetchPlaceRequest.newInstance(
            placeId,
            listOf(Place.Field.LAT_LNG)
        )

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val latLng = response.place.latLng
                if (latLng != null) {
                    onResult(latLng.latitude, latLng.longitude)
                }
            }
            .addOnFailureListener {
                // Handle errors (e.g., show a toast)
            }
    }



    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .menuAnchor()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) expanded = false
                },
            value = selectedPlace,
            onValueChange = { newText ->
                selectedPlace = newText.copy(selection = TextRange(newText.text.length))
                expanded = newText.text.isNotEmpty() && placePredictions.isNotEmpty()
                coroutineScope.launch {
                    if (newText.text.isNotEmpty()) {
                        onSearch(newText.text)
                    }
                }
            },
            readOnly = false,
            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            label = { Text(text = label, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedLabelColor = Color.Gray
            )
        )


        // Dropdown menu for showing places
        if (placePredictions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // ✅ First Option: Use Current Location
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Your Current Location", style = TextStyle(fontSize = 14.sp))
                        }
                    },
                    onClick = {
                        val selectedText = currentLocation
                        selectedPlace = TextFieldValue(
                            text = selectedText,
                            selection = TextRange(selectedText.length)
                        )
                        expanded = false
                        if (currentLat != null && currentLng != null) {
                            onPlaceSelected(selectedText, currentLat, currentLng)
                        }
                    }
                )

                // ✅ Predicted Places
                placePredictions.drop(1).forEach { prediction ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp, horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        prediction.getPrimaryText(null).toString(),
                                        style = TextStyle(fontSize = 14.sp)
                                    )
                                    Text(
                                        prediction.getSecondaryText(null).toString(),
                                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                                    )
                                }
                            }
                        },
                        onClick = {
                            val selectedText = prediction.getPrimaryText(null).toString()
                            val placeId = prediction.placeId

                            fetchPlaceDetails(placeId) { lat, lng ->
                                selectedPlace = TextFieldValue(
                                    text = selectedText,
                                    selection = TextRange(selectedText.length)
                                )
                                expanded = false
                                onPlaceSelected(selectedText, lat, lng)
                            }
                        }
                    )
                }
            }
        }
    }
}
