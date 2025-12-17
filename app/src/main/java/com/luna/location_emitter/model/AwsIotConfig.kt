package com.luna.location_emitter.model

data class AwsIotConfig(
    val identityPoolId: String,
    val region: String,
    val iotEndpoint: String,
    val deviceId: String
)
