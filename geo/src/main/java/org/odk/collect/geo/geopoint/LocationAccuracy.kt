package org.odk.collect.geo.geopoint

internal sealed class LocationAccuracy {

    abstract val value: Float
    abstract val provider: String?

    data class Improving @JvmOverloads constructor(
        override val value: Float,
        override val provider: String? = null
    ) : LocationAccuracy()

    data class Poor @JvmOverloads constructor(
        override val value: Float,
        override val provider: String? = null
    ) : LocationAccuracy()

    data class Unacceptable @JvmOverloads constructor(
        override val value: Float,
        override val provider: String? = null
    ) : LocationAccuracy()
}
