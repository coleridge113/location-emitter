package com.luna.location_emitter.data.remote

import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import com.luna.location_emitter.data.dto.LocationPayload

interface TrackingApi {

    @POST("/tracking/batch")
    suspend fun sendBatch(
        @Body payload: List<LocationPayload>
    ): Response<Unit>

}
