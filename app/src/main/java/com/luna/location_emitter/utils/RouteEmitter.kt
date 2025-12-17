package com.luna.location_emitter.utils

import android.content.Context
import android.util.Log
import android.location.Location
import com.luna.location_emitter.data.entity.LocationEntity
import com.luna.location_emitter.data.repository.Repository
import com.luna.location_emitter.model.LocationData
import com.luna.location_emitter.utils.aws.AwsMqttClient
import com.pusher.client.connection.ConnectionState
import io.ably.lib.realtime.Channel
import io.radar.sdk.Radar
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader


class RouteEmitter(
    private val context: Context,
    private val repository: Repository,
    private val mqttClient: AwsMqttClient
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val route: List<Pair<Double, Double>> by lazy { loadRoutePoints() }

    @Volatile
    private var publishing: Boolean = false

    private var job: Job? = null
    
    fun init() {
        scope.launch {
            mqttClient.connect()
        }
    }

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

        publishing = true
        job = scope.launch {
            var idx = 0
            while (isActive && publishing && idx < route.size) {
                val (lng, lat) = route[idx]
                try {
                    val loc = LocationData(
                        seq = idx,
                        type = "Point",
                        latitude = lat,
                        longitude = lng,
                        timestamp = System.currentTimeMillis()
                    )

                    mqttClient.publish(loc)

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
