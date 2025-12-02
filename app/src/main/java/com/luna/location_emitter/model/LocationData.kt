package com.luna.location_emitter.model

data class LocationData(
    val seq: Int,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
