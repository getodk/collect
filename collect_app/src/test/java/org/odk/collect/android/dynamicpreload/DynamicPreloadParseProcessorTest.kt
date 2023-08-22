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
    fun `getBindAttributes returns calculate, readonly, relevant, constraint and required`() {
        assertThat(
            processor.bindAttributes,
            contains(
                Pair("", "calculate"),
                Pair("", "readonly"),
                Pair("", "required"),
                Pair("", "relevant"),
                Pair("", "constraint")
            )
        )
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

    @Test
    fun `usesDynamicPreload is false when readonly does not contain pulldata`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.readonlyCondition = createTriggerable(createNonPullDataExpression())

        processor.processBindAttribute("readonly", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is false when readonly is absolute`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.readonlyAbsolute = false

        processor.processBindAttribute("readonly", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when readonly does contain pulldata`() {
        val formDef = FormDef()

        val bindingWithPullData = DataBinding()
        bindingWithPullData.readonlyCondition = createTriggerable(createPullDataExpression())

        processor.processBindAttribute("readonly", "", bindingWithPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }

    @Test
    fun `usesDynamicPreload is false when required does not contain pulldata`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.requiredCondition = createTriggerable(createNonPullDataExpression())

        processor.processBindAttribute("required", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is false when required is absolute`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.requiredAbsolute = false

        processor.processBindAttribute("required", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when required does contain pulldata`() {
        val formDef = FormDef()

        val bindingWithPullData = DataBinding()
        bindingWithPullData.requiredCondition = createTriggerable(createPullDataExpression())

        processor.processBindAttribute("required", "", bindingWithPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }

    @Test
    fun `usesDynamicPreload is false when relevant does not contain pulldata`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.relevancyCondition = createTriggerable(createNonPullDataExpression())

        processor.processBindAttribute("relevant", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is false when relevant is absolute`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.relevantAbsolute = false

        processor.processBindAttribute("relevant", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when relevant does contain pulldata`() {
        val formDef = FormDef()

        val bindingWithPullData = DataBinding()
        bindingWithPullData.relevancyCondition = createTriggerable(createPullDataExpression())

        processor.processBindAttribute("relevant", "", bindingWithPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }

    @Test
    fun `usesDynamicPreload is false when constraint does not contain pulldata`() {
        val formDef = FormDef()

        val bindingWithoutPullData = DataBinding()
        bindingWithoutPullData.constraint = createCondition(createNonPullDataExpression())

        processor.processBindAttribute("constraint", "", bindingWithoutPullData)
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when constraint does contain pulldata`() {
        val formDef = FormDef()

        val bindingWithPullData = DataBinding()
        bindingWithPullData.constraint = createCondition(createPullDataExpression())

        processor.processBindAttribute("constraint", "", bindingWithPullData)
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
        val condition = createCondition(expression)

        return mock {
            on { expr } doReturn condition
        }
    }

    private fun createCondition(expression: XPathExpression): IConditionExpr {
        val condition = mock<IConditionExpr> {
            on { expr } doReturn expression
        }
        return condition
    }
}
