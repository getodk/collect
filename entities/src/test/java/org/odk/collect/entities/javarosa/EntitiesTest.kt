package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.data.UncastData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.group
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.item
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.repeat
import org.javarosa.test.XFormsElement.select1
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
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.javarosa.spec.EntityAction.CREATE
import org.odk.collect.entities.javarosa.support.EntityXFormsElement.entityLabelBind
import org.odk.collect.entities.javarosa.support.EntityXFormsElement.entityNode
import org.odk.collect.entities.javarosa.support.EntityXFormsElement.withSaveTo

class EntitiesTest {
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
    fun `filling form without create does not create any entities`() {
        val scenario = Scenario.init(
            "Entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"entity-form\"",
                                t("name"),
                                t(
                                    "meta",
                                    t("entity dataset=\"people\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/name", "Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `filling form with create makes entity available`() {
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
                                    t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name"),
                        setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/name", "Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].dataset, equalTo("people"))
        assertThat(entities[0].id, equalTo(scenario.answerOf<StringData>("/data/meta/entity/@id").value))
        assertThat(entities[0].label, equalTo("Tom Wambsgans"))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Tom Wambsgans"))))
        assertThat(entities[0].action, equalTo(CREATE))
    }

    @Test
    fun `filling form with create in multiple groups makes entities available`() {
        val scenario = Scenario.init(
            "Create entities from multiple groups form",
            html(
                listOf("entities" to "http://www.opendatakit.org/xforms/entities"),
                head(
                    title("Create entities from multiple groups form"),
                    model(
                        listOf(Pair("entities:entities-version", "2025.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entities-from-multiple-groups-form\"",
                                t(
                                    "people",
                                    t("name"),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"people\" create=\"1\" id=\"\"",
                                            t("label")
                                        )
                                    )
                                ),
                                t(
                                    "cars",
                                    t("model"),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"cars\" create=\"1\" id=\"\"",
                                            t("label")
                                        )
                                    )
                                )
                            )
                        ),
                        bind("/data/people/name").type("string").withSaveTo("name"),
                        bind("/data/people/meta/entity/@id").type("string"),
                        bind("/data/people/meta/entity/label").type("string").calculate("/data/people/name"),
                        setvalue("odk-instance-first-load", "/data/people/meta/entity/@id", "uuid()"),
                        bind("/data/cars/model").type("string").withSaveTo("car_model"),
                        bind("/data/cars/meta/entity/@id").type("string"),
                        bind("/data/cars/meta/entity/label").type("string").calculate("/data/cars/model"),
                        setvalue("odk-instance-first-load", "/data/cars/meta/entity/@id", "uuid()"),
                    )
                ),
                body(
                    group(
                        "/data/people",
                        input("/data/people/name"),
                    ),
                    group(
                        "/data/cars",
                        input("/data/cars/model"),
                    )
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/people/name", "Tom Wambsgans")
        scenario.answer("/data/cars/model", "Range Rover")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(2))

        assertThat(
            entities,
            containsInAnyOrder(
                FormEntity(
                    CREATE,
                    "people",
                    scenario.answerOf<StringData>("/data/people/meta/entity/@id").value as String,
                    "Tom Wambsgans",
                    listOf(Pair("name", "Tom Wambsgans"))
                ),
                FormEntity(
                    CREATE,
                    "cars",
                    scenario.answerOf<StringData>("/data/cars/meta/entity/@id").value as String,
                    "Range Rover",
                    listOf(Pair("car_model", "Range Rover"))
                )
            )
        )
    }

    @Test
    fun `filling form with update in multiple groups makes entities available`() {
        val scenario = Scenario.init(
            "Update entities from multiple groups form",
            html(
                listOf("entities" to "http://www.opendatakit.org/xforms/entities"),
                head(
                    title("Update entities from multiple groups form"),
                    model(
                        listOf(Pair("entities:entities-version", "2025.1.0")),
                        mainInstance(
                            t(
                                "data id=\"update-entities-from-multiple-groups-form\"",
                                t(
                                    "people",
                                    t("name"),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                            t("label")
                                        )
                                    )
                                ),
                                t(
                                    "cars",
                                    t("model"),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"cars\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                            t("label")
                                        )
                                    )
                                )
                            )
                        ),
                        bind("/data/people/name").type("string").withSaveTo("name"),
                        bind("/data/people/meta/entity/@id").type("string"),
                        bind("/data/people/meta/entity/label").type("string").calculate("/data/people/name"),
                        setvalue("odk-instance-first-load", "/data/people/meta/entity/@id", "uuid()"),
                        bind("/data/cars/model").type("string").withSaveTo("car_model"),
                        bind("/data/cars/meta/entity/@id").type("string"),
                        bind("/data/cars/meta/entity/label").type("string").calculate("/data/cars/model"),
                        setvalue("odk-instance-first-load", "/data/cars/meta/entity/@id", "uuid()"),
                    )
                ),
                body(
                    group(
                        "/data/people",
                        input("/data/people/name"),
                        setvalue("odk-new-repeat", "/data/people/meta/entity/@id", "uuid()")
                    ),
                    group(
                        "/data/cars",
                        input("/data/cars/model"),
                        setvalue("odk-new-repeat", "/data/cars/meta/entity/@id", "uuid()")
                    )
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/people/name", "Tom Wambsgans")
        scenario.answer("/data/cars/model", "Range Rover")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(2))

        assertThat(
            entities,
            containsInAnyOrder(
                FormEntity(
                    EntityAction.UPDATE,
                    "people",
                    scenario.answerOf<StringData>("/data/people/meta/entity/@id").value as String,
                    "Tom Wambsgans",
                    listOf(Pair("name", "Tom Wambsgans"))
                ),
                FormEntity(
                    EntityAction.UPDATE,
                    "cars",
                    scenario.answerOf<StringData>("/data/cars/meta/entity/@id").value as String,
                    "Range Rover",
                    listOf(Pair("car_model", "Range Rover"))
                )
            )
        )
    }

    @Test
    fun `filling form with create in repeats makes entities available`() {
        val scenario = Scenario.init(
            "Create entities from repeats form",
            html(
                listOf("entities" to "http://www.opendatakit.org/xforms/entities"),
                head(
                    title("Create entities from repeats form"),
                    model(
                        listOf(Pair("entities:entities-version", "2025.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entities-from-repeats-form\"",
                                t(
                                    "people",
                                    t("name"),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"people\" create=\"1\" id=\"\"",
                                            t("label")
                                        )
                                    )
                                )
                            )
                        ),
                        bind("/data/people/name").type("string").withSaveTo("name"),
                        bind("/data/people/meta/entity/@id").type("string"),
                        bind("/data/people/meta/entity/label").type("string").calculate("/data/people/name"),
                        setvalue("odk-instance-first-load", "/data/people/meta/entity/@id", "uuid()"),
                    )
                ),
                body(
                    repeat(
                        "/data/people",
                        input("/data/people/name"),
                        setvalue("odk-new-repeat", "/data/people/meta/entity/@id", "uuid()")
                    )
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/people[1]/name", "Tom Wambsgans")
        scenario.createNewRepeat("/data/people")
        scenario.answer("/data/people[2]/name", "Shiv Roy")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(2))

        assertThat(
            entities,
            containsInAnyOrder(
                FormEntity(
                    CREATE,
                    "people",
                    scenario.answerOf<StringData>("/data/people[1]/meta/entity/@id").value as String,
                    "Tom Wambsgans",
                    listOf(Pair("name", "Tom Wambsgans"))
                ),
                FormEntity(
                    CREATE,
                    "people",
                    scenario.answerOf<UncastData>("/data/people[2]/meta/entity/@id").value as String,
                    "Shiv Roy",
                    listOf(Pair("name", "Shiv Roy"))
                )
            )
        )
    }

    @Test
    fun `filling form with update in repeats makes entities available`() {
        val scenario = Scenario.init(
            "Update entities from repeats form",
            html(
                listOf("entities" to "http://www.opendatakit.org/xforms/entities"),
                head(
                    title("Update entities from repeats form"),
                    model(
                        listOf(Pair("entities:entities-version", "2025.1.0")),
                        mainInstance(
                            t(
                                "data id=\"update-entities-from-repeats-form\"",
                                t(
                                    "people",
                                    t("name"),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                            t("label")
                                        )
                                    )
                                )
                            )
                        ),
                        bind("/data/people/name").type("string").withSaveTo("name"),
                        bind("/data/people/meta/entity/@id").type("string"),
                        bind("/data/people/meta/entity/label").type("string").calculate("/data/people/name"),
                        setvalue("odk-instance-first-load", "/data/people/meta/entity/@id", "uuid()"),
                    )
                ),
                body(
                    repeat(
                        "/data/people",
                        input("/data/people/name"),
                        setvalue("odk-new-repeat", "/data/people/meta/entity/@id", "uuid()")
                    )
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/people[1]/name", "Tom Wambsgans")
        scenario.createNewRepeat("/data/people")
        scenario.answer("/data/people[2]/name", "Shiv Roy")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(2))

        assertThat(
            entities,
            containsInAnyOrder(
                FormEntity(
                    EntityAction.UPDATE,
                    "people",
                    scenario.answerOf<StringData>("/data/people[1]/meta/entity/@id").value as String,
                    "Tom Wambsgans",
                    listOf(Pair("name", "Tom Wambsgans"))
                ),
                FormEntity(
                    EntityAction.UPDATE,
                    "people",
                    scenario.answerOf<UncastData>("/data/people[2]/meta/entity/@id").value as String,
                    "Shiv Roy",
                    listOf(Pair("name", "Shiv Roy"))
                )
            )
        )
    }

    @Test
    fun `filling form with create in nested repeats makes entities available`() {
        val scenario = Scenario.init(
            "Create entities from nested repeats form",
            html(
                listOf("entities" to "http://www.opendatakit.org/xforms/entities"),
                head(
                    title("Create entities from nested repeats form"),
                    model(
                        listOf(Pair("entities:entities-version", "2025.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entities-from-nested-repeats-form\"",
                                t(
                                    "people",
                                    t("name"),
                                    t(
                                        "cars",
                                        t("model"),
                                        t(
                                            "meta",
                                            t(
                                                "entity dataset=\"cars\" create=\"1\" id=\"\"",
                                                t("label")
                                            )
                                        )
                                    ),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"people\" create=\"1\" id=\"\"",
                                            t("label")
                                        )
                                    )
                                )
                            )
                        ),
                        bind("/data/people/name").type("string").withSaveTo("name"),
                        bind("/data/people/meta/entity/@id").type("string"),
                        bind("/data/people/meta/entity/label").type("string").calculate("/data/people/name"),
                        setvalue("odk-instance-first-load", "/data/people/meta/entity/@id", "uuid()"),
                        bind("/data/people/cars/model").type("string").withSaveTo("car_model"),
                        bind("/data/people/cars/meta/entity/@id").type("string"),
                        bind("/data/people/cars/meta/entity/label").type("string").calculate("/data/people/cars/model"),
                        setvalue("odk-instance-first-load", "/data/people/cars/meta/entity/@id", "uuid()"),
                    )
                ),
                body(
                    repeat(
                        "/data/people",
                        input("/data/people/name"),
                        setvalue("odk-new-repeat", "/data/people/meta/entity/@id", "uuid()"),
                        repeat(
                            "/data/people/cars",
                            input("/data/people/cars/model"),
                            setvalue("odk-new-repeat", "/data/people/cars/meta/entity/@id", "uuid()")
                        )
                    )
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/people[1]/name", "Tom Wambsgans")
        scenario.answer("/data/people[1]/cars[1]/model", "Range Rover")
        scenario.createNewRepeat("/data/people")
        scenario.answer("/data/people[2]/name", "Shiv Roy")
        scenario.createNewRepeat("/data/people[2]/cars")
        scenario.answer("/data/people[2]/cars[1]/model", "Audi A8")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(4))

        assertThat(
            entities,
            containsInAnyOrder(
                FormEntity(
                    CREATE,
                    "people",
                    scenario.answerOf<StringData>("/data/people[1]/meta/entity/@id").value as String,
                    "Tom Wambsgans",
                    listOf(Pair("name", "Tom Wambsgans"))
                ),
                FormEntity(
                    CREATE,
                    "people",
                    scenario.answerOf<UncastData>("/data/people[2]/meta/entity/@id").value as String,
                    "Shiv Roy",
                    listOf(Pair("name", "Shiv Roy"))
                ),
                FormEntity(
                    CREATE,
                    "cars",
                    scenario.answerOf<StringData>("/data/people[1]/cars[1]/meta/entity/@id").value as String,
                    "Range Rover",
                    listOf(Pair("car_model", "Range Rover"))
                ),
                FormEntity(
                    CREATE,
                    "cars",
                    scenario.answerOf<UncastData>("/data/people[2]/cars[1]/meta/entity/@id").value as String,
                    "Audi A8",
                    listOf(Pair("car_model", "Audi A8"))
                )
            )
        )
    }

    @Test
    fun `filling form with update in nested repeats makes entities available`() {
        val scenario = Scenario.init(
            "Update entities from nested repeats form",
            html(
                listOf("entities" to "http://www.opendatakit.org/xforms/entities"),
                head(
                    title("Update entities from nested repeats form"),
                    model(
                        listOf(Pair("entities:entities-version", "2025.1.0")),
                        mainInstance(
                            t(
                                "data id=\"update-entities-from-nested-repeats-form\"",
                                t(
                                    "people",
                                    t("name"),
                                    t(
                                        "cars",
                                        t("model"),
                                        t(
                                            "meta",
                                            t(
                                                "entity dataset=\"cars\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                                t("label")
                                            )
                                        )
                                    ),
                                    t(
                                        "meta",
                                        t(
                                            "entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                            t("label")
                                        )
                                    )
                                )
                            )
                        ),
                        bind("/data/people/name").type("string").withSaveTo("name"),
                        bind("/data/people/meta/entity/@id").type("string"),
                        bind("/data/people/meta/entity/label").type("string").calculate("/data/people/name"),
                        setvalue("odk-instance-first-load", "/data/people/meta/entity/@id", "uuid()"),
                        bind("/data/people/cars/model").type("string").withSaveTo("car_model"),
                        bind("/data/people/cars/meta/entity/@id").type("string"),
                        bind("/data/people/cars/meta/entity/label").type("string").calculate("/data/people/cars/model"),
                        setvalue("odk-instance-first-load", "/data/people/cars/meta/entity/@id", "uuid()"),
                    )
                ),
                body(
                    repeat(
                        "/data/people",
                        input("/data/people/name"),
                        setvalue("odk-new-repeat", "/data/people/meta/entity/@id", "uuid()"),
                        repeat(
                            "/data/people/cars",
                            input("/data/people/cars/model"),
                            setvalue("odk-new-repeat", "/data/people/cars/meta/entity/@id", "uuid()")
                        )
                    )
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/people[1]/name", "Tom Wambsgans")
        scenario.answer("/data/people[1]/cars[1]/model", "Range Rover")
        scenario.createNewRepeat("/data/people")
        scenario.answer("/data/people[2]/name", "Shiv Roy")
        scenario.createNewRepeat("/data/people[2]/cars")
        scenario.answer("/data/people[2]/cars[1]/model", "Audi A8")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(4))

        assertThat(
            entities,
            containsInAnyOrder(
                FormEntity(
                    EntityAction.UPDATE,
                    "people",
                    scenario.answerOf<StringData>("/data/people[1]/meta/entity/@id").value as String,
                    "Tom Wambsgans",
                    listOf(Pair("name", "Tom Wambsgans"))
                ),
                FormEntity(
                    EntityAction.UPDATE,
                    "people",
                    scenario.answerOf<UncastData>("/data/people[2]/meta/entity/@id").value as String,
                    "Shiv Roy",
                    listOf(Pair("name", "Shiv Roy"))
                ),
                FormEntity(
                    EntityAction.UPDATE,
                    "cars",
                    scenario.answerOf<StringData>("/data/people[1]/cars[1]/meta/entity/@id").value as String,
                    "Range Rover",
                    listOf(Pair("car_model", "Range Rover"))
                ),
                FormEntity(
                    EntityAction.UPDATE,
                    "cars",
                    scenario.answerOf<UncastData>("/data/people[2]/cars[1]/meta/entity/@id").value as String,
                    "Audi A8",
                    listOf(Pair("car_model", "Audi A8"))
                )
            )
        )
    }

    @Test
    fun `filling form with create without an id does not make entity available`() {
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
                                t("id"),
                                t("name"),
                                t(
                                    "meta",
                                    t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/id").type("string"),
                        bind("/data/meta/entity/@id").type("string").calculate("/data/id"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name")
                    )
                ),
                body(
                    input("/data/id"),
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `filling form with update makes entity available`() {
        val scenario = Scenario.init(
            "Update entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Update entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"update-entity-form\"",
                                t("name"),
                                t(
                                    "meta",
                                    t("entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name"),
                        bind("/data/meta/entity/@id").type("string"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.answer("/data/name", "Tom Wambsgans")
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
    fun `filling form with update without an id does not make entity available`() {
        val scenario = Scenario.init(
            "Update entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Update entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"update-entity-form\"",
                                t("id"),
                                t(
                                    "meta",
                                    t("entity dataset=\"people\" update=\"1\" id=\"\" baseVersion=\"\"")
                                )
                            )
                        ),
                        bind("/data/id").type("string"),
                        bind("/data/meta/entity/@id").type("string").calculate("/data/id").readonly()
                    )
                ),
                body(
                    input("/data/id")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `filling form with create and update does not make entity available`() {
        val scenario = Scenario.init(
            "Upsert entity form",
            html(
                listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Upsert entity form"),
                    model(
                        listOf(Pair("entities:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"upsert-entity-form\"",
                                t("name"),
                                t(
                                    "meta",
                                    t("entity dataset=\"people\" create=\"1\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                        t("label")
                                    )
                                )
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name"),
                        bind("/data/meta/entity/label").type("string").calculate("/data/name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.answer("/data/name", "Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }

    @Test
    fun `filling form with dynamic create expression conditionally creates entities`() {
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
                                t("join"),
                                t(
                                    "meta",
                                    entityNode("members", CREATE, optionalAction = false)
                                )
                            )
                        ),
                        bind("/data/meta/entity/@create").calculate("/data/join = 'yes'"),
                        bind("/data/name").type("string").withSaveTo("name"),
                        entityLabelBind("/data/name")
                    )
                ),
                body(
                    input("/data/name"),
                    select1("/data/join", item("yes", "Yes"), item("no", "No"))
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
                                    entityNode("people", CREATE)
                                )
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name"),
                        entityLabelBind("/data/name")
                    )
                ),
                body(
                    input("/data/name")
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
            html(
                listOf(Pair("blah", "http://www.opendatakit.org/xforms/entities")),
                head(
                    title("Create entity form"),
                    model(
                        listOf(Pair("blah:entities-version", "2024.1.0")),
                        mainInstance(
                            t(
                                "data id=\"create-entity-form\"",
                                t("name"),
                                t("meta", entityNode("people", CREATE))
                            )
                        ),
                        bind("/data/name").type("string").withAttribute("blah", "saveto", "name"),
                        entityLabelBind("/data/name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())

        scenario.answer("/data/name", "Tom Wambsgans")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(listOf(Pair("name", "Tom Wambsgans"))))
    }

    @Test
    fun `filling form with select saveto and with create saves values correctly to entity`() {
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
                                t("team"),
                                t("meta", entityNode("people", CREATE))
                            )
                        ),
                        bind("/data/team").type("string").withSaveTo("team"),
                        entityLabelBind("/data/team")
                    )
                ),
                body(
                    select1("/data/team", item("kendall", "Kendall"),
                        item("logan", "Logan")
                    )
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
                                t("age"),
                                t("meta", entityNode("people", CREATE))
                            )
                        ),
                        bind("/data/name").type("string"),
                        bind("/data/age").withSaveTo("age"),
                        entityLabelBind("/data/name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.answer("/data/name", "James")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties, equalTo(listOf(Pair("age", ""))))
    }

    @Test
    fun `saveto is removed from bind attributes for clients`() {
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
                                    t("entity dataset=\"people\" create=\"1\"")
                                )
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.next()
        val bindAttributes: List<TreeElement> = scenario.formEntryPromptAtIndex.bindAttributes
        val containsSaveTo = bindAttributes.any { it.name == "saveto" }
        assertThat(containsSaveTo, equalTo(false))
    }

    @Test
    fun `filling form with blank label does not make entity available`() {
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
                                t("meta", entityNode("people", CREATE))
                            )
                        ),
                        bind("/data/name").type("string").withSaveTo("name"),
                        entityLabelBind("/data/name")
                    )
                ),
                body(
                    input("/data/name")
                )
            )
        )

        scenario.formEntryController.addPostProcessor(EntityFormFinalizationProcessor())
        scenario.answer("/data/name", " ")
        scenario.finalizeInstance()

        val entities = scenario.formEntryController.model.extras.get(EntitiesExtra::class.java).entities
        assertThat(entities.size, equalTo(0))
    }
}
