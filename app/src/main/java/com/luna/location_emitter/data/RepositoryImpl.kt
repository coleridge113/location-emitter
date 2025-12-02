package com.luna.location_emitter.data

class RepositoryImpl : Repository {

    private val db = DatabaseProvider.get()

    override suspend fun insertLocationData(entity: LocationEntity) {
        db.locationDao().insertLocationData(entity)
    }

    override suspend fun getLocationData(): List<LocationEntity> {
        return db.locationDao().getLocationData()
    }
}
