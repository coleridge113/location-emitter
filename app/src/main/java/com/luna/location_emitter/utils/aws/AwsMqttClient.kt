package com.luna.location_emitter.utils.aws

import android.content.Context
import com.luna.location_emitter.model.LocationData
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import software.amazon.awssdk.crt.mqtt.QualityOfService
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient
import software.amazon.awssdk.regions.Region

class AwsMqttClient(
    private val context: Context,
    private val identityPoolId: String,
    private val region: String,
    private val iotEndpoint: String,
    private val deviceId: String
) {
    private var mqttClient: MqttAsyncClient? = null

    suspend fun connectAndPublish(seq: Int, lat: Double, lng: Double) {
        val credentials = fetchAwsCredentials()

        val mqttConnectOptions = MqttConnectOptions().apply {
            isCleanSession = true
            userName = "AWSIoWebSocket"
            password = buildSigV4Password(credentials).toCharArray()
        }

        val clientId = "android-$deviceId"
        val serverUri = "wss://$iotEndpoint/mqtt"

        mqttClient = MqttAsyncClient(serverUri, clientId, MemoryPersistence())
        mqttClient?.connect(mqttConnectOptions)?.waitForCompletion()

        val payload = LocationData(
            seq = seq,
            type = "Point",
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis()
        ).toString()

        val topic = "test/$deviceId/location"
        val message = MqttMessage(payload.toByteArray())
        mqttClient?.publish(topic, message)
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

