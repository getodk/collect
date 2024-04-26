package org.odk.collect.geo.selection

import org.odk.collect.maps.MapPoint

sealed class MappableSelectItem {
    abstract val id: Long
    abstract val name: String
    abstract val properties: List<IconifiedText>
    abstract val selected: Boolean
    abstract val info: String?
    abstract val action: IconifiedText?
    abstract val status: Status?

    data class MappableSelectPoint(
        override val id: Long,
        override val name: String,
        override val properties: List<IconifiedText> = emptyList(),
        override val selected: Boolean = false,
        override val info: String? = null,
        override val action: IconifiedText? = null,
        override val status: Status? = null,
        val point: MapPoint,
        val smallIcon: Int,
        val largeIcon: Int,
        val color: String? = null,
        val symbol: String? = null
    ) : MappableSelectItem()

    data class MappableSelectLine(
        override val id: Long,
        override val name: String,
        override val properties: List<IconifiedText> = emptyList(),
        override val selected: Boolean = false,
        override val info: String? = null,
        override val action: IconifiedText? = null,
        override val status: Status? = null,
        val points: List<MapPoint>,
        val strokeWidth: String? = null,
        val strokeColor: String? = null
    ) : MappableSelectItem()

    data class MappableSelectPolygon(
        override val id: Long,
        override val name: String,
        override val properties: List<IconifiedText> = emptyList(),
        override val selected: Boolean = false,
        override val info: String? = null,
        override val action: IconifiedText? = null,
        override val status: Status? = null,
        val points: List<MapPoint>,
        val strokeWidth: String? = null,
        val strokeColor: String? = null,
        val fillColor: String? = null
    ) : MappableSelectItem()
}

data class IconifiedText(val icon: Int?, val text: String)

enum class Status { ERRORS, NO_ERRORS }
