package org.odk.collect.geo.geopoint

internal sealed class GeoPointAccuracy {

    abstract val value: Float

    data class Improving(override val value: Float) : GeoPointAccuracy()
    data class Poor(override val value: Float) : GeoPointAccuracy()
    data class Unacceptable(override val value: Float) : GeoPointAccuracy()
}
