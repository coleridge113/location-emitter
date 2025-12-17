package com.luna.location_emitter.utils

import com.luna.location_emitter.data.entity.LocationEntity
import com.luna.location_emitter.model.LocationData

fun LocationData.toEntity(): LocationEntity {
    return LocationEntity(
        type = type,
        seq = seq,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}

fun LocationEntity.toModel(): LocationData {
    return LocationData(
        type = type,
        seq = seq,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}
