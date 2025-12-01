package org.odk.collect.shared.geometry.support

import org.odk.collect.shared.geometry.LineSegment
import org.odk.collect.shared.geometry.Point
import org.odk.collect.shared.geometry.Trace
import org.odk.collect.shared.geometry.segments
import kotlin.random.Random

object GeometryTestUtils {

    fun getTraceGenerator(maxLength: Int = 10, maxCoordinate: Double = 100.0): Sequence<Trace> {
        return generateSequence {
            val length = Random.nextInt(3, maxLength)
            val trace = Trace(0.until(length).map {
                Point(
                    Random.nextDouble(maxCoordinate * -1, maxCoordinate),
                    Random.nextDouble(maxCoordinate * -1, maxCoordinate)
                )
            })

            if (trace.isClosed()) {
                trace
            } else {
                val shouldClose = Random.nextBoolean()
                if (shouldClose) {
                    trace.copy(points = trace.points + trace.points.first())
                } else {
                    trace
                }
            }
        }
    }

    fun Trace.reverse(): Trace {
        return Trace(points.reversed())
    }

    fun Trace.scale(factor: Double): Trace {
        return Trace(points.map {
            Point(it.x * factor, it.y * factor)
        })
    }

    fun Trace.addRandomIntersectingSegment(): Trace {
        val intersectionSegment = segments().dropLast(1).random()
        val intersectPosition = Random.nextDouble(0.1, 1.0)
        val intersectionPoint = intersectionSegment.interpolate(intersectPosition)
        val lineSegment = LineSegment(points.last(), intersectionPoint)
        val intersectingSegment =
            LineSegment(lineSegment.start, lineSegment.interpolate(1.1))
        return Trace(points + intersectingSegment.end)
    }

    /**
     * Calculate a [Point] on this [LineSegment] based on the `position` using
     * [Linear interpolation](https://en.wikipedia.org/wiki/Linear_interpolation). `0` will return
     * [LineSegment.start] and `1` will return [LineSegment.end].
     */
    fun LineSegment.interpolate(position: Double): Point {
        val x = start.x + position * (end.x - start.x)
        val y = start.y + position * (end.y - start.y)
        return Point(x, y)
    }
}
