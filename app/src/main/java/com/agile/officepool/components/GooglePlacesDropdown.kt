package com.agile.officepool.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agile.officepool.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun GooglePlacesDropdown(label: String, onPlaceSelected: (String) -> Unit) {
    val context = LocalContext.current
    val placesClient = remember { initializePlacesClient(context) }

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fetch places when searchQuery changes
    LaunchedEffect(searchQuery.text) {
        if (searchQuery.text.isNotEmpty()) {
            predictions = fetchPlacePredictions(searchQuery.text, placesClient)
            expanded = predictions.isNotEmpty()
        } else {
            predictions = emptyList()
            expanded = false
        }
    }

    Column(modifier = Modifier.padding(16.dp,0.dp,16.dp,5.dp)) {
        // Text Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true // Expand dropdown only when user is typing
            },
            label = { Text(label) },
            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseOnSurface),
            modifier = Modifier.fillMaxWidth().wrapContentHeight().focusRequester(focusRequester).onFocusChanged { expanded = it.hasFocus },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )

        // Dropdown menu
        if (expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(MaterialTheme.colorScheme.surface),

            ) {
                items(predictions) { prediction ->
                    DropdownMenuItem(
                        text = { Text(prediction.getPrimaryText(null).toString(),
                            style = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground),) },
                        onClick = {
                            searchQuery = TextFieldValue(text=prediction.getPrimaryText(null).toString(),
                                selection = TextRange(prediction.getPrimaryText(null).toString().length)
                            )
                            expanded = false
                            keyboardController?.hide() // ✅ Close keyboard
                            onPlaceSelected(searchQuery.text)
                        }
                    )
                }
            }
        }
    }
}

// Initialize the Places Client
fun initializePlacesClient(context: Context): PlacesClient {
    if (!Places.isInitialized()) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
    }
    return Places.createClient(context)
}

// Fetch place predictions from Google Places API
suspend fun fetchPlacePredictions(
    query: String,
    placesClient: PlacesClient
): List<AutocompletePrediction> {
    if (query.isEmpty()) return emptyList()

    return withContext(Dispatchers.IO) {
        try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictionsSuspended(request)
            response?.autocompletePredictions ?: emptyList()
        } catch (e: Exception) {
            emptyList() // Handle errors gracefully
        }
    }
}

// Extension function to make Google Places API suspendable
suspend fun PlacesClient.findAutocompletePredictionsSuspended(
    request: FindAutocompletePredictionsRequest
) = suspendCoroutine { continuation ->
    this.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            continuation.resume(response)
        }
        .addOnFailureListener { _ ->
            continuation.resume(null) // Return null on failure
        }
}
