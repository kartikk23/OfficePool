package com.agile.officepool.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agile.officepool.model.RideRequest
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.ViewModel.RideRequestUIState
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.ui.theme.RobotoCondensed


@Composable
fun RideRequestCard(
    state: RideRequestUIState,
    onAccept: (RideRequest) -> Unit,
    onReject: (RideRequest) -> Unit,
    onStart: (RideRequest) -> Unit,
    navController: NavController
) {
    val request = state.rideRequest
    val status = request.requestStatus.uppercase()
    val isLoading = state.isLoading

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.passengerName.trim().replaceFirstChar(Char::uppercase),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ride ID: ${request.rideId}", style = MaterialTheme.typography.bodyMedium)
                }

                StatusChip(
                    text = request.requestStatus.lowercase().replaceFirstChar(Char::uppercase),
                    color = when (status) {
                        "ACCEPTED" -> MaterialTheme.colorScheme.primary
                        "REJECTED" -> MaterialTheme.colorScheme.error
                        "COMPLETED" -> Color.Gray
                        "ACTIVE" -> Color.Green
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            when (status) {
                "REQUESTED" -> {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp).align(Alignment.CenterHorizontally).padding(bottom = 10.dp), )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Button(
                                onClick = { onAccept(request) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            )
                            {
                                Text("Accept")
                            }
                            OutlinedButton(
                                onClick = { onReject(request) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp,MaterialTheme.colorScheme.error),
                            ){
                                Text("Reject",color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                "ACCEPTED" -> {
                    OutlinedButton(
                        onClick = { onStart(request) },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        border = BorderStroke(1.dp,MaterialTheme.colorScheme.primary),
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                        else Text("Start Ride")
                    }
                }

                "ACTIVE" -> {
                    OutlinedButton(
                        onClick = {
                            navController.navigate("liveTrackingMap/${request.rideId}/${request.id}")
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp,Color.Green),
                    ) {
                        Text("Ride in Progress", color = Color.Green)
                    }
                }
            }
        }
    }
}


@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                color = color,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                fontFamily = RobotoCondensed
            )
        }
    }
}




