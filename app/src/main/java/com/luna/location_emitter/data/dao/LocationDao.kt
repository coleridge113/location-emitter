package com.luna.location_emitter.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.luna.location_emitter.data.entity.LocationEntity

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocationData(entity: LocationEntity)

    @Query("SELECT * FROM location ORDER BY timestamp ASC")
    suspend fun getLocationData(): List<LocationEntity>

    @Query("DELETE FROM location")
    suspend fun flushDB()

}