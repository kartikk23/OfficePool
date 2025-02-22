import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import com.agile.officepool.screens.fetchUserLocation
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.model.AutocompletePrediction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlacesDropdown(
    label: String,
    placePredictions: List<AutocompletePrediction>,
    onPlaceSelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    currentLocation:String
) {
    var selectedPlace by remember { mutableStateOf(TextFieldValue("")) } // Stores text with cursor control
    var expanded by remember { mutableStateOf(false) } // Controls dropdown visibility
    val coroutineScope = rememberCoroutineScope()

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


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) expanded = false // Close dropdown when focus is lost
                },
            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseOnSurface),
            value = selectedPlace,
            onValueChange = { newText ->
                selectedPlace = newText.copy(selection = TextRange(newText.text.length)) // ✅ Move cursor to end
                expanded = newText.text.isNotEmpty() && placePredictions.isNotEmpty() // Expand only if text is typed
                coroutineScope.launch {
                    if (newText.text.isNotEmpty()) {
                        onSearch(newText.text) // Fetch places dynamically
                    }
                }
            },
            label = { Text(label) },
            readOnly = false, // Allow typing
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )



        // Dropdown menu for showing places
        if (placePredictions.isNotEmpty()) { // ✅ Show dropdown only if results exist
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // First option: "Use Current Location" (Always Present)
                DropdownMenuItem(
                    text = {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Your Current Location", style = TextStyle(fontSize = 14.sp))
                        }
                    },
                    onClick = {
                        val selectedText = currentLocation
                        selectedPlace = TextFieldValue(
                            text = selectedText,
                            selection = TextRange(selectedText.length) // ✅ Move cursor to the end
                        )
                        expanded = false // ✅ Close dropdown after selection
                        onPlaceSelected(selectedText) // Send selected place back
//                        onCurrentLocationSelected() // Handle current location selection
                    }
                )

                placePredictions.drop(1).forEach { prediction ->
                    DropdownMenuItem(
                        text = {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp) ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(prediction.getPrimaryText(null).toString(), style = TextStyle(fontSize = 14.sp))
                                    Text(prediction.getSecondaryText(null).toString(), style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                                }
                            }
                        },
                        onClick = {
                            val selectedText = prediction.getPrimaryText(null).toString()
                            selectedPlace = TextFieldValue(
                                text = selectedText,
                                selection = TextRange(selectedText.length) // ✅ Move cursor to the end
                            )
                            expanded = false // ✅ Close dropdown after selection
                            onPlaceSelected(selectedText) // Send selected place back
                        }
                    )
                }
            }
        }
    }
}
