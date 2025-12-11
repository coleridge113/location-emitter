package com.luna.location_emitter.utils.radar

import io.radar.sdk.Radar
import io.radar.sdk.RadarTrackingOptions
import io.radar.sdk.RadarTripOptions
import org.json.JSONObject
import android.util.Log

object RadarTrip {
    var externalId: String? = null
    const val destinationGeofenceExternalId = "2"
    const val destinationGeofenceTag = "dest"
    val metadata = JSONObject()

    init {
        metadata.put("Customer Name","Jacob Pena")
        metadata.put("Car Model","Green Honda Civic")
    }

    val mode = Radar.RadarRouteMode.CAR
    var tripOptions: RadarTripOptions? = null

    fun setTripData(
        input: String,
        userId: String = "metromart-user"
    ) {
        externalId = input
        Radar.setUserId(userId)

        tripOptions = RadarTripOptions(
            externalId!!,
            metadata,
            destinationGeofenceTag,
            destinationGeofenceExternalId,
            mode
        )
    }

    fun start() {
        Radar.startTrip(tripOptions!!, RadarTrackingOptions.CONTINUOUS) { status, trip, events ->
            if (status == Radar.RadarStatus.SUCCESS) {
                Log.d("RadarTrip", "STATUS: $status")
                Log.d("RadarTrip", "TRIP: $trip")
                Log.d("RadarTrip", "EVENTS: $events")
            } else {
                Log.d("RadarTrip", "No success")
            }
        }
    }

    fun stop() {
        Radar.completeTrip { status, trip, events ->
            Log.d("RadarTrip-STOP", "STATUS: $status")
            Log.d("RadarTrip-STOP", "TRIP: $trip")
            Log.d("RadarTrip-STOP", "EVENTS: $events")
        }
    }
}
