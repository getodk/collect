package org.odk.collect.entities.javarosa.filter

import org.javarosa.core.model.CompareToNodeExpression
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.xpath.expr.XPathEqExpr
import org.javarosa.xpath.expr.XPathExpression
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import java.util.function.Supplier

class LocalEntitiesFilterStrategy(private val entitiesRepository: EntitiesRepository) :
    FilterStrategy {
    override fun filter(
        sourceInstance: DataInstance<*>,
        nodeSet: TreeReference,
        predicate: XPathExpression,
        children: MutableList<TreeReference>,
        evaluationContext: EvaluationContext,
        next: Supplier<MutableList<TreeReference>>
    ): List<TreeReference> {
        if (!entitiesRepository.getLists().contains(sourceInstance.instanceId)) {
            return next.get()
        }

        val candidate = CompareToNodeExpression.parse(predicate)

        return when (val original = candidate?.original) {
            is XPathEqExpr -> {
                if (original.isEqual) {
                    when {
                        (candidate.nodeSide.steps[0].name.name == "name") -> {
                            val value = candidate.evalContextSide(sourceInstance, evaluationContext)
                            val entity =
                                entitiesRepository.getById(
                                    sourceInstance.instanceId,
                                    value as String
                                )

                            if (entity != null) {
                                listOf(convertToReference(nodeSet, entity))
                            } else {
                                emptyList()
                            }
                        }

                        else -> next.get()
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

    private fun convertToReference(
        nodeSet: TreeReference,
        entity: Entity.Saved
    ): TreeReference {
        return nodeSet.clone().also {
            it.setMultiplicity(it.size() - 1, entity.index)
        }
    }
}
