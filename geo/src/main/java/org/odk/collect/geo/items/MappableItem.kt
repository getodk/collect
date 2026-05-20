package org.odk.collect.geo.items

import androidx.lifecycle.LiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.maps.MapPoint

sealed class MappableItem {
    abstract val id: Long
    abstract val name: String
    abstract val properties: List<IconifiedText>
    abstract val info: String?
    abstract val action: IconifiedText?
    abstract val status: Status?

    data class Point(
        override val id: Long,
        override val name: String,
        override val properties: List<IconifiedText> = emptyList(),
        override val info: String? = null,
        override val action: IconifiedText? = null,
        override val status: Status? = null,
        val point: MapPoint,
        val smallIcon: Int,
        val largeIcon: Int,
        val color: String? = null,
        val symbol: String? = null
    ) : MappableItem()

    data class Line(
        override val id: Long,
        override val name: String,
        override val properties: List<IconifiedText> = emptyList(),
        override val info: String? = null,
        override val action: IconifiedText? = null,
        override val status: Status? = null,
        val points: List<MapPoint>,
        val strokeWidth: String? = null,
        val strokeColor: String? = null
    ) : MappableItem()

    data class Polygon(
        override val id: Long,
        override val name: String,
        override val properties: List<IconifiedText> = emptyList(),
        override val info: String? = null,
        override val action: IconifiedText? = null,
        override val status: Status? = null,
        val points: List<MapPoint>,
        val strokeWidth: String? = null,
        val strokeColor: String? = null,
        val fillColor: String? = null
    ) : MappableItem()
}

data class IconifiedText(val icon: Int?, val text: String)

enum class Status { ERRORS, NO_ERRORS }

interface MappableData {
    fun getMappableItems(): LiveData<List<MappableItem>?>
    fun isLoading(): NonNullLiveData<Boolean>
}
