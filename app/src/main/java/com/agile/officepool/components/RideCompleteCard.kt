package com.agile.officepool.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.agile.officepool.ui.theme.RobotoCondensed


@Composable
fun RideCompletedCard(
    riderName: String = "Ajay Sharma",
    from: String = "Tech Park",
    to: String = "City Center",
    fareAmount: String = "₹120",
    upiId: String = "ajay.upi@bank",
    onPayClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.LightGray.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 20.dp)
            ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ride Completed",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoCondensed
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Rider Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Image(
                    imageVector = Icons.Default.Person, // replace with real drawable
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = riderName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.weight(3f),
                            text = from,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            modifier = Modifier.weight(1f), text = "→", color = Color.Gray)
                        Text(
                            modifier = Modifier.weight(3f),
                            text = to,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fare Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Fare Summary",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = fareAmount,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5),
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Pay to: $upiId",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pay Now via UPI",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Checkmark Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFF64B5F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}



@Preview(showBackground = true, apiLevel = 33)
@Composable
fun RideCompletedCardPreview() {
    OfficePoolTheme { // Or OfficePoolTheme if you have a custom one
        RideCompletedCard(
            riderName = "Ajay Sharma",
            from = "Tech Park",
            to = "City Center",
            fareAmount = "₹120",
            upiId = "ajay.upi@bank"
        )
    }
}

