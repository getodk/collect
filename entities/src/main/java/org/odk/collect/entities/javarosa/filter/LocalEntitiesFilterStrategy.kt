package org.odk.collect.entities.javarosa.filter

import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.xpath.expr.XPathExpression
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceProvider
import org.odk.collect.entities.javarosa.parse.XPathExpressionExt.toQuery
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.QueryException
import org.odk.collect.shared.Query
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

    private val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)

    override fun filter(
        sourceInstance: DataInstance<*>,
        nodeSet: TreeReference,
        predicate: XPathExpression,
        children: MutableList<TreeReference>,
        evaluationContext: EvaluationContext,
        next: Supplier<MutableList<TreeReference>>
    ): List<TreeReference> {
        if (sourceInstance.instanceId == null || !instanceAdapter.supportsInstance(sourceInstance.instanceId)) {
            return next.get()
        }

        val query = predicate.toQuery(sourceInstance, evaluationContext)
        return if (query != null) {
            try {
                queryToTreeReferences(query, sourceInstance)
            } catch (e: QueryException) {
                next.get()
            }
        } else {
            next.get()
        }
    }

    private fun queryToTreeReferences(
        query: Query,
        sourceInstance: DataInstance<*>
    ): List<TreeReference> {
        val results = instanceAdapter.query(sourceInstance.instanceId, query)
        sourceInstance.replacePartialElements(results)
        return results.map {
            it.parent = sourceInstance.root
            it.ref
        }
    }
}
