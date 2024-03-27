package org.odk.collect.geo.selection

import org.odk.collect.maps.MapPoint

data class MappableSelectItem(
    val id: Long,
    val points: List<MapPoint>,
    val smallIcon: Int,
    val largeIcon: Int,
    val name: String,
    val properties: List<IconifiedText> = emptyList(),
    val selected: Boolean = false,
    val color: String? = null,
    val symbol: String? = null,
    val info: String? = null,
    val action: IconifiedText? = null,
    val status: Status? = null
)

data class IconifiedText(val icon: Int?, val text: String)

enum class Status { ERRORS, NO_ERRORS }
