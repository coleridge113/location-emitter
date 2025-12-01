package com.luna.location_emitter.utils

import android.util.Log
import com.luna.location_emitter.BuildConfig
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object PusherClient {

    const val PRIVATE_CHANNEL_NAME = "private-psher-channel"
    const val EVENT_NAME = "client-psher-route"
    const val TAG = "PusherClient"

    private const val AUTH_URL = "http://192.168.100.70:3000/pusher/auth"
    // private const val AUTH_URL = "http://10.0.0.2.2:3000/pusher/auth"
    private val httpClient = OkHttpClient()
    private val options: PusherOptions = PusherOptions().apply {
        setCluster(BuildConfig.PUSHER_CLUSTER)

        setChannelAuthorizer { channelName, socketId ->
            try {
                val bodyJson = JSONObject().apply {
                    put("channel_name", channelName)
                    put("socket_id", socketId)
                }

                val body = bodyJson
                    .toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(AUTH_URL)
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        Log.e(TAG, "Pusher auth failed: HTTP ${resp.code}")
                        null
                    } else {
                        val authResponse = resp.body?.string()
                        Log.d(TAG, "Pusher auth response: $authResponse")
                        authResponse
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pusher auth error: ${e.message}", e)
                null
            }
        }
    }

    val pusher: Pusher = Pusher(BuildConfig.PUSHER_KEY, options)

    @Volatile
    var subscribedChannel: PrivateChannel? = null
    fun connectAndSubscribe(
        channelName: String = PRIVATE_CHANNEL_NAME,
        eventName: String = EVENT_NAME  // client event name
    ) {
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d(
                    TAG,
                    "Pusher connection state: ${change.previousState} -> ${change.currentState}"
                )
                if (change.currentState == ConnectionState.CONNECTED && subscribedChannel == null) {
                    subscribe(channelName, eventName)
                }
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                Log.e(
                    TAG,
                    "Pusher connection error: message=$message code=$code exception=${e?.message}",
                    e
                )
            }
        }, ConnectionState.ALL)
    }

    private fun subscribe(channelName: String, eventName: String) {
        val channel: PrivateChannel = pusher.subscribePrivate(
            channelName,
            object : PrivateChannelEventListener {
                override fun onSubscriptionSucceeded(channelName: String?) {
                    Log.d(TAG, "Subscribed to Pusher channel: $channelName")
                }

                override fun onEvent(event: PusherEvent?) {
                    Log.d(
                        TAG,
                        "Pusher event received: channel=${event?.channelName} " +
                            "event=${event?.eventName} data=${event?.data}"
                    )
                }

                override fun onAuthenticationFailure(
                    message: String?,
                    e: java.lang.Exception?
                ) {
                    Log.d("PusherConfig", "Authentication: $message, ${e?.message}")
                }
            },
            eventName
        )
        subscribedChannel = channel 

    }

    fun triggerClientEvent(data: String) {
        val channel: PrivateChannel? = subscribedChannel
        if (channel == null) {
            Log.d("Pusher", "Channel is null")
            return
        }
        
        try {
            channel.trigger(EVENT_NAME, data)
            Log.d("Pusher", "Sending data: $data")
        } catch(e: Exception) {
            Log.d("Pusher", "Error trigger: ${e.message}")
        }
    }

    fun disconnect() {
        pusher.disconnect()
    }
}
