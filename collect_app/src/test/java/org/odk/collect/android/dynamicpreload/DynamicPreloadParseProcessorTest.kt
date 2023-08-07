package org.odk.collect.android.dynamicpreload

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.DataBinding
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.condition.IConditionExpr
import org.javarosa.core.model.condition.Triggerable
import org.javarosa.xpath.expr.XPathExpression
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DynamicPreloadParseProcessorTest {

    private val processor = DynamicPreloadParseProcessor()

    @Test
    fun `getBindAttributes returns calculate`() {
        assertThat(processor.bindAttributes, contains(Pair("", "calculate")))
    }

    @Test
    fun `usesDynamicPreload is false when calculate does not contain pulldata`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.calculate = createTriggerable(createNonPullDataExpression())

        processor.processBindAttribute("calculate", "", bindingWithoutPullData)

        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when calculate does contain pulldata`() {
        val formDef = FormDef()

        val bindingWithPullData = DataBinding()
        bindingWithPullData.calculate = createTriggerable(createPullDataExpression())

        processor.processBindAttribute("calculate", "", bindingWithPullData)

        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }

    private fun createPullDataExpression() = mock<XPathExpression> {
        on { containsFunc("pulldata") } doReturn true
    }

    private fun createNonPullDataExpression() = mock<XPathExpression> {
        on { containsFunc("pulldata") } doReturn false
    }

    private fun createTriggerable(expression: XPathExpression): Triggerable {
        val condition = mock<IConditionExpr> {
            on { expr } doReturn expression
        }

        return mock {
            on { expr } doReturn condition
        }
    }
}
