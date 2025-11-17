package org.odk.collect.geo.javarosa

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.BooleanData
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.t
import org.javarosa.test.XFormsElement.title
import org.junit.Test

class IntersectsFunctionHandlerTest {

    @Test
    fun `returns false when input is empty`() {
        val scenario = Scenario.init(
            "Intersects form",
            html(
                head(
                    title("Intersects form"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"intersects-form\"",
                                t("question"),
                                t("calculate")
                            )
                        ),
                        bind("/data/question").type("geotrace"),
                        bind("/data/calculate").type("boolean")
                            .calculate("intersects(/data/question)")
                    )
                ),
                body(
                    input("/data/question"),
                    input("/data/calculate")
                )
            )
        ) { formDef ->
            FormEntryController(FormEntryModel(formDef)).also {
                it.addFunctionHandler(IntersectsFunctionHandler())
            }
        }

        assertThat(scenario.answerOf<BooleanData>("/data/calculate").value, equalTo(false))
    }

    @Test
    fun `returns false when input is a string`() {
        val scenario = Scenario.init(
            "Intersects form",
            html(
                head(
                    title("Intersects form"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"intersects-form\"",
                                t("question"),
                                t("calculate")
                            )
                        ),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("boolean")
                            .calculate("intersects(/data/question)")
                    )
                ),
                body(
                    input("/data/question"),
                    input("/data/calculate")
                )
            )
        ) { formDef ->
            FormEntryController(FormEntryModel(formDef)).also {
                it.addFunctionHandler(IntersectsFunctionHandler())
            }
        }

        scenario.answer("/data/question", "blah")
        assertThat(scenario.answerOf<BooleanData>("/data/calculate").value, equalTo(false))
    }

    @Test
    fun `returns true when input is intersecting trace`() {
        val scenario = Scenario.init(
            "Intersects form",
            html(
                head(
                    title("Intersects form"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"intersects-form\"",
                                t("question"),
                                t("calculate")
                            )
                        ),
                        bind("/data/question").type("geotrace"),
                        bind("/data/calculate").type("boolean")
                            .calculate("intersects(/data/question)")
                    )
                ),
                body(
                    input("/data/question"),
                    input("/data/calculate")
                )
            )
        ) { formDef ->
            FormEntryController(FormEntryModel(formDef)).also {
                it.addFunctionHandler(IntersectsFunctionHandler())
            }
        }

        scenario.answer("/data/question", "1.0 1.0 0.0 0.0; 1.0 3.0 0.0 0.0; 2.0 3.0 0.0 0.0; 2.0 2.0 0.0 0.0; 0.0 2.0 0.0 0.0")
        assertThat(scenario.answerOf<BooleanData>("/data/calculate").value, equalTo(true))
    }
}
