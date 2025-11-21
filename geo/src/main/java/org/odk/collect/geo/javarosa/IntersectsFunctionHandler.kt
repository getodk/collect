package org.odk.collect.geo.javarosa

import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.IFunctionHandler
import org.javarosa.xpath.XPathTypeMismatchException
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry
import org.odk.collect.maps.toPoint
import org.odk.collect.shared.geometry.Trace
import org.odk.collect.shared.geometry.intersects

class IntersectsFunctionHandler : IFunctionHandler {
    override fun getName(): String {
        return "intersects"
    }

    override fun getPrototypes(): List<Array<out Class<*>>> {
        return listOf(arrayOf(String::class.java))
    }

    override fun rawArgs(): Boolean {
        return false
    }

    override fun realTime(): Boolean {
        TODO("Not yet implemented")
    }

    override fun eval(
        args: Array<out Any?>,
        ec: EvaluationContext
    ): Any {
        try {
            val mapPoints = parseGeometry(args[0] as String, strict = true)
            val trace = Trace(mapPoints.map { it.toPoint() })
            return trace.intersects()
        } catch (_: IllegalArgumentException) {
            throw XPathTypeMismatchException()
        }
    }
}
