// SharedRideViewModel.kt
package com.agile.officepool.ViewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedRideViewModel : ViewModel() {
    private val _rideInfo = mutableStateOf<RideInfo?>(null)
    private var rideStatusListener: ValueEventListener? = null
    val rideInfo: State<RideInfo?> = _rideInfo

    private val _isRideActive = mutableStateOf<Boolean?>(null)
    val isRideActive: State<Boolean?> get() = _isRideActive

    private val _passengerId = mutableStateOf<String?>(null)
    val passengerId: String? get() = _passengerId.value

    fun setPassengerId(id: String) {
        _passengerId.value = id
    }


    fun setRideActive(status: Boolean?) {
        _isRideActive.value = status
    }


    fun updateRideInfo(info: RideInfo) {
        _rideInfo.value = info
    }

    fun getActiveRideForPassenger(passengerId: Long, onResult: (Int?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getActiveRideForPassengerId(passengerId)
                if (response.isSuccessful) {
                    Log.d("SharedRideVM", "Ride ID: ${response.body()}")
                    onResult(response.body())
                } else {
                    Log.e("SharedRideVM", "Error: ${response.code()}")
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("SharedRideVM", "Exception: ${e.message}")
                onResult(null)
            }
        }
    }


    fun observePassengerRideStatus(rideId: String, onStarted: () -> Unit,onNotActive: () -> Unit) {
        Log.d("RideStatusObserver", "observePassengerRideStatus called with rideId=$rideId")

        viewModelScope.launch(Dispatchers.IO) {
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
                        }else{
                            onNotActive()
                            removeRideStatusListener(rideId)
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
                onNotActive()
            }
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


