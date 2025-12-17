package com.luna.location_emitter.utils.aws

import android.content.Context
import android.util.Log
import com.luna.location_emitter.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.crt.CrtRuntimeException
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider
import software.amazon.awssdk.crt.io.ClientBootstrap
import software.amazon.awssdk.crt.io.TlsContext
import software.amazon.awssdk.crt.io.TlsContextOptions
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents
import software.amazon.awssdk.crt.mqtt.MqttMessage
import software.amazon.awssdk.crt.mqtt.QualityOfService
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient
import java.util.concurrent.CompletableFuture

const val TAG = "AwsMqttClient"

class AwsMqttClient(
    private val context: Context,
    private val identityPoolId: String,
    private val region: String,
    private val iotEndpoint: String,
    private val deviceId: String
) {
    private var connection: MqttClientConnection? = null
    private val httpClient = OkHttpClient()

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            val identityId = fetchIdentityId()
            Log.d(TAG, "Fetched Identity: $identityId")

            // Keep these alive as fields
            val tlsContext = TlsContext(TlsContextOptions.createDefaultClient())
            val bootstrap = ClientBootstrap.getOrCreateStaticDefault()

            val credentialsProvider = CognitoCredentialsProvider.CognitoCredentialsProviderBuilder()
                .withEndpoint("cognito-identity.ap-northeast-1.amazonaws.com")
                .withIdentity(identityId)
                .withTlsContext(tlsContext)
                .withClientBootstrap(bootstrap)
                .build()

            val builder = AwsIotMqttConnectionBuilder.newDefaultBuilder()
                .withEndpoint("a2894riu5taphn-ats.iot.ap-northeast-1.amazonaws.com"
) // your ATS endpoint
                .withWebsocketCredentialsProvider(credentialsProvider)
                .withClientId("android-$deviceId")
                .withCleanSession(true)
                .withConnectionEventCallbacks(object : MqttClientConnectionEvents {
                    override fun onConnectionInterrupted(errorCode: Int) {
                        Log.w(TAG, "MQTT Connection interrupted: $errorCode")
                    }

                    override fun onConnectionResumed(sessionPresent: Boolean) {
                        Log.d(TAG, "MQTT Session resumed!")
                    }
                })

            connection = builder.build()

            val connected = connection?.connect()?.get() ?: false
            Log.d(TAG, "MQTT connected: $connected")
            connected
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}", e)
            false
        }
    }

    suspend fun publish(location: LocationData) = withContext(Dispatchers.IO) {
        val conn = connection
        if (conn == null) {
            Log.e(TAG, "Connection is null")
            return@withContext false
        }

        val topic = "test/$deviceId/location"
        val payload = location.toString().toByteArray()
        val message = MqttMessage(topic, payload, QualityOfService.AT_LEAST_ONCE, false)
        
        return@withContext try {
            val publishFuture: CompletableFuture<Int> = conn.publish(message)
            publishFuture.get()
            Log.d(TAG, "Published to $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Publish failed ${e.message}")
            false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            connection?.disconnect()?.get()
            Log.d(TAG, "MQTT disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect")
        } finally {
            connection = null
        }
    }

    private suspend fun fetchIdentityId(): String = withContext(Dispatchers.IO) {
        val url = "https://cognito-identity.$region.amazonaws.com/"
        val mediaType = "application/x-amz-json-1.1".toMediaType()
        val bodyJson = JSONObject().put("IdentityPoolId", identityPoolId).toString()
        val requestBody = bodyJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/x-amz-json-1.1")
            .addHeader("X-Amz-Target", "AWSCognitoIdentityService.GetId")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("GetId failed: HTTP ${response.code}")
            }
            val respBody = response.body.string()
            val json = JSONObject(respBody)
            json.getString("IdentityId")
        }
    }

    private suspend fun fetchAwsCredentials(): AwsSessionCredentials {
        val provider = CognitoIdentityClient.builder()
            .region(Region.of(region))
            .build()
        
        val identityId = provider.getId {
            it.identityPoolId(identityPoolId)
        }.identityId()

        val creds = provider.getCredentialsForIdentity {
            it.identityId(identityId)
        }.credentials()

        return AwsSessionCredentials.create(
            creds.accessKeyId(),
            creds.secretKey(),
            creds.sessionToken()
        )
    }

    private fun buildSigV4Password(creds: AwsSessionCredentials): String {
        return "PLACEHOLDER_SIGV4_PASSWORD"
    }
}

