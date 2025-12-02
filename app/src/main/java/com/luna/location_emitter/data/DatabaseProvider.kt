package com.luna.location_emitter.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun init(context: Context) {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "location_emitter.db"
                    ).build()
                }
            }
        }
    }

    fun get(): AppDatabase {
        return checkNotNull(instance) {
            "DatabaseProvider not initialized. Call DatabaseProvider.init(context) in Application.onCreate()"
        }
    }
}
