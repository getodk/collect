package org.odk.collect.geo.selection

import org.odk.collect.maps.MapPoint

sealed interface MappableSelectItem {

    val id: Long
    val points: List<MapPoint>
    val smallIcon: Int
    val largeIcon: Int
    val name: String
    val properties: List<IconifiedText>
    val selected: Boolean
    val color: String?
    val symbol: String?

    data class WithInfo(
        override val id: Long,
        override val points: List<MapPoint>,
        override val smallIcon: Int,
        override val largeIcon: Int,
        override val name: String,
        override val properties: List<IconifiedText>,
        val info: String,
        override val selected: Boolean = false,
        override val color: String? = null,
        override val symbol: String? = null,
    ) : MappableSelectItem {

        constructor(
            id: Long,
            latitude: Double,
            longitude: Double,
            smallIcon: Int,
            largeIcon: Int,
            name: String,
            properties: List<IconifiedText>,
            info: String,
            selected: Boolean = false,
            color: String? = null,
            symbol: String? = null,
        ) : this(
            id,
            listOf(MapPoint(latitude, longitude)),
            smallIcon,
            largeIcon,
            name,
            properties,
            info,
            selected,
            color,
            symbol
        )
    }

    data class WithAction(
        override val id: Long,
        override val points: List<MapPoint>,
        override val smallIcon: Int,
        override val largeIcon: Int,
        override val name: String,
        override val properties: List<IconifiedText>,
        val action: IconifiedText,
        override val selected: Boolean = false,
        override val color: String? = null,
        override val symbol: String? = null,
    ) : MappableSelectItem {

        constructor(
            id: Long,
            latitude: Double,
            longitude: Double,
            smallIcon: Int,
            largeIcon: Int,
            name: String,
            properties: List<IconifiedText>,
            action: IconifiedText,
            selected: Boolean = false,
            color: String? = null,
            symbol: String? = null,
        ) : this(
            id,
            listOf(MapPoint(latitude, longitude)),
            smallIcon,
            largeIcon,
            name,
            properties,
            action,
            selected,
            color,
            symbol
        )
    }

    data class IconifiedText(val icon: Int?, val text: String)
}
