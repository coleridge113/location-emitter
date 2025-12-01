package com.luna.location_emitter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.luna.location_emitter.presentation.MainScreen
import com.luna.location_emitter.ui.theme.LocationEmitterTheme
import com.luna.location_emitter.utils.Ably
import com.luna.location_emitter.utils.PusherClient
import com.luna.location_emitter.utils.RouteEmitter
import io.ably.lib.realtime.Channel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val routeEmitter = RouteEmitter(this)

        val channel: Channel = Ably.realtime.channels.get("ably-channel")
        PusherClient.connectAndSubscribe()
        channel.subscribe("ably-route") { msg ->
            Log.d(
                "AblyStuff",
                "MainActivity received Ably msg: name=${msg.name}, data=${msg.data}"
            )
        }
        channel.publish("ably-route", "Hello Android!")

        setContent {
            LocationEmitterTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStartEmitting = { routeEmitter.start() },
                        onStopEmitting = { routeEmitter.stop() }
                    )
                }
            }
        }
    }
}
