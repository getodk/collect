package org.odk.collect.geo.javarosa

import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.IFunctionHandler
import org.odk.collect.geo.geopoly.GeoPolyUtils.intersects
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry

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
        val trace = parseGeometry(args[0] as String)
        return intersects(trace)
    }
}
