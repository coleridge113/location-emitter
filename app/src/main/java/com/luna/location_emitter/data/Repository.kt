package com.luna.location_emitter.data

interface Repository {

    suspend fun insertLocationData(entity: LocationEntity)

    suspend fun getLocationData(): List<LocationEntity>
}
