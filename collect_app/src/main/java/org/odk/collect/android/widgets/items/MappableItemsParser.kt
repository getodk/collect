package org.odk.collect.android.widgets.items

import org.javarosa.core.model.SelectChoice
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.FILL
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.GEOMETRY
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.MARKER_COLOR
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.MARKER_SYMBOL
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.STROKE
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements.STROKE_WIDTH
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry
import org.odk.collect.geo.items.IconifiedText
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.icons.R

object MappableItemsParser {

    fun parseChoices(
        choices: List<SelectChoice>,
        options: Options,
        translator: (SelectChoice) -> String
    ): List<MappableItem> {
        return choices.mapIndexedNotNull { index, selectChoice ->
            val geometry = selectChoice.getChild(GEOMETRY)

            if (geometry != null) {
                try {
                    val points = parseGeometry(geometry)
                    if (points.isNotEmpty()) {
                        val withinBounds = points.all {
                            GeoWidgetUtils.isWithinMapBounds(it)
                        }

                        if (withinBounds) {
                            val properties = selectChoice.additionalChildren.filterNot {
                                FILTERED_PROPERTIES.contains(it.first)
                            }.map {
                                IconifiedText(null, "${it.first}: ${it.second}")
                            }

                            if (points.size == 1) {
                                val markerColor =
                                    getPropertyValue(selectChoice, MARKER_COLOR)
                                val markerSymbol =
                                    getPropertyValue(selectChoice, MARKER_SYMBOL)

                                MappableItem.Point(
                                    index.toLong(),
                                    translator(selectChoice),
                                    properties,
                                    point = points[0],
                                    smallIcon = if (markerSymbol.isNullOrBlank()) R.drawable.ic_map_marker_with_hole_small else R.drawable.ic_map_marker_small,
                                    largeIcon = if (markerSymbol.isNullOrBlank()) R.drawable.ic_map_marker_with_hole_big else R.drawable.ic_map_marker_big,
                                    color = markerColor ?: options.color,
                                    symbol = markerSymbol,
                                    action = options.action
                                )
                            } else if (points.first() != points.last()) {
                                MappableItem.Line(
                                    index.toLong(),
                                    translator(selectChoice),
                                    properties,
                                    points = points,
                                    strokeWidth = getPropertyValue(selectChoice, STROKE_WIDTH),
                                    strokeColor = getPropertyValue(selectChoice, STROKE)
                                        ?: options.color,
                                    action = options.action
                                )
                            } else {
                                MappableItem.Polygon(
                                    index.toLong(),
                                    translator(selectChoice),
                                    properties,
                                    points = points,
                                    strokeWidth = getPropertyValue(selectChoice, STROKE_WIDTH),
                                    strokeColor = getPropertyValue(selectChoice, STROKE)
                                        ?: options.color,
                                    fillColor = getPropertyValue(selectChoice, FILL)
                                        ?: options.color,
                                    action = options.action
                                )
                            }
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (_: NumberFormatException) {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun getPropertyValue(selectChoice: SelectChoice, propertyName: String): String? {
        return selectChoice.additionalChildren.firstOrNull { it.first == propertyName }?.second
    }

    private val FILTERED_PROPERTIES = arrayOf(
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