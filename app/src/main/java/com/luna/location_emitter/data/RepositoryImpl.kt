package com.luna.location_emitter.data

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
