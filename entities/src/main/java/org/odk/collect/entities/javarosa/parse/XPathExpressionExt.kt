package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.model.CompareToNodeExpression
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.xpath.expr.XPathBoolExpr
import org.javarosa.xpath.expr.XPathEqExpr
import org.javarosa.xpath.expr.XPathExpression
import org.odk.collect.shared.Query

object XPathExpressionExt {

    /**
     * Converts an XPath expression to a [Query]. For example:
     * - `label = 'blah'` will be converted to `Query.StringEq("label", "foo")`
     * - `label` = /some/string/ref will be converted to `Query.StringEq("label", "blah")`
     * (where `/some/string/ref` evaluates to `"blah"` within the context of the passed
     * `DataInstance` and `EvaluationContext`)
     *
     * `and`, `or`, `=` and `!=` are all supported. If an expression cannot be converted to a
     * `Query`, `null` will be returned.
     */
    fun XPathExpression.parseToQuery(
        sourceInstance: DataInstance<*>,
        evaluationContext: EvaluationContext
    ): Query? {
        return when (this) {
            is XPathBoolExpr -> xPathBoolExprToQuery(this, sourceInstance, evaluationContext)
            is XPathEqExpr -> xPathEqExprToQuery(this, sourceInstance, evaluationContext)
            else -> null
        }
    }

    private fun xPathBoolExprToQuery(
        predicate: XPathBoolExpr,
        sourceInstance: DataInstance<*>,
        evaluationContext: EvaluationContext
    ): Query? {
        val queryA = predicate.a.parseToQuery(sourceInstance, evaluationContext)
        val queryB = predicate.b.parseToQuery(sourceInstance, evaluationContext)

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
        evaluationContext: EvaluationContext
    ): Query? {
        val candidate = CompareToNodeExpression.parse(predicate)

        return if (candidate != null) {
            val child = if (candidate.nodeSide.steps.size == 1) {
                candidate.nodeSide.steps[0].name.name
            } else {
                candidate.nodeSide.steps[1].name.name
            }

            val value = candidate.evalContextSide(sourceInstance, evaluationContext)

            if (predicate.isEqual) {
                if (value is Double) {
                    Query.NumericEq(child, value)
                } else {
                    Query.StringEq(child, value.toString())
                }
            } else {
                if (value is Double) {
                    Query.NumericNotEq(child, value)
                } else {
                    Query.StringNotEq(child, value.toString())
                }
            }
        } else {
            null
        }
    }
}
