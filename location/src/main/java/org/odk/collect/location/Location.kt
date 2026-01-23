package org.odk.collect.location

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0.0f
)
