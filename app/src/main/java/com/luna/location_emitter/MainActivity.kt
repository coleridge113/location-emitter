package com.luna.location_emitter

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.luna.location_emitter.data.DatabaseProvider
import com.luna.location_emitter.data.RepositoryImpl
import com.luna.location_emitter.presentation.ButtonScreen
import com.luna.location_emitter.ui.theme.LocationEmitterTheme
import com.luna.location_emitter.utils.PusherClient
import com.luna.location_emitter.utils.RouteEmitter
import com.luna.location_emitter.utils.radar.MyRadarReceiver
import com.luna.location_emitter.utils.radar.RadarTrip
import com.luna.location_emitter.utils.os.requestPriorityGPS
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import io.radar.sdk.Radar
import io.radar.sdk.RadarInitializeOptions

class MainActivity : ComponentActivity() {
    private val foregroundLocationPermissionsRequestCode = 1
    private val backgroundLocationPermissionsRequestCode = 2

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val receiver = MyRadarReceiver()

        Radar.initialize(
            this,
            BuildConfig.RADAR_TEST_PUBLISHABLE, 
            RadarInitializeOptions(
                radarReceiver = receiver, 
                locationProvider = Radar.RadarLocationServicesProvider.GOOGLE
            )
        )

        requestLocationPermissions()
        requestPriorityGPS(this) 

        setContent {
            val repository = RepositoryImpl()
            val routeEmitter = RouteEmitter(
                context = this,
                repository = repository
            )

            PusherClient.onResubscribed = {
                routeEmitter.onPusherResubscribed()
            }

            PusherClient.connectAndSubscribe()

            LocationEmitterTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    ButtonScreen(
                        modifier = Modifier.padding(innerPadding),
                        // onStartEmitting = { routeEmitter.start() },
                        // onStopEmitting = { routeEmitter.stop() }
                        onStartEmitting = { RadarTrip.start() },
                        onStopEmitting = { RadarTrip.stop() }
                    )
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), backgroundLocationPermissionsRequestCode)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), foregroundLocationPermissionsRequestCode)
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == foregroundLocationPermissionsRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                AlertDialog.Builder(this)
                .setTitle("Background location permissions needed")
                .setMessage("Background location permission needed, tap \"Allow all the time\" on the next screen")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), backgroundLocationPermissionsRequestCode)
                }
                .create()
                .show()
            }
        }
    }
}
