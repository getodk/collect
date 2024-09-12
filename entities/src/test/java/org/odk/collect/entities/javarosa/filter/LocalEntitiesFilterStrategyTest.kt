package org.odk.collect.entities.javarosa.filter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.data.IntegerData
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.instance
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.select1Dynamic
import org.javarosa.test.XFormsElement.t
import org.javarosa.test.XFormsElement.title
import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.parse.ExternalInstanceParser.InstanceProvider
import org.javarosa.xform.util.XFormUtils
import org.javarosa.xpath.expr.XPathExpression
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceProvider
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository
import java.util.function.Supplier

class LocalEntitiesFilterStrategyTest {

    private val entitiesRepository = InMemEntitiesRepository()
    private val fallthroughFilterStrategy = FallthroughFilterStrategy()
    private val instanceProvider =
        SpyInstanceProvider(LocalEntitiesInstanceProvider(::entitiesRepository))

    private val controllerSupplier: (FormDef) -> FormEntryController = { formDef ->
        FormEntryController(FormEntryModel(formDef)).also {
            it.addFilterStrategy(LocalEntitiesFilterStrategy(entitiesRepository))
            it.addFilterStrategy(fallthroughFilterStrategy)
        }
    }

    @Before
    fun setup() {
        XFormUtils.setExternalInstanceParserFactory {
            ExternalInstanceParser().also {
                it.addInstanceProvider(instanceProvider)
            }
        }
    }

    @After
    fun teardown() {
        XFormUtils.setExternalInstanceParserFactory { ExternalInstanceParser() }
    }

    @Test
    fun `returns matching nodes when entity matches name`() {
        entitiesRepository.save("things", Entity.New("thing1", "Thing 1"))
        entitiesRepository.save("things", Entity.New("thing2", "Thing 2"))

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
                            .calculate("instance('things')/root/item[name='thing1']/label")
                    )
                ),
                body(
                    input("/data/calculate")
                )
            ),
            controllerSupplier
        )

        assertThat(fallthroughFilterStrategy.fellThrough, equalTo(false))
        assertThat(scenario.answerOf<StringData>("/data/calculate").value, equalTo("Thing 1"))
    }

    @Test
    fun `replaces partial elements when entity matches name`() {
        entitiesRepository.save(
            "things",
            Entity.New("thing", "Thing"),
            Entity.New("other", "Other")
        )

        Scenario.init(
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

        assertThat(instanceProvider.fullParsePerformed, equalTo(false))
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
        entitiesRepository.save("things", Entity.New("thing", "Thing"))

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
        entitiesRepository.save("things", Entity.New("thing", "Thing"))

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

    @Test
    fun `works correctly with filtering on a repeat`() {
        val scenario = Scenario.init(
            "Count people underage",
            html(
                head(
                    title("Count people underage"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"count_people_underage\"",
                                t("people",
                                    t("name"),
                                    t("age")
                                ),
                                t("total_underage")
                            )
                        ),
                        bind("/data/people/name").type("string"),
                        bind("/data/people/age").type("int"),
                        bind("/data/question").type("string"),
                        bind("/data/total_underage").type("string").calculate("count( /data/people [age&lt;18])")
                    )
                ),
                body(
                    XFormsElement.repeat("/data/people",
                        input("/data/people/name"),
                        input("/data/people/age")
                    ),
                    input("/data/total_underage")
                )
            ),
            controllerSupplier
        )

        assertThat(scenario.answerOf<IntegerData>("/data/total_underage").value, equalTo(0))
    }

    @Test
    fun `returns matching nodes when entity matches property`() {
        entitiesRepository.save(
            "things",
            Entity.New(
                "thing1",
                "Thing1",
                properties = listOf("property" to "value")
            ),
            Entity.New(
                "thing2",
                "Thing2",
                properties = listOf("property" to "value")
            ),
            Entity.New(
                "other",
                "Other",
                properties = listOf("property" to "other")
            )
        )

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
                            )
                        ),
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic(
                        "/data/question",
                        "instance('things')/root/item[property='value']",
                        "name",
                        "label"
                    )
                )
            ),
            controllerSupplier
        )

        assertThat(fallthroughFilterStrategy.fellThrough, equalTo(false))
        val choices = scenario.choicesOf("/data/question").map { it.value }
        assertThat(choices, containsInAnyOrder("thing1", "thing2"))
    }

    @Test
    fun `replaces partial elements when entity matches property`() {
        entitiesRepository.save(
            "things",
            Entity.New(
                "thing1",
                "Thing1",
                properties = listOf("property" to "value")
            )
        )

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
                            )
                        ),
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic(
                        "/data/question",
                        "instance('things')/root/item[property='value']",
                        "name",
                        "label"
                    )
                )
            ),
            controllerSupplier
        )

        scenario.choicesOf("/data/question") // Calculate choices
        assertThat(instanceProvider.fullParsePerformed, equalTo(false))
    }

    @Test
    fun `works correctly with label = expressions`() {
        entitiesRepository.save(
            "things",
            Entity.New(
                "thing1",
                "Thing1",
                properties = listOf("property" to "value")
            )
        )

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
                            )
                        ),
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic(
                        "/data/question",
                        "instance('things')/root/item[label='Thing1']",
                        "name",
                        "label"
                    )
                )
            ),
            controllerSupplier
        )

        val choices = scenario.choicesOf("/data/question").map { it.value }
        assertThat(choices, containsInAnyOrder("thing1"))
    }

    @Test
    fun `works correctly with version = expressions`() {
        entitiesRepository.save(
            "things",
            Entity.New(
                "thing1",
                "Thing1",
                version = 2,
                properties = listOf("property" to "value")
            )
        )

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
                            )
                        ),
                        t("instance id=\"things\" src=\"jr://file-csv/things.csv\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic(
                        "/data/question",
                        "instance('things')/root/item[__version='2']",
                        "name",
                        "label"
                    )
                )
            ),
            controllerSupplier
        )

        val choices = scenario.choicesOf("/data/question").map { it.value }
        assertThat(choices, containsInAnyOrder("thing1"))
    }
}

private class FallthroughFilterStrategy : FilterStrategy {

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

private class SpyInstanceProvider(private val wrapped: InstanceProvider) : InstanceProvider {
    var fullParsePerformed = false
        private set

    override fun get(instanceId: String, instanceSrc: String): TreeElement {
        return wrapped.get(instanceId, instanceSrc)
    }

    override fun get(instanceId: String, instanceSrc: String, partial: Boolean): TreeElement {
        if (!partial) {
            fullParsePerformed = true
        }

        return wrapped.get(instanceId, instanceSrc, partial)
    }

    override fun isSupported(instanceId: String, instanceSrc: String): Boolean {
        return wrapped.isSupported(instanceId, instanceSrc)
    }
}
