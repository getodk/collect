package org.odk.collect.entities.javarosa.filter

import org.javarosa.core.model.CompareToNodeExpression
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.xpath.expr.XPathEqExpr
import org.javarosa.xpath.expr.XPathExpression
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceProvider
import org.odk.collect.entities.storage.EntitiesRepository
import java.util.function.Supplier

/**
 * A JavaRosa [FilterStrategy] that will use an [EntitiesRepository] to perform filters. For
 * supported expressions, this prevents JavaRosa from using it's standard [FilterStrategy] chain
 * which requires loading the whole secondary instance into memory (assuming that
 * [LocalEntitiesInstanceProvider] or similar is used to take advantage of JavaRosa's partial
 * parsing).
 */
class LocalEntitiesFilterStrategy(entitiesRepository: EntitiesRepository) :
    FilterStrategy {

    private val dataAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)

    override fun filter(
        sourceInstance: DataInstance<*>,
        nodeSet: TreeReference,
        predicate: XPathExpression,
        children: MutableList<TreeReference>,
        evaluationContext: EvaluationContext,
        next: Supplier<MutableList<TreeReference>>
    ): List<TreeReference> {
        if (sourceInstance.instanceId == null || !dataAdapter.supportsInstance(sourceInstance.instanceId)) {
            return next.get()
        }

        val candidate = CompareToNodeExpression.parse(predicate)
        return when (val original = candidate?.original) {
            is XPathEqExpr -> {
                if (original.isEqual) {
                    val child = candidate.nodeSide.steps[0].name.name
                    val value = candidate.evalContextSide(sourceInstance, evaluationContext)

                    val results = dataAdapter.queryEq(
                        sourceInstance.instanceId,
                        child,
                        value as String
                    )
                    return if (results != null) {
                        sourceInstance.replacePartialElements(results)
                        results.map {
                            it.parent = sourceInstance.root
                            it.ref
                        }
                    } else {
                        next.get()
                    }
                } else {
                    next.get()
                }
            }

            else -> {
                next.get()
            }
        }
    }
}
