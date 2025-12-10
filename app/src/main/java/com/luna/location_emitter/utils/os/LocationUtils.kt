package com.luna.location_emitter.utils.os

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper
import android.content.Context
import android.util.Log

fun requestPriorityGPS(context: Context) {
    val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L
    ).build()
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.requestLocationUpdates(
        request,
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                Log.d("LocationRes", "LOCATION RES: $result")
            }
        },
        Looper.getMainLooper()
    )
}
