package com.agile.officepool.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
fun RideRequestCardShimmer() {
    val shimmerInstance = rememberShimmer(ShimmerBounds.View)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shimmer(shimmerInstance)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.6f)
                    .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.4f)
                    .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    Spacer(
                        modifier = Modifier
                            .height(36.dp)
                            .weight(1f)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}
