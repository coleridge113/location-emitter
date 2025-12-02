package com.luna.location_emitter.utils

import android.util.Log
import android.content.Context
import com.luna.location_emitter.data.LocationEntity
import com.luna.location_emitter.data.Repository
import com.luna.location_emitter.model.LocationData
import com.pusher.client.connection.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlin.lazy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.System

class RouteEmitter2(
    private val context: Context,
    private val repository: Repository
) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val routePoints: List<Pair<Double, Double>> by lazy {
        loadRoutePoints()
    }

    private var job: Job? = null

    fun start() {
        var idx = 0
        job = scope.launch {
            while(idx < routePoints.size) {
                val (lng, lat) = routePoints[idx]
                val loc = LocationData(
                    type = "point",
                    seq = idx,
                    latitude = lat,
                    longitude = lng,
                    timestamp = System.currentTimeMillis()
                )

                if (
                    PusherClient.subscribedChannel?.isSubscribed == true
                    && PusherClient.pusher.connection.state == ConnectionState.CONNECTED
                ) {
                    PusherClient.triggerClientEvent(loc.toString())
                } else {
                    repository.insertLocationData(
                        LocationEntity(
                            type = loc.type,
                            seq = loc.seq,
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            timestamp = loc.timestamp
                        )
                    )
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun onPusherResubscribed() {
        scope.launch {
            pusherDumpFlushOnRecon()
        }
    }

    private suspend fun pusherDumpFlushOnRecon() {
        val storedRoutePoints = repository.getLocationData()
        repository.flushDB()

        storedRoutePoints.forEach {
            PusherClient.triggerClientEvent(it.toString())
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
}
