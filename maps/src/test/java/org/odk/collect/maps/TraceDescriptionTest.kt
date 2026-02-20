package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

@RunWith(AndroidJUnit4::class)
class TraceDescriptionTest {

    @Test
    fun `#getMarkersForPoints returns MarkerDescriptions for points on trace`() {
        val traceDescription = TestTraceDescription(
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            highlightLastPoint = false,
            strokeWidth = 5f,
            strokeColor = 123
        )

        val markers = traceDescription.getMarkersForPoints()
        assertThat(
            markers,
            contains(
                MarkerDescription(
                    traceDescription.points[0],
                    true,
                    MapFragment.IconAnchor.CENTER,
                    MarkerIconDescription.TracePoint(
                        traceDescription.getStrokeWidth(),
                        traceDescription.getStrokeColor()
                    )
                ),
                MarkerDescription(
                    traceDescription.points[1],
                    true,
                    MapFragment.IconAnchor.CENTER,
                    MarkerIconDescription.TracePoint(
                        traceDescription.getStrokeWidth(),
                        traceDescription.getStrokeColor()
                    )
                )
            )
        )
    }

    @Test
    fun `#getMarkersForPoints returns last marker with highlight color when highlightLastPoint is true`() {
        val traceDescription = TestTraceDescription(
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            highlightLastPoint = true,
            strokeWidth = 5f,
            strokeColor = 123
        )

        val icons = traceDescription.getMarkersForPoints().map {
            it.iconDescription as MarkerIconDescription.TracePoint
        }

        assertThat(icons[0].color, equalTo(traceDescription.getStrokeColor()))
        assertThat(icons[1].color, equalTo(MapConsts.DEFAULT_HIGHLIGHT_COLOR))
    }
}

private class TestTraceDescription(
    override val points: List<MapPoint>,
    override val highlightLastPoint: Boolean,
    private val strokeWidth: Float,
    private val strokeColor: Int
) : TraceDescription {
    override fun getStrokeWidth(): Float {
        return strokeWidth
    }

    override fun getStrokeColor(): Int {
        return strokeColor
    }
}
