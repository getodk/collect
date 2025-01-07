package org.odk.collect.entities.javarosa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.junit.Assert.fail;
import static java.util.Arrays.asList;

import org.javarosa.core.model.FormDef;
import org.javarosa.test.XFormsElement;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.parse.XFormParser.MissingModelAttributeException;
import org.junit.Test;
import org.odk.collect.entities.javarosa.parse.EntityFormExtra;
import org.odk.collect.entities.javarosa.parse.EntityFormParseProcessor;
import org.odk.collect.entities.javarosa.spec.UnrecognizedEntityVersionException;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import kotlin.Pair;

public class EntityFormParseProcessorTest {

    @Test
    public void whenVersionIsMissing_parsesWithoutError() throws XFormParser.ParseException {
        XFormsElement form = XFormsElement.html(
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
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);
        parser.parse(null);
    }

    @Test
    public void whenVersionIsMissing_andThereIsAnEntityElement_throwsException() {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);

        try {
            parser.parse(null);
            fail("Expected exception!");
        } catch (Exception e) {
            assertThat(e, instanceOf(MissingModelAttributeException.class));

            MissingModelAttributeException missingModelAttributeException = (MissingModelAttributeException) e;
            assertThat(missingModelAttributeException.getNamespace(), equalTo("http://www.opendatakit.org/xforms/entities"));
            assertThat(missingModelAttributeException.getName(), equalTo("entities-version"));
        }
    }

    @Test(expected = UnrecognizedEntityVersionException.class)
    public void whenVersionIsNotRecognized_throwsException() throws XFormParser.ParseException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "somethingElse")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);
        parser.parse(null);
    }

    @Test
    public void whenVersionIsNewPatch_parsesCorrectly() throws XFormParser.ParseException {
        String newPatchVersion = "2022.1.12";

        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", newPatchVersion)),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);

        FormDef formDef = parser.parse(null);
        assertThat(formDef, notNullValue());
    }

    @Test
    public void whenVersionIsNewVersionWithUpdates_parsesCorrectly() throws XFormParser.ParseException {
        String updateVersion = "2023.1.0";

        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", updateVersion)),
                    mainInstance(
                        t("data id=\"update-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" update=\"1\" id=\"17\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);

        FormDef formDef = parser.parse(null);
        assertThat(formDef, notNullValue());
    }

    @Test
    public void saveTosWithIncorrectNamespaceAreIgnored() throws XFormParser.ParseException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("correct", "http://www.opendatakit.org/xforms/entities"),
                new Pair<>("incorrect", "blah")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("correct:entities-version", "2024.1.0")),
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
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);

        FormDef formDef = parser.parse(null);
        assertThat(formDef.getExtras().get(EntityFormExtra.class).getSaveTos(), is(empty()));
    }
}
