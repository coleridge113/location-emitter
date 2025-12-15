package com.luna.location_emitter.data.repository

import com.luna.location_emitter.data.entity.LocationEntity

interface Repository {

    suspend fun insertLocationData(entity: LocationEntity)

    suspend fun getLocationData(): List<LocationEntity>

    suspend fun flushDB()
}
