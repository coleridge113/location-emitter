package com.luna.location_emitter.application

import android.app.Application
import com.luna.location_emitter.data.DatabaseProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.init(this)
    }
}
