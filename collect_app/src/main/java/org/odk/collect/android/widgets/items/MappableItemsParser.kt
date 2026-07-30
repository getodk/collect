package org.odk.collect.android.widgets.items

import org.javarosa.core.model.SelectChoice
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.FILL
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.GEOMETRY
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.MARKER_COLOR
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.MARKER_SYMBOL
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.STROKE
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.STROKE_WIDTH
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry
import org.odk.collect.geo.items.IconifiedText
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.icons.R
import org.odk.collect.maps.MapPoint

object MappableItemsParser {

    fun parseChoices(
        choices: List<SelectChoice>,
        options: Options = Options(),
        translator: (SelectChoice) -> String = { it.value }
    ): List<MappableItem> {
        return choices.mapIndexedNotNull { index, selectChoice ->
            val geometry = selectChoice.getChild(GEOMETRY)

            if (geometry != null) {
                val points = parseGeometry(geometry)
                if (points.isNotEmpty()) {
                    val withinBounds = points.all {
                        GeoWidgetUtils.isWithinMapBounds(it)
                    }

                    if (withinBounds) {
                        val properties = selectChoice.additionalChildren.filterNot {
                            FILTERED_PROPERTIES.contains(it.first)
                        }.map {
                            IconifiedText(null, DisplayString.Raw("${it.first}: ${it.second}"))
                        }

                        if (points.size == 1) {
                            parsePoint(selectChoice, index, translator, properties, points, options)
                        } else {
                            if (points.first() != points.last()) {
                                parseLine(
                                    selectChoice,
                                    index,
                                    translator,
                                    properties,
                                    points,
                                    options
                                )
                            } else {
                                parsePolygon(
                                    selectChoice,
                                    index,
                                    translator,
                                    properties,
                                    points,
                                    options
                                )
                            }
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun parsePolygon(
        selectChoice: SelectChoice,
        index: Int,
        translator: (SelectChoice) -> String,
        properties: List<IconifiedText>,
        points: List<MapPoint>,
        options: Options
    ): MappableItem.Polygon {
        val strokeColor = getPropertyValue(selectChoice, STROKE)
        val fillColor = getPropertyValue(selectChoice, FILL)
        return MappableItem.Polygon(
            index.toLong(),
            translator(selectChoice),
            properties,
            points = points,
            strokeWidth = getPropertyValue(selectChoice, STROKE_WIDTH),
            strokeColor = if (!strokeColor.isNullOrBlank()) strokeColor else options.color,
            fillColor = if (!fillColor.isNullOrBlank()) fillColor else options.color,
            action = options.action
        )
    }

    private fun parseLine(
        selectChoice: SelectChoice,
        index: Int,
        translator: (SelectChoice) -> String,
        properties: List<IconifiedText>,
        points: List<MapPoint>,
        options: Options
    ): MappableItem.Line {
        val strokeColor = getPropertyValue(selectChoice, STROKE)
        return MappableItem.Line(
            index.toLong(),
            translator(selectChoice),
            properties,
            points = points,
            strokeWidth = getPropertyValue(selectChoice, STROKE_WIDTH),
            strokeColor = if (!strokeColor.isNullOrBlank()) strokeColor else options.color,
            action = options.action
        )
    }

    private fun parsePoint(
        selectChoice: SelectChoice,
        index: Int,
        translator: (SelectChoice) -> String,
        properties: List<IconifiedText>,
        points: List<MapPoint>,
        options: Options
    ): MappableItem.Point {
        val markerColor =
            getPropertyValue(selectChoice, MARKER_COLOR)
        val markerSymbol =
            getPropertyValue(selectChoice, MARKER_SYMBOL)

        return MappableItem.Point(
            index.toLong(),
            translator(selectChoice),
            properties,
            point = points[0],
            smallIcon = if (markerSymbol.isNullOrBlank()) R.drawable.ic_map_marker_with_hole_small else R.drawable.ic_map_marker_small,
            largeIcon = if (markerSymbol.isNullOrBlank()) R.drawable.ic_map_marker_with_hole_big else R.drawable.ic_map_marker_big,
            color = if (!markerColor.isNullOrBlank()) markerColor else options.color,
            symbol = markerSymbol,
            action = options.action
        )
    }

    private fun getPropertyValue(selectChoice: SelectChoice, propertyName: String): String? {
        return selectChoice.additionalChildren.firstOrNull { it.first == propertyName }?.second
    }

    private val FILTERED_PROPERTIES = setOf(
        GEOMETRY,
        MARKER_COLOR,
        MARKER_SYMBOL,
        STROKE,
        STROKE_WIDTH,
        FILL,
        EntitySchema.VERSION,
        EntitySchema.TRUNK_VERSION,
        EntitySchema.BRANCH_ID
    )

    data class Options(val action: IconifiedText? = null, val color: String? = null)
}

object GeoSelectChoiceElements {
    const val GEOMETRY = "geometry"
    const val MARKER_COLOR = "marker-color"
    const val MARKER_SYMBOL = "marker-symbol"
    const val STROKE = "stroke"
    const val STROKE_WIDTH = "stroke-width"
    const val FILL = "fill"
}
