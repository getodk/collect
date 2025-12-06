package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement
import org.javarosa.xform.parse.XFormParserFactory
import org.javarosa.xform.util.XFormUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor
import org.odk.collect.entities.javarosa.parse.EntityXFormParserFactory
import org.odk.collect.entities.javarosa.spec.EntityAction

class EntitiesTest {
    private val entityXFormParserFactory = EntityXFormParserFactory(XFormParserFactory()) { false }

    @Before
    fun setup() {
        XFormUtils.setXFormParserFactory(entityXFormParserFactory)
    }

    @After
    fun teardown() {
        XFormUtils.setXFormParserFactory(XFormParserFactory())
    }

    @Test
    fun `filling form without create does not create any entities`() {
        val scenario = Scenario.init(
            "Entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.next()
        scenario.answer("Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `filling form with create makes entity available`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                        XFormsElement.t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name"),
                        XFormsElement.setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.next()
        scenario.answer("Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].id, equalTo(scenario.answerOf<StringData>("/data/meta/entity/@id").value))
        assertThat(entities[0].label, equalTo("Tom Wambsgans"))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Tom Wambsgans"))))
        assertThat(entities[0].action, equalTo(EntityAction.CREATE))
    }

    @Test
    fun `filling form with create without an id makes entity available`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("id"),
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                        XFormsElement.t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/id").type("string"),
                        bind("/data/meta/entity/@id").type("string").calculate("/data/id"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/id"),
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].id, equalTo(null))
        assertThat(entities[0].action, equalTo(EntityAction.CREATE))
    }

    @Test
    fun `filling form with update makes entity available`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Update entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"update-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                        XFormsElement.t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.next()
        scenario.answer("Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].id, equalTo("123"))
        assertThat(entities[0].label, equalTo("Tom Wambsgans"))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Tom Wambsgans"))))
        assertThat(entities[0].action, equalTo(EntityAction.UPDATE))
    }

    @Test
    fun `filling form with update and no label makes entity available with null label`() {
        val scenario = Scenario.init(
            "Update entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Update entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"update-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                        bind("/data/meta/entity/@id").type("string")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.next()
        scenario.answer("Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].id, equalTo("123"))
        assertThat(entities[0].label, equalTo(null))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Tom Wambsgans"))))
    }

    @Test
    fun `filling form with update with null id makes entity available`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Update entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"update-entity-form\"",
                                XFormsElement.t("id"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" update=\"1\" id=\"\" baseVersion=\"\"")
                                )
                            )
                        ),
                        bind("/data/id").type("string"),
                        bind("/data/meta/entity/@id").type("string").calculate("/data/id").readonly()
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/id")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].id, equalTo(null))
        assertThat(entities[0].action, equalTo(EntityAction.UPDATE))
    }

    @Test
    fun `filling form with create and update does not make entity available`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Upsert entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"upsert-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                        XFormsElement.t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.next()
        scenario.answer("Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `filling form with dynamic create expression conditionally creates entities`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t("join"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"members\" create=\"\" id=\"1\"")
                                )
                            )
                        ),
                        bind("/data/meta/entity/@create").calculate("/data/join = 'yes'"),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name"),
                    XFormsElement.select1("/data/join", XFormsElement.item("yes", "Yes"), XFormsElement.item("no", "No"))
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.next()
        scenario.answer("Roman Roy")
        scenario.next()
        scenario.answer(scenario.choicesOf("/data/join")[0])
        scenario.finalizeInstance()

        var entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))

        scenario.newInstance()
        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.next()
        scenario.answer("Roman Roy")
        scenario.next()
        scenario.answer(scenario.choicesOf("/data/join")[1])
        scenario.finalizeInstance()

        entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `entity form can be serialized`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entities:entity dataset=\"people\" create=\"1\" id=\"1\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        val deserializedScenario = scenario.serializeAndDeserializeForm()
        deserializedScenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        deserializedScenario.next()
        deserializedScenario.answer("Shiv Roy")
        deserializedScenario.finalizeInstance()

        val entities = deserializedScenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Shiv Roy"))))
    }

    @Test
    fun `entities namespace works regardless of name`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("blah", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("blah:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\" id=\"1\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("blah", "saveto", "name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.next()
        scenario.answer("Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Tom Wambsgans"))))
    }

    @Test
    fun `filling form with select saveto and with create saves values correctly to entity`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("team"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\" id=\"1\"")
                                )
                            )
                        ),
                        bind("/data/team").type("string").withAttribute("entities", "saveto", "team")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.select1("/data/team", XFormsElement.item("kendall", "Kendall"), XFormsElement.item("logan", "Logan"))
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.next()
        scenario.answer(scenario.choicesOf("/data/team")[0])
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(listOf(Pair("team", "kendall"))))
    }

    @Test
    fun `when saveto question is not answered, entity property is empty string`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\" id=\"1\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", ""))))
    }

    @Test
    fun `saveto is removed from bind attributes for clients`() {
        val scenario = Scenario.init(
            "Create entity form",
            XFormsElement.html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                XFormsElement.head(
                    XFormsElement.title("Create entity form"),
                    XFormsElement.model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        XFormsElement.mainInstance(
                            XFormsElement.t(
                                "data id=\"create-entity-form\"",
                                XFormsElement.t("name"),
                                XFormsElement.t(
                                    "meta",
                                    XFormsElement.t("entity dataset=\"people\" create=\"1\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                    )
                ),
                XFormsElement.body(
                    XFormsElement.input("/data/name")
                )
            )
        )

        scenario.next()
        val bindAttributes: List<TreeElement> = scenario.formEntryPromptAtIndex.bindAttributes
        val containsSaveTo = bindAttributes.any { it.name == "saveto" }
        assertThat(containsSaveTo, equalTo(false))
    }
}
