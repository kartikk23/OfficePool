// SharedRideViewModel.kt
package com.agile.officepool.ViewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.agile.officepool.model.RideInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SharedRideViewModel : ViewModel() {
    private val _rideInfo = mutableStateOf<RideInfo?>(null)
    private var rideStatusListener: ValueEventListener? = null
    val rideInfo: State<RideInfo?> = _rideInfo

    fun updateRideInfo(info: RideInfo) {
        _rideInfo.value = info
    }


    fun observePassengerRideStatus(rideId: String, onStarted: () -> Unit) {
        Log.d("RideStatusObserver", "observePassengerRideStatus called with rideId=$rideId")

        try {
            val ref = FirebaseDatabase.getInstance()
                .getReference("riderLocations")
                .child(rideId)
                .child("status")

            Log.d("RideStatusObserver", "Firebase reference initialized: ${ref.path}")

            rideStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("RideStatusObserver", "onDataChange triggered")

                    val status = snapshot.getValue(String::class.java)
                    Log.d("RideStatusObserver", "Status received from Firebase: $status")

                    if (status == "Active") {
                        Log.d("RideStatusObserver", "Ride is Active, calling onStarted()")
                        onStarted()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RideStatusObserver", "Failed to read status: ${error.message}", error.toException())
                }
            }

            ref.addValueEventListener(rideStatusListener!!)
            Log.d("RideStatusObserver", "Listener added to Firebase reference")

        } catch (e: Exception) {
            Log.e("RideStatusObserver", "Exception in observePassengerRideStatus", e)
        }
    }


    fun removeRideStatusListener(rideId: String) {
        rideStatusListener?.let {
            FirebaseDatabase.getInstance()
                .getReference("riderLocations")
                .child(rideId)
                .child("status")
                .removeEventListener(it)
            rideStatusListener = null
        }
    }

}


