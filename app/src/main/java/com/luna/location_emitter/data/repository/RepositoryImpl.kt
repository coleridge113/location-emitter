package com.luna.location_emitter.data.repository

import com.luna.location_emitter.data.database.DatabaseProvider
import com.luna.location_emitter.data.entity.LocationEntity

class RepositoryImpl : Repository {

    private val db = DatabaseProvider.get()
    private val dao = db.locationDao()

    override suspend fun insertLocationData(entity: LocationEntity) {
        dao.insertLocationData(entity)
    }

    override suspend fun getLocationData(): List<LocationEntity> {
        return dao.getLocationData()
    }

    override suspend fun flushDB() {
        return dao.flushDB()
    }
}
