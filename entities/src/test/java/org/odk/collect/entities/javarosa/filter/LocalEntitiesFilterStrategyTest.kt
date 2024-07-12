package org.odk.collect.entities.javarosa.filter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.StringData
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.instance
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.t
import org.javarosa.test.XFormsElement.title
import org.javarosa.xform.util.XFormUtils
import org.junit.Test
import org.odk.collect.entities.storage.InMemEntitiesRepository
import java.io.InputStreamReader

class LocalEntitiesFilterStrategyTest {

    @Test
    fun `does not effect name queries on non entity instances`() {
        val xForm = html(
            head(
                title("Secondary instance form"),
                model(
                    mainInstance(
                        t(
                            "data id=\"create-entity-form\"",
                            t("question"),
                            t("calculate")
                        )
                    ),
                    instance(
                        "secondary",
                        t("item", t("label", "Thing"), t("name", "thing"))
                    ),
                    bind("/data/question").type("string"),
                    bind("/data/calculate").type("string")
                        .calculate("instance('secondary')/root/item[name='thing']/label")
                )
            ),
            body(
                input("/data/calculate")
            )
        )

        val xml = xForm.asXml()
        val formDef = XFormUtils.getFormRaw(InputStreamReader(xml.byteInputStream()))
        formDef.addFilterStrategy(LocalEntitiesFilterStrategy(InMemEntitiesRepository()))

        val scenario = Scenario.init(formDef)
        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("Thing"))
    }
}
