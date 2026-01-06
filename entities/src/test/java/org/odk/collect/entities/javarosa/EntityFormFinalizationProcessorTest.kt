package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.data.DateData
import org.javarosa.core.model.data.StringData
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.group
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.setvalue
import org.javarosa.test.XFormsElement.t
import org.javarosa.test.XFormsElement.title
import org.javarosa.xform.parse.XFormParserFactory
import org.javarosa.xform.util.XFormUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor
import org.odk.collect.entities.javarosa.finalization.FormEntity
import org.odk.collect.entities.javarosa.parse.EntityXFormParserFactory
import java.sql.Date

class EntityFormFinalizationProcessorTest {

    private val entityXFormParserFactory = EntityXFormParserFactory(XFormParserFactory())

    @Before
    fun setup() {
        XFormUtils.setXFormParserFactory(entityXFormParserFactory)
    }

    @After
    fun teardown() {
        XFormUtils.setXFormParserFactory(XFormParserFactory())
    }

    @Test
    fun `when form does not have entity element, adds no entities to extras`() {
        val scenario = Scenario.init(
            "Normal form",
            html(
                head(
                    title("Normal form"),
                    model(
                        mainInstance(
                            t(
                                "data id=\"normal\"",
                                t("name")
                            )
                        ),
                        bind("/data/name").type("string")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        val processor = EntityFormFinalizationProcessor()
        val model = scenario.formEntryController.model
        processor.processForm(model)
        assertThat(model.extras.get(EntitiesExtra::class.java), equalTo(null))
    }

    @Test
    fun `when saveTo is not relevant, it is not included in entity`() {
        val scenario = Scenario.init(
            "Create entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Create entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entity-form\"",
                                t("name"),
                                t(
                                    "meta",
                                    t(
                                        "entity dataset=\"people\" create=\"1\" id=\"\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string")
                            .withAttribute("entities", "saveto", "name")
                            .relevant("false()"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string")
                            .calculate("/data/name"),
                        setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        val processor = EntityFormFinalizationProcessor()
        val model = scenario.formEntryController.model
        processor.processForm(model)

        val entities: List<FormEntity> =
            model.extras.get(EntitiesExtra::class.java).entities

        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(emptyList()))
    }

    @Test
    fun `creates entity with values treated as opaque strings`() {
        val scenario = Scenario.init(
            "Create entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Create entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entity-form\"",
                                t("birthday"),
                                t(
                                    "meta",
                                    t(
                                        "entity dataset=\"people\" create=\"1\" id=\"\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/birthday").type("date")
                            .withAttribute("entities", "saveto", "birthday"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string")
                            .calculate("/data/birthday"),
                        setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                    )
                ),
                body(
                    input("/data/birthday")
                )
            )
        )

        val processor = EntityFormFinalizationProcessor()
        val model = scenario.formEntryController.model

        scenario.next()
        scenario.formEntryController.answerQuestion(DateData(Date.valueOf("2024-11-15")), true)

        processor.processForm(model)

        val entities = model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(
            entities[0].properties[0],
            equalTo(Pair("birthday", "2024-11-15"))
        )
    }

    @Test
    fun `when saveTo is in not relevant group, it is not included in entity`() {
        val scenario = Scenario.init(
            "Create entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Create entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entity-form\"",
                                t(
                                    "group",
                                    t("name")
                                ),
                                t(
                                    "meta",
                                    t(
                                        "entity dataset=\"people\" create=\"1\" id=\"\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/group").relevant("false()"),
                        bind("/data/group/name").type("string")
                            .withAttribute("entities", "saveto", "name"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string")
                            .calculate("/data/group/name"),
                        setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                    )
                ),
                body(
                    group(
                        "/data/group",
                        input("/data/group/name")
                    )
                )
            )
        )

        val processor = EntityFormFinalizationProcessor()
        val model = scenario.formEntryController.model
        processor.processForm(model)

        val entities = model.extras.get(EntitiesExtra::class.java).entities

        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(emptyList()))
    }

    @Test
    fun `when saveTo is nested in an extra group, creates entity with values`() {
        val scenario = Scenario.init(
            "Create entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Create entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entity-form\"",
                                t(
                                    "group",
                                    t("name")
                                ),
                                t(
                                    "meta",
                                    t(
                                        "entity dataset=\"people\" create=\"1\" id=\"\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/group"),
                        bind("/data/group/name").type("string")
                            .withAttribute("entities", "saveto", "name"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string")
                            .calculate("/data/group/name"),
                        setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                    )
                ),
                body(
                    group(
                        "/data/group",
                        input("/data/group/name")
                    )
                )
            )
        )

        val processor = EntityFormFinalizationProcessor()
        val model = scenario.formEntryController.model
        scenario.next()
        scenario.next()
        scenario.formEntryController.answerQuestion(StringData("John"), true)
        processor.processForm(model)

        val entities = model.extras.get(EntitiesExtra::class.java).entities

        assertThat(entities.size, equalTo(1))
        assertThat(
            entities[0].properties[0],
            equalTo(Pair("name", "John"))
        )
    }
}
