package com.luna.location_emitter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luna.location_emitter.data.dao.LocationDao
import com.luna.location_emitter.data.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

}