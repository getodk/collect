package org.odk.collect.android.dynamicpreload

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.QuestionDef
import org.javarosa.xpath.expr.XPathExpression
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DynamicPreloadParseProcessorTest {

    private val processor = DynamicPreloadParseProcessor()

    @Test
    fun `usesDynamicPreload is false when xpath does not contain pulldata`() {
        val formDef = FormDef()

        processor.processXPath(createNonPullDataExpression())
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when xpath does contain pulldata`() {
        val formDef = FormDef()

        processor.processXPath(createPullDataExpression())
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }

    @Test
    fun `usesDynamicPreload is false when question appearance does not contain search`() {
        val formDef = FormDef()

        processor.processQuestion(createQuestion(appearance = "minimal"))
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(false)
        )
    }

    @Test
    fun `usesDynamicPreload is true when question appearance does contain search`() {
        val formDef = FormDef()

        processor.processQuestion(createQuestion(appearance = "search('fruits')"))
        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }

    private fun createNonPullDataExpression() = mock<XPathExpression> {
        on { containsFunc("pulldata") } doReturn false
    }

    private fun createPullDataExpression() = mock<XPathExpression> {
        on { containsFunc("pulldata") } doReturn true
    }

    private fun createQuestion(appearance: String): QuestionDef {
        return mock {
            on { appearanceAttr } doReturn appearance
        }
    }
}
