package com.luna.location_emitter.data.remote

import retrofit2.http.POST
import retrofit2.http.Body

interface TrackingApi {

    @POST("/tracking/batch")
    suspend fun sendBatch(
        @Body payload: List<Map<String, Any?>>
    ): String

}
