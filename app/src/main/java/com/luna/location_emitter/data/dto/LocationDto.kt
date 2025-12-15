package com.luna.location_emitter.data.dto

data class LocationPayload(
    val type: String,
    val seq: Int,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)
