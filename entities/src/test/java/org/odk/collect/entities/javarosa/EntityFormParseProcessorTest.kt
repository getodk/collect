package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.FormDef
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.XFormsElement
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParser.MissingModelAttributeException
import org.junit.Assert.fail
import org.junit.Test
import org.odk.collect.entities.javarosa.parse.EntityFormExtra
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor
import org.odk.collect.entities.javarosa.spec.UnrecognizedEntityVersionException
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class EntityFormParseProcessorTest {
    @Test
    fun `when version is missing parses without error`() {
        val form = XFormsElement.html(
            XFormsElement.head(
                XFormsElement.title("Non entity form"),
                XFormsElement.model(
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"create-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta")
                        )
                    ),
                    bind("/data/name").type("string")
                )
            ),
            XFormsElement.body(
                XFormsElement.input("/data/name")
            )
        )

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)
        parser.parse(null)
    }

    @Test
    fun `when version is missing and there is an entity element throws exception`() {
        val form = XFormsElement.html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"create-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
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

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        try {
            parser.parse(null)
            fail("Expected exception!")
        } catch (e: Exception) {
            assertThat(e, instanceOf(MissingModelAttributeException::class.java))
            val ex = e as MissingModelAttributeException
            assertThat(ex.namespace, equalTo("http://www.opendatakit.org/xforms/entities"))
            assertThat(ex.name, equalTo("entities-version"))
        }
    }

    @Test(expected = UnrecognizedEntityVersionException::class)
    fun `when version is not recognized throws exception`() {
        val form = XFormsElement.html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    listOf(Pair("entities:entities-version", "somethingElse")),
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"create-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
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

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)
        parser.parse(null)
    }

    @Test
    fun `when version is new patch parses correctly`() {
        val newPatchVersion = "2022.1.12"
        val form = XFormsElement.html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    listOf(Pair("entities:entities-version", newPatchVersion)),
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"create-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
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

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef, notNullValue())
    }

    @Test
    fun `when version is new version with updates parses correctly`() {
        val updateVersion = "2023.1.0"
        val form = XFormsElement.html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    listOf(Pair("entities:entities-version", updateVersion)),
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"update-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
                                XFormsElement.t("entity dataset=\"people\" update=\"1\" id=\"17\"")
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

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef, notNullValue())
    }

    @Test
    fun `saveTos with incorrect namespace are ignored`() {
        val form = XFormsElement.html(
            listOf(
                Pair("correct", "http://www.opendatakit.org/xforms/entities"),
                Pair("incorrect", "blah")
            ),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    listOf(Pair("correct:entities-version", "2024.1.0")),
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"create-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
                                XFormsElement.t("entity dataset=\"people\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("incorrect", "saveto", "name")
                )
            ),
            XFormsElement.body(
                XFormsElement.input("/data/name")
            )
        )

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef.extras.get(EntityFormExtra::class.java).saveTos.isEmpty(), equalTo(true))
    }

    @Test
    fun `when version is 2025_1 and feature is enabled parses correctly`() {
        val updateVersion = "2025.1.0"
        val form = XFormsElement.html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    listOf(Pair("entities:entities-version", updateVersion)),
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"update-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
                                XFormsElement.t("entity dataset=\"people\" update=\"1\" id=\"17\"")
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

        val processor = EntityFormParseProcessor { true }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef.extras.get(EntityFormExtra::class.java).saveTos.isEmpty(), equalTo(false))
    }

    @Test(expected = UnrecognizedEntityVersionException::class)
    fun `when version is 2025_1 and feature is disabled throws exception`() {
        val updateVersion = "2025.1.0"
        val form = XFormsElement.html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            XFormsElement.head(
                XFormsElement.title("Create entity form"),
                XFormsElement.model(
                    listOf(Pair("entities:entities-version", updateVersion)),
                    XFormsElement.mainInstance(
                        XFormsElement.t("data id=\"update-entity-form\"",
                            XFormsElement.t("name"),
                            XFormsElement.t("meta",
                                XFormsElement.t("entity dataset=\"people\" update=\"1\" id=\"17\"")
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

        val processor = EntityFormParseProcessor { false }
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)
        parser.parse(null)
    }
}
