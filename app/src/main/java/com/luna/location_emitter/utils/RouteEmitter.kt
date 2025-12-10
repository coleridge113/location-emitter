package com.luna.location_emitter.utils

import android.content.Context
import android.util.Log
import android.location.Location
import com.luna.location_emitter.data.AppDatabase
import com.luna.location_emitter.data.LocationEntity
import com.luna.location_emitter.data.RepositoryImpl
import com.pusher.client.connection.ConnectionState
import io.ably.lib.realtime.Channel
import io.radar.sdk.Radar
import io.radar.sdk.RadarTrackingOptions
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

private const val ABLY_CHANNEL_NAME = "ably-channel"
private const val ABLY_EVENT_NAME = "ably-route"

class RouteEmitter(
    private val context: Context,
    private val repository: RepositoryImpl
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val channel: Channel by lazy {
        Ably.realtime.channels.get(ABLY_CHANNEL_NAME).also { ch ->
            ch.on { stateChange ->
                Log.d(
                    TAG,
                    "Android Channel[$ABLY_CHANNEL_NAME] state: ${stateChange.previous} -> ${stateChange.current}" +
                        (stateChange.reason?.let { " reason=${it.message}" } ?: "")
                )
            }
        }
    }
    
    private val route: List<Pair<Double, Double>> by lazy { loadRoutePoints() }

    @Volatile
    private var publishing: Boolean = false

    private var job: Job? = null

    fun start() {
        Log.d(TAG, "RouteEmitter.start() called. publishing=$publishing, routeSize=${route.size}")
        if (publishing) {
            Log.d(TAG, "Already publishing; ignoring start()")
            return
        }
        if (route.isEmpty()) {
            Log.w(TAG, "No route points loaded; nothing to emit")
            return
        }

        Radar.setUserId("metromart-user2")
        publishing = true
        job = scope.launch {
            Radar.startTracking(RadarTrackingOptions.CONTINUOUS)
            var idx = 0
            while (isActive && publishing && idx < route.size) {
                val (lng, lat) = route[idx]
                try {
                    val loc = Location("mock").apply {
                        latitude = lat
                        longitude = lng
                        accuracy = 5f
                        time = System.currentTimeMillis()
                    }

                    // Radar.trackOnce(loc) { status, location, events, user ->
                    //     Log.d("Radar", "STATUS: $status")
                    //     Log.d("Radar", "LOCATION: $location")
                    //     Log.d("Radar", "EVENTS: $events")
                    //     Log.d("Radar", "USER: ${user?.userId}")
                    // }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while publishing: ${e.message}", e)
                }

                idx++
                delay(1000L)
            }

            Log.d(TAG, "RouteEmitter finished route or stopped; idx=$idx")
        }
    }

    fun stop() {
        Log.d(TAG, "RouteEmitter.stop() called")
        publishing = false
        job?.cancel()
        job = null
    }

    fun destroy() {
        Log.d(TAG, "RouteEmitter.destroy() called")
        stop()
        scope.cancel()
    }

    fun onPusherResubscribed() {
        scope.launch {
            flushOfflineQueueIfNeeded()
        }
    }

    private fun loadRoutePoints(): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        return try {
            val inputStream = context.assets.open("location_data.txt1")
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val parts = trimmed.split(',')
                    if (parts.size != 2) return@forEach

                    val lat = parts[0].toDoubleOrNull()
                    val lng = parts[1].toDoubleOrNull()
                    if (lat != null && lng != null) {
                        // server.js: return [lng, lat]
                        result.add(lng to lat)
                    }
                }
            }

            Log.d(TAG, "Loaded ${result.size} points from assets/location_data.txt")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read location_data.txt: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun writeToLocal(payload: Map<String, Any>) {
        val connectionState = PusherClient.pusher.connection.state
        if (connectionState != ConnectionState.CONNECTED) {
            try {
                val loc = LocationEntity(
                    type = payload["type"] as String,
                    seq = (payload["seq"] as Number).toInt(),
                    latitude = (payload["lat"] as Number).toDouble(),
                    longitude = (payload["lng"] as Number).toDouble(),
                    timestamp = (payload["ts"] as Number).toLong()
                )

                repository.insertLocationData(loc)
                Log.d("PusherDB", "Successfully wrote to DB")
            } catch (e: Exception) {
                Log.d("PusherDB", "Failed to write: $e")
            }
        }
    }

    private suspend fun publishPusher(event: Map<String, Any>) {
        val connectionState = PusherClient.pusher.connection.state
        val isSubscribed = PusherClient.subscribedChannel?.isSubscribed
        if (connectionState == ConnectionState.CONNECTED && isSubscribed == true) {
            PusherClient.triggerClientEvent(event.toString())
        } else {
            writeToLocal(event)
        }
    }

    private suspend fun flushOfflineQueueIfNeeded() {
        val pending = repository.getLocationData()
        if (pending.isEmpty()) return

        Log.d("PusherDB", "Flushing ${pending.size} offline records")

        for (entity in pending) {
            val payload = entityToPayload(entity)
            try {
                PusherClient.triggerClientEvent(payload.toString())
            } catch (e: Exception) {
                Log.d("PusherDB", "Failed to send offline record seq=${entity.seq}: $e")
                return
            }
        }

        repository.flushDB()
        Log.d("PusherDB", "Offline records flushed and DB cleared")
    }
    private fun entityToPayload(entity: LocationEntity): Map<String, Any> {
        return mapOf(
            "type" to entity.type,
            "seq" to entity.seq,
            "lat" to entity.latitude,
            "lng" to entity.longitude,
            "ts" to entity.timestamp
        )
    }
}
