package com.luna.location_emitter.utils.os

import android.Manifest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import io.radar.sdk.Radar

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

                result.lastLocation?.let {
                    Radar.trackOnce(it) { status, location, events, user ->
                        Log.d("RadarGPS", "STATUS: $status")
                        Log.d("RadarGPS", "LOCATION: $location")
                        Log.d("RadarGPS", "EVENTS: $events")
                        Log.d("RadarGPS", "USER: ${user?.userId}")
                    }
                }
            }
        },
        Looper.getMainLooper()
    )
}
