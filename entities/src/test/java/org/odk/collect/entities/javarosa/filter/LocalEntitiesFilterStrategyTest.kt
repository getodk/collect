package org.odk.collect.entities.javarosa.filter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
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
import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.util.XFormUtils
import org.javarosa.xpath.expr.XPathExpression
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesExternalInstanceParserFactory
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository
import java.util.function.Supplier

class LocalEntitiesFilterStrategyTest {

    private val entitiesRepository = InMemEntitiesRepository()
    private val fallthroughFilterStrategy = FallthroughFilterStrategy()

    private val controllerSupplier: (FormDef) -> FormEntryController = { formDef ->
        FormEntryController(FormEntryModel(formDef)).also {
            it.addFilterStrategy(LocalEntitiesFilterStrategy(entitiesRepository))
            it.addFilterStrategy(fallthroughFilterStrategy)
        }
    }

    @Before
    fun setup() {
        XFormUtils.setExternalInstanceParserFactory(
            LocalEntitiesExternalInstanceParserFactory(::entitiesRepository)
        )
    }

    @After
    fun teardown() {
        XFormUtils.setExternalInstanceParserFactory { ExternalInstanceParser() }
    }

    @Test
    fun `returns matching nodes when entity matches name`() {
        entitiesRepository.save(Entity.New("things", "thing", "Thing"))

        val scenario = Scenario.init(
            "Secondary instance form",
            html(
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
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("string")
                            .calculate("instance('things')/root/item[name='thing']/label")
                    )
                ),
                body(
                    input("/data/calculate")
                )
            ),
            controllerSupplier
        )

        assertThat(fallthroughFilterStrategy.fellThrough, equalTo(false))
        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("Thing"))
    }

    @Test
    fun `returns empty nodeset when no entity matches name`() {
        entitiesRepository.addList("things")

        val scenario = Scenario.init(
            "Secondary instance form",
            html(
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
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("string")
                            .calculate("instance('things')/root/item[name='other']/label")
                    )
                ),
                body(
                    input("/data/calculate")
                )
            ),
            controllerSupplier
        )

        assertThat(fallthroughFilterStrategy.fellThrough, equalTo(false))
        assertThat(scenario.answerOf<StringData>("/data/calculate"), equalTo(null))
    }

    @Test
    fun `works correctly with name != expressions`() {
        entitiesRepository.save(Entity.New("things", "thing", "Thing"))

        val scenario = Scenario.init(
            "Secondary instance form",
            html(
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
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("string")
                            .calculate("instance('things')/root/item[name!='other']/label")
                    )
                ),
                body(
                    input("/data/calculate")
                )
            ),
            controllerSupplier
        )

        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("Thing"))
    }

    @Test
    fun `works correctly with non eq name expressions`() {
        entitiesRepository.save(Entity.New("things", "thing", "Thing"))

        val scenario = Scenario.init(
            "Secondary instance form",
            html(
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
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string"),
                        bind("/data/calculate").type("string")
                            .calculate("instance('things')/root/item[starts-with(name, 'thing')]/label")
                    )
                ),
                body(
                    input("/data/calculate")
                )
            ),
            controllerSupplier
        )

        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("Thing"))
    }

    @Test
    fun `does not effect name queries on non entity instances`() {
        val scenario = Scenario.init(
            "Secondary instance form",
            html(
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
            ),
            controllerSupplier
        )

        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("Thing"))
    }
}

class FallthroughFilterStrategy : FilterStrategy {

    var fellThrough = false
        private set

    override fun filter(
        sourceInstance: DataInstance<*>,
        nodeSet: TreeReference,
        predicate: XPathExpression,
        children: MutableList<TreeReference>,
        evaluationContext: EvaluationContext,
        next: Supplier<MutableList<TreeReference>>
    ): MutableList<TreeReference> {
        fellThrough = true
        return next.get()
    }
}
