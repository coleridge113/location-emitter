package com.luna.location_emitter.data.repository

import com.luna.location_emitter.data.dao.LocationDao
import com.luna.location_emitter.data.entity.LocationEntity

class RepositoryImpl(
    private val locationDao: LocationDao
) : Repository {

    override suspend fun insertLocationData(entity: LocationEntity) {
        locationDao.insertLocationData(entity)
    }

    override suspend fun getLocationData(): List<LocationEntity> {
        return locationDao.getLocationData()
    }

    override suspend fun flushDB() {
        return locationDao.flushDB()
    }
}
