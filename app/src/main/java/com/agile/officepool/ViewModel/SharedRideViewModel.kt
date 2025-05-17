// SharedRideViewModel.kt
package com.agile.officepool.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.agile.officepool.model.RideInfo

class SharedRideViewModel : ViewModel() {
    private val _rideInfo = mutableStateOf<RideInfo?>(null)
    val rideInfo: State<RideInfo?> = _rideInfo

    fun updateRideInfo(info: RideInfo) {
        _rideInfo.value = info
    }
}


