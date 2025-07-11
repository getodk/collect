package org.odk.collect.entities.javarosa.parse

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.instance.ExternalDataInstance
import org.javarosa.xpath.XPathParseTool
import org.junit.Test
import org.odk.collect.entities.javarosa.parse.XPathExpressionExt.toQuery
import org.odk.collect.shared.Query

class XPathExpressionExtTest {

    @Test
    fun `#toQuery returns Query when node side is qualified relative expression`() {
        val sourceInstance = ExternalDataInstance()
        val evaluationContext = EvaluationContext(sourceInstance)

        val expression = XPathParseTool.parseXPath("./label = 'blah'")
        assertThat(
            expression.toQuery(sourceInstance, evaluationContext),
            equalTo(Query.StringEq("label", "blah"))
        )
    }

    @Test
    fun `#toQuery returns Query when node side is node() expression`() {
        val sourceInstance = ExternalDataInstance()
        val evaluationContext = EvaluationContext(sourceInstance)

        val expression = XPathParseTool.parseXPath("node()/label = 'blah'")
        assertThat(
            expression.toQuery(sourceInstance, evaluationContext),
            equalTo(Query.StringEq("label", "blah"))
        )
    }

    @Test
    fun `#toQuery returns null when node side is multiple levels`() {
        val sourceInstance = ExternalDataInstance()
        val evaluationContext = EvaluationContext(sourceInstance)

        val expression = XPathParseTool.parseXPath("one/two = 'blah'")
        assertThat(
            expression.toQuery(sourceInstance, evaluationContext),
            equalTo(null)
        )
    }

    @Test
    fun `#toQuery returns null when node side is multiple levels and contains level called node`() {
        val sourceInstance = ExternalDataInstance()
        val evaluationContext = EvaluationContext(sourceInstance)

        val expression = XPathParseTool.parseXPath("node/two = 'blah'")
        assertThat(
            expression.toQuery(sourceInstance, evaluationContext),
            equalTo(null)
        )
    }
}
