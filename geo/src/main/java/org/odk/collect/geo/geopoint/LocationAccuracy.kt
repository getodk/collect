package org.odk.collect.geo.geopoint

internal sealed class LocationAccuracy {

    abstract val value: Float

    data class Improving(override val value: Float) : LocationAccuracy()
    data class Poor(override val value: Float) : LocationAccuracy()
    data class Unacceptable(override val value: Float) : LocationAccuracy()
}
