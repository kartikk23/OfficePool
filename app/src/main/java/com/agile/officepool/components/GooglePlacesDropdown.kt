import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agile.officepool.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlacesDropdown(
    label: String,
    placePredictions: List<AutocompletePrediction>,
    onPlaceSelected: (String, Double, Double) -> Unit,
    onSearch: (String) -> Unit,
    currentLocation: String,
    currentLat: Double?,
    currentLng: Double?
) {
    var selectedPlace by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    fun initializePlacesClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }

    val placesClient = remember { initializePlacesClient(context) }

    fun fetchPlaceDetails(placeId: String, onResult: (Double, Double) -> Unit) {
        val request = FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.LAT_LNG))
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                response.place.latLng?.let {
                    onResult(it.latitude, it.longitude)
                }
            }
    }

    // Handle input change and debouncing
    fun handleInputChange(newText: String) {
        selectedPlace = TextFieldValue(newText, TextRange(newText.length))
        expanded = newText.isNotEmpty() && placePredictions.isNotEmpty()

        debounceJob?.cancel()  // Cancel any previous debounced job
        debounceJob = coroutineScope.launch {
            delay(400)  // Wait for the debounce delay
            if (newText.length > 2) { // don't search until user has typed enough
                onSearch(newText)
                expanded = placePredictions.isNotEmpty()
            } else {
                expanded = false
            }
        }
    }

    // Handle clearing the text
    fun handleClear() {
        selectedPlace = TextFieldValue("")
        expanded = false
        debounceJob?.cancel()  // Cancel the previous debounce job
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
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
            onValueChange = {newText -> handleInputChange(newText.text) },
            readOnly = false,
            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedPlace.text.isNotEmpty() && placePredictions.isEmpty()) {
                        // Show clear icon when a place is selected (no predictions)
                        IconButton(onClick = { handleClear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    } else if (expanded) {
                        // Show dropdown icon when user is typing and the dropdown is expanded
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }
            },
            label = { Text(text = label, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true
        )

        if (placePredictions.isNotEmpty() && expanded) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Current Location Option
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.padding(end = 10.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Your Current Location", fontSize = 14.sp)
                        }
                    },
                    onClick = {
                        selectedPlace = TextFieldValue(currentLocation, TextRange(currentLocation.length))
                        expanded = false
                        if (currentLat != null && currentLng != null) {
                            onPlaceSelected(currentLocation, currentLat, currentLng)
                        }
                    }
                )

                // Autocomplete predictions
                placePredictions.forEach { prediction ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        text = {
                            Column {
                                Text(prediction.getPrimaryText(null).toString(), fontSize = 14.sp, maxLines = 1,overflow = TextOverflow.Ellipsis)
                                Text(prediction.getSecondaryText(null).toString(), fontSize = 12.sp, color = Color.Gray, maxLines = 1,overflow = TextOverflow.Ellipsis)
                            }
                        },
                        onClick = {
                            val selectedText = prediction.getPrimaryText(null).toString()
                            fetchPlaceDetails(prediction.placeId) { lat, lng ->
                                selectedPlace = TextFieldValue(selectedText, TextRange(selectedText.length))
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
