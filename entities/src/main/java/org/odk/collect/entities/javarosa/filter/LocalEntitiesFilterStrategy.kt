package org.odk.collect.entities.javarosa.filter

import android.database.sqlite.SQLiteException
import org.javarosa.core.model.CompareToNodeExpression
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.xpath.expr.XPathBoolExpr
import org.javarosa.xpath.expr.XPathEqExpr
import org.javarosa.xpath.expr.XPathExpression
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceProvider
import org.odk.collect.entities.storage.EntitiesRepository
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

        val query = xPathExpressionToQuery(predicate, sourceInstance, evaluationContext)

        return if (query != null) {
            try {
                queryToTreeReferences(query, sourceInstance)
            } catch (e: SQLiteException) {
                next.get()
            }
        } else {
            next.get()
        }
    }

    private fun xPathExpressionToQuery(
        predicate: XPathExpression,
        sourceInstance: DataInstance<*>,
        evaluationContext: EvaluationContext,
    ): Query? {
        return when (predicate) {
            is XPathBoolExpr -> xPathBoolExprToQuery(predicate, sourceInstance, evaluationContext)
            is XPathEqExpr -> xPathEqExprToQuery(predicate, sourceInstance, evaluationContext)
            else -> null
        }
    }

    private fun xPathBoolExprToQuery(
        predicate: XPathBoolExpr,
        sourceInstance: DataInstance<*>,
        evaluationContext: EvaluationContext,
    ): Query? {
        val queryA = xPathExpressionToQuery(predicate.a, sourceInstance, evaluationContext)
        val queryB = xPathExpressionToQuery(predicate.b, sourceInstance, evaluationContext)

        return if (queryA != null && queryB != null) {
            if (predicate.op == XPathBoolExpr.AND) {
                Query.And(queryA, queryB)
            } else {
                Query.Or(queryA, queryB)
            }
        } else {
            null
        }
    }

    private fun xPathEqExprToQuery(
        predicate: XPathEqExpr,
        sourceInstance: DataInstance<*>,
        evaluationContext: EvaluationContext,
    ): Query? {
        val candidate = CompareToNodeExpression.parse(predicate)

        return if (candidate != null) {
            val child = candidate.nodeSide.steps[0].name.name
            val value = candidate.evalContextSide(sourceInstance, evaluationContext) as String

            if (predicate.isEqual) {
                Query.Eq(child, value)
            } else {
                Query.NotEq(child, value)
            }
        } else {
            null
        }
    }

    private fun queryToTreeReferences(query: Query, sourceInstance: DataInstance<*>): List<TreeReference> {
        val results = instanceAdapter.query(sourceInstance.instanceId, query)
        sourceInstance.replacePartialElements(results)
        return results.map {
            it.parent = sourceInstance.root
            it.ref
        }
    }
}
