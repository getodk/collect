package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.FormDef
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.input
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.t
import org.javarosa.test.XFormsElement.title
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParser.MissingModelAttributeException
import org.junit.Assert.fail
import org.junit.Test
import org.odk.collect.entities.javarosa.parse.EntityFormExtra
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor
import org.odk.collect.entities.javarosa.spec.UnrecognizedEntityVersionException
import org.odk.collect.entities.javarosa.support.EntityXFormsElement.withSaveTo
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class EntityFormParseProcessorTest {
    @Test
    fun `when version is missing parses without error`() {
        val form = html(
            head(
                title("Non entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta")
                        )
                    ),
                    bind("/data/name").type("string")
                )
            ),
            body(
                input("/data/name")
            )
        )

        val processor = EntityFormParseProcessor()
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)
        parser.parse(null)
    }

    @Test
    fun `when version is missing and there is an entity element throws exception`() {
        val form = html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
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

        val processor = EntityFormParseProcessor()
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
        val form = html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            head(
                title("Create entity form"),
                model(
                    listOf(Pair("entities:entities-version", "somethingElse")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
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

        val processor = EntityFormParseProcessor()
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)
        parser.parse(null)
    }

    @Test
    fun `when version is new patch parses correctly`() {
        val newPatchVersion = "2022.1.12"
        val form = html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            head(
                title("Create entity form"),
                model(
                    listOf(Pair("entities:entities-version", newPatchVersion)),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
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

        val processor = EntityFormParseProcessor()
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef, notNullValue())
    }

    @Test
    fun `when version is new version with updates parses correctly`() {
        val updateVersion = "2023.1.0"
        val form = html(
            listOf(Pair("entities", "http://www.opendatakit.org/xforms/entities")),
            head(
                title("Create entity form"),
                model(
                    listOf(Pair("entities:entities-version", updateVersion)),
                    mainInstance(
                        t("data id=\"update-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" update=\"1\" id=\"17\"")
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

        val processor = EntityFormParseProcessor()
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef, notNullValue())
    }

    @Test
    fun `saveTos with incorrect namespace are ignored`() {
        val form = html(
            listOf(
                Pair("correct", "http://www.opendatakit.org/xforms/entities"),
                Pair("incorrect", "blah")
            ),
            head(
                title("Create entity form"),
                model(
                    listOf(Pair("correct:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("incorrect", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        )

        val processor = EntityFormParseProcessor()
        val parser = XFormParser(InputStreamReader(ByteArrayInputStream(form.asXml().toByteArray())))
        parser.addProcessor(processor)

        val formDef: FormDef = parser.parse(null)
        assertThat(formDef.extras.get(EntityFormExtra::class.java).saveTos.isEmpty(), equalTo(true))
    }
}
