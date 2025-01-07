package org.odk.collect.entities.javarosa.filter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.StringData
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
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository

class PullDataFunctionHandlerTest {

    @Test
    fun `returns empty string when there are no matching results`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.addList("things")

        val scenario = Scenario.init(
            "Pull data form",
            html(
                head(
                    title("Pull data form"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"pull-data-form\"",
                                t("question"),
                                t("calculate")
                            )
                        ),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("string")
                            .calculate("pulldata('things', 'label', 'name', 'blah')")
                    )
                ),
                body(
                    input("/data/question"),
                    input("/data/calculate")
                )
            )
        ) { formDef ->
            FormEntryController(FormEntryModel(formDef)).also {
                it.addFunctionHandler(PullDataFunctionHandler(entitiesRepository))
            }
        }

        assertThat(scenario.answerOf<StringData>("/data/calculate"), equalTo(null))
    }

    @Test
    fun `returns first match when there are multiple`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save(
            "things",
            Entity.New("one", "One", properties = listOf(Pair("property", "value")))
        )
        entitiesRepository.save(
            "things",
            Entity.New("two", "Two", properties = listOf(Pair("property", "value")))
        )

        val scenario = Scenario.init(
            "Pull data form",
            html(
                head(
                    title("Pull data form"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"pull-data-form\"",
                                t("question"),
                                t("calculate")
                            )
                        ),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("string")
                            .calculate("pulldata('things', 'label', 'property', 'value')")
                    )
                ),
                body(
                    input("/data/question"),
                    input("/data/calculate")
                )
            )
        ) { formDef ->
            FormEntryController(FormEntryModel(formDef)).also {
                it.addFunctionHandler(PullDataFunctionHandler(entitiesRepository))
            }
        }

        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("One"))
    }
}
