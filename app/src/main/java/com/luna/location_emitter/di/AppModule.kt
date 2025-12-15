package com.luna.location_emitter.di

import org.koin.dsl.module
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.luna.location_emitter.data.remote.TrackingApi
import java.util.concurrent.TimeUnit
import androidx.room.Room
import com.luna.location_emitter.data.database.AppDatabase
import org.koin.android.ext.koin.androidContext

val appModule = module {
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<TrackingApi> {
        get<Retrofit>().create(TrackingApi::class.java)
    }

    single {
        Room.databaseBuilder(
            androidContext(), 
            AppDatabase::class.java,
            "location_emitter.db" 
        ).build()
    }
}
