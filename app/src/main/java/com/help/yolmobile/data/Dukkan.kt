package com.help.yolmobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoLocation(
    val coordinates: List<Double>? = null // Nullable yapıldı
) {
    val longitude: Double
        get() = if (coordinates?.isNotEmpty() == true) coordinates[0] else 0.0
    val latitude: Double
        get() = if (coordinates?.size ?: 0 > 1) coordinates?.get(1) ?: 0.0 else 0.0
}

@Serializable
data class Dukkan(
    val id: Int,
    @SerialName("name")
    val isim: String,
    @SerialName("primaryCategory")
    val kategori: String,
    @SerialName("location")
    val geoLocation: GeoLocation
) {
    val latitude: Double
        get() = geoLocation.latitude
    val longitude: Double
        get() = geoLocation.longitude
}
