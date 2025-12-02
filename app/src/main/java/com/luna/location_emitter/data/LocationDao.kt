package com.luna.location_emitter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocationData(entity: LocationEntity)

    @Query("SELECT * FROM location ORDER BY timestamp ASC")
    suspend fun getLocationData(): List<LocationEntity>
}
