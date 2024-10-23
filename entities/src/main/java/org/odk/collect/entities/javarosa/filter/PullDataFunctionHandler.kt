package org.odk.collect.entities.javarosa.filter

import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.IFunctionHandler
import org.javarosa.xpath.expr.XPathFuncExpr
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.storage.EntitiesRepository

class PullDataFunctionHandler(
    entitiesRepository: EntitiesRepository,
    private val fallback: IFunctionHandler? = null
) : IFunctionHandler {

    private val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)

    override fun getName(): String {
        return NAME
    }

    override fun getPrototypes(): List<Array<Class<Any>>> {
        return emptyList()
    }

    override fun rawArgs(): Boolean {
        return true
    }

    override fun realTime(): Boolean {
        return false
    }

    override fun eval(args: Array<Any>, ec: EvaluationContext): Any {
        val instanceId = XPathFuncExpr.toString(args[0])

        return if (instanceAdapter.supportsInstance(instanceId)) {
            val child = XPathFuncExpr.toString(args[1])
            val filterChild = XPathFuncExpr.toString(args[2])
            val filterValue = XPathFuncExpr.toString(args[3])

            instanceAdapter.queryEq(instanceId, filterChild, filterValue).firstOrNull()
                ?.getFirstChild(child)?.value?.value ?: ""
        } else {
            fallback?.eval(args, ec) ?: ""
        }
    }

    companion object {
        private const val NAME = "pulldata"
    }
}
