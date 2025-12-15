package com.luna.location_emitter.application

import android.app.Application
import com.luna.location_emitter.data.database.DatabaseProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.luna.location_emitter.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.init(this)
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}
