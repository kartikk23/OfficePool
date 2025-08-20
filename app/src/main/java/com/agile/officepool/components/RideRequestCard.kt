package com.agile.officepool.components


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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp
import com.agile.officepool.viewModel.RideRequestUIState
import com.agile.officepool.responseDTO.RideReqResponseDTO
import com.agile.officepool.ui.theme.RobotoCondensed
import java.util.Locale


@Composable
fun RideRequestCard(
    state: RideRequestUIState,
    onAccept: (RideReqResponseDTO) -> Unit,
    onReject: (RideReqResponseDTO) -> Unit,
    onStart: (RideReqResponseDTO) -> Unit,
    navController: NavController
) {
    val request = state.rideRequest
    val status = request.requestStatus.uppercase()
    val isLoading = state.isLoading

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardColors(
            containerColor = Color(0xFFF9F9FB),
            contentColor = Color.Black,
            disabledContainerColor = Color.Blue.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ) ,
        modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.passenger.name.replaceFirstChar(Char::uppercase),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Ride ID: ${request.ride.id}", style = MaterialTheme.typography.bodyMedium)
                }

                StatusChip(
                    text = request.requestStatus.lowercase().replaceFirstChar(Char::uppercase),
                    color = when (status) {
                        "ACCEPTED" -> Color(0xFF31965B)
                        "REJECTED" -> Color(0xFFEC5B4F)
                        "COMPLETED" -> Color(0xFF697179)
                        "ACTIVE" -> Color(0xFFDEEDE3)
                        else -> Color(color = 0xFF2B71C3)
                    }
                )
            }

            when (status) {
                "REQUESTED" -> {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp).align(Alignment.CenterHorizontally).padding(bottom = 10.dp) ,color = Color.White)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Button(
                                onClick = { onAccept(request) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonColors(
                                    containerColor = Color(color = 0xFF2B71C3),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Blue.copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                            )
                            {
                                Text("Accept", color = Color.White,fontFamily = RobotoCondensed)
                            }
                            OutlinedButton(
                                onClick = { onReject(request) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonColors(
                                    containerColor = Color(0xFFFCEAEE),
                                    contentColor = Color(0xFfC3232C),
                                    disabledContainerColor = Color.Blue.copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp,Color(0xFfC3232C)),
                            ){
                                Text("Reject",fontFamily = RobotoCondensed)
                            }
                        }
                    }
                }

                "ACCEPTED" -> {
                    Button(
                        onClick = { onStart(request) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonColors(
                            containerColor =  Color(0xFF31965B),
                            contentColor = Color.White,
                            disabledContainerColor =  Color(0xFF31965B).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                        enabled = !isLoading,
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                        else Text("Start Ride",fontFamily = RobotoCondensed)
                    }
                }

                "ACTIVE" -> {
                    Button(
                        onClick = {
                            navController.navigate("liveTrackingMap/${request.ride.id}/${request.id}")
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                       colors = ButtonColors(
                           containerColor = Color(0xFFDEEFE2),
                           contentColor = Color(0xFF31965B),
                           disabledContainerColor = Color.Blue.copy(alpha = 0.5f),
                           disabledContentColor = Color.White.copy(alpha = 0.5f)
                       ),
                    ) {
                        Text("Ride in Progress",fontFamily = RobotoCondensed)
                    }
                }
            }
        }
    }
}


@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = color,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,

        ) {
            Text(
                text = text.toUpperCase(Locale.ROOT),
                fontSize = 11.sp,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                fontFamily = RobotoCondensed
            )
        }
    }
}

//@Preview(showBackground = true, apiLevel = 33)
//@Composable
//fun RideRequestCardAcceptedPreview() {
//    val mockRequest = RideReqResponseDTO(
//        id = 333,
//        ride = RideInfoResponseDTO(
//            id = 1L,
//            source = "123 Main St",
//            destination = "456 Elm St",
//            rider = UserDTO(
//                id = 1L,
//                name = "John Doe",
//                email = "william.henry.harrison@example-pet-store.com",
//                linkedInId = "linkedin.com/in/johndoe",
//                fcmToken = "fcmToken123",
//                phone = "123-456-7890",
//                companyName = "OfficePool",
//                upiId = "upi123"
//            ),
//        ),
//        passenger = UserDTO(
//            id = 2L,
//            name = "John toe",
//            email = "henry.harrison@example-pet-store.com",
//            linkedInId = "linkedin.com/in/johntoe",
//            fcmToken = "fcmToken154",
//            phone = "123-456-7890",
//            companyName = "OfficePool2",
//            upiId = "upi1234"
//        ),
//        requestStatus = "Active",
//        rideFare =
//    )
//
//    val mockState = RideRequestUIState(
//        rideRequest = mockRequest,
//        isLoading = false
//    )
//
//    MaterialTheme {
//        RideRequestCard(
//            state = mockState,
//            onAccept = {},
//            onReject = {},
//            onStart = {},
//            navController = rememberNavController()
//        )
//    }
//}





