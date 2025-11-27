package com.luna.base_jetpack_compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.luna.base_jetpack_compose.presentation.MainScreen
import com.luna.base_jetpack_compose.ui.theme.BasejetpackcomposeTheme
import com.luna.base_jetpack_compose.utils.Ably
import com.luna.base_jetpack_compose.utils.RouteEmitter
import io.ably.lib.realtime.Channel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val routeEmitter = RouteEmitter(this)

        val channel: Channel = Ably.realtime.channels.get("ably-channel")
        channel.subscribe("ably-route") { msg ->
            Log.d(
                "AblyStuff",
                "MainActivity received Ably msg: name=${msg.name}, data=${msg.data}"
            )
        }
        channel.publish("ably-route", "Hello Android!")

        setContent {
            BasejetpackcomposeTheme {
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
