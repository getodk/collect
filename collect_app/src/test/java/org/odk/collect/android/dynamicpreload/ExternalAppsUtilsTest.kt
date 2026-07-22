package org.odk.collect.android.dynamicpreload

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.repeat
import org.javarosa.test.XFormsElement.t
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.javarosawrapper.FormController

@RunWith(AndroidJUnit4::class)
class ExternalAppsUtilsTest {
    private val scenario = Scenario.init(
        html(
            head(
                model(
                    mainInstance(
                        t(
                            "data id=\"form\"",
                            t("label"),
                            t(
                                "rep",
                                t("name")
                            )
                        )
                    ),
                    bind("/data/label").type("string"),
                    bind("/data/rep/name").type("string")
                )
            ),
            body(
                input("/data/label"),
                repeat(
                    "/data/rep",
                    input("/data/rep/name")
                )
            )
        )
    ).apply {
        answer("/data/label", "foo")
        answer("/data/rep[1]/name", "bar")
    }

    private val formController = mock<FormController> {
        on { getFormDef() } doReturn scenario.formDef
    }

    @Test
    fun `#getValueRepresentedBy resolves a relative reference inside a repeat against the current repeat instance`() {
        val reference = Scenario.getRef("/data/rep[1]/name")

        val result = ExternalAppsUtils.getValueRepresentedBy("../name", reference, formController)

        assertThat(result, equalTo("bar"))
    }

    @Test
    fun `#getValueRepresentedBy resolves a relative reference outside a repeat`() {
        val reference = Scenario.getRef("/data/label")

        val result = ExternalAppsUtils.getValueRepresentedBy("../label", reference, formController)

        assertThat(result, equalTo("foo"))
    }

    @Test
    fun `#getValueRepresentedBy resolves an absolute reference`() {
        val reference = Scenario.getRef("/data/rep[1]/name")

        val result = ExternalAppsUtils.getValueRepresentedBy("/data/label", reference, formController)

        assertThat(result, equalTo("foo"))
    }

    @Test
    fun `#getValueRepresentedBy evaluates a function`() {
        val reference = Scenario.getRef("/data/rep[1]/name")

        val result = ExternalAppsUtils.getValueRepresentedBy("true()", reference, formController)

        assertThat(result, equalTo(true))
    }

    @Test
    fun `#getValueRepresentedBy returns a constant as is`() {
        val reference = Scenario.getRef("/data/rep[1]/name")

        val result = ExternalAppsUtils.getValueRepresentedBy("'hello'", reference, formController)

        assertThat(result, equalTo("hello"))
    }
}
