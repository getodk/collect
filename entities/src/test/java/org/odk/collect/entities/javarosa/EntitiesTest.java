package org.odk.collect.entities.javarosa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.item;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.select1;
import static org.javarosa.test.XFormsElement.setvalue;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static java.util.Arrays.asList;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.test.Scenario;
import org.javarosa.test.XFormsElement;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.parse.XFormParserFactory;
import org.javarosa.xform.util.XFormUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra;
import org.odk.collect.entities.javarosa.finalization.FormEntity;
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor;
import org.odk.collect.entities.javarosa.parse.EntityXFormParserFactory;
import org.odk.collect.entities.javarosa.spec.EntityAction;

import java.io.IOException;
import java.util.List;

import kotlin.Pair;

public class EntitiesTest {

    private final EntityXFormParserFactory entityXFormParserFactory = new EntityXFormParserFactory(new XFormParserFactory());

    @Before
    public void setup() {
        XFormUtils.setXFormParserFactory(entityXFormParserFactory);
    }

    @After
    public void teardown() {
        XFormUtils.setXFormParserFactory(new XFormParserFactory());
    }

    @Test
    public void fillingFormWithoutCreate_doesNotCreateAnyEntities() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"entity-form\"",
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
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void fillingFormWithCreate_makesEntityAvailable() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                    t("label")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                    bind("/data/meta/entity/@id").type("string"),
                    bind("/data/meta/entity/label").type("string").calculate("/data/name"),
                    setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).id, equalTo(scenario.answerOf("/data/meta/entity/@id").getValue()));
        assertThat(entities.get(0).label, equalTo("Tom Wambsgans"));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
        assertThat(entities.get(0).action, equalTo(EntityAction.CREATE));
    }

    @Test
    public void fillingFormWithCreate_withoutAnId_makesEntityAvailable() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("id"),
                            t("name"),
                            t("meta",
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
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());
        scenario.finalizeInstance();

        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).id, equalTo(null));
        assertThat(entities.get(0).action, equalTo(EntityAction.CREATE));
    }

    @Test
    public void fillingFormWithUpdate_makesEntityAvailable() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Update entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"update-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                    t("label")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                    bind("/data/meta/entity/@id").type("string"),
                    bind("/data/meta/entity/label").type("string").calculate("/data/name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).id, equalTo("123"));
        assertThat(entities.get(0).label, equalTo("Tom Wambsgans"));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
        assertThat(entities.get(0).action, equalTo(EntityAction.UPDATE));
    }

    @Test
    public void fillingFormWithUpdate_andNoLabel_makesEntityAvailableWithNullLabel() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Update entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Update entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"update-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" update=\"1\" id=\"123\" baseVersion=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                    bind("/data/meta/entity/@id").type("string")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).id, equalTo("123"));
        assertThat(entities.get(0).label, equalTo(null));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
    }

    @Test
    public void fillingFormWithUpdate_withNullId_makesEntityAvailable() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Update entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"update-entity-form\"",
                            t("id"),
                            t("meta",
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
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());
        scenario.finalizeInstance();

        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).id, equalTo(null));
        assertThat(entities.get(0).action, equalTo(EntityAction.UPDATE));
    }

    @Test
    public void fillingFormWithCreateAndUpdate_doesNotMakeEntityAvailable() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Upsert entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"upsert-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\" update=\"1\" id=\"123\" baseVersion=\"1\"",
                                    t("label")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name"),
                    bind("/data/meta/entity/label").type("string").calculate("/data/name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void fillingFormWithDynamicCreateExpression_conditionallyCreatesEntities() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("join"),
                            t("meta",
                                t("entity dataset=\"members\" create=\"\" id=\"1\"")
                            )
                        )
                    ),
                    bind("/data/meta/entity/@create").calculate("/data/join = 'yes'"),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name"),
                select1("/data/join", item("yes", "Yes"), item("no", "No"))
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Roman Roy");
        scenario.next();
        scenario.answer(scenario.choicesOf("/data/join").get(0));

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));

        scenario.newInstance();
        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());
        scenario.next();
        scenario.answer("Roman Roy");
        scenario.next();
        scenario.answer(scenario.choicesOf("/data/join").get(1));

        scenario.finalizeInstance();
        entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void entityFormCanBeSerialized() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entities:entity dataset=\"people\" create=\"1\" id=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        deserializedScenario.next();
        deserializedScenario.answer("Shiv Roy");

        deserializedScenario.finalizeInstance();
        List<FormEntity> entities = deserializedScenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Shiv Roy"))));
    }

    @Test
    public void entitiesNamespaceWorksRegardlessOfName() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("blah", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("blah:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\" id=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("blah", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
    }

    @Test
    public void fillingFormWithSelectSaveTo_andWithCreate_savesValuesCorrectlyToEntity() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("team"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\" id=\"1\"")
                            )
                        )
                    ),
                    bind("/data/team").type("string").withAttribute("entities", "saveto", "team")
                )
            ),
            body(
                select1("/data/team", item("kendall", "Kendall"), item("logan", "Logan"))
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer(scenario.choicesOf("/data/team").get(0));

        scenario.finalizeInstance();
        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("team", "kendall"))));
    }

    @Test
    public void whenSaveToQuestionIsNotAnswered_entityPropertyIsEmptyString() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\" id=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());
        scenario.finalizeInstance();

        List<FormEntity> entities = scenario.getFormEntryController().getModel().getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", ""))));
    }

    @Test
    public void savetoIsRemovedFromBindAttributesForClients() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.next();
        List<TreeElement> bindAttributes = scenario.getFormEntryPromptAtIndex().getBindAttributes();
        boolean containsSaveTo = bindAttributes.stream().anyMatch(treeElement -> treeElement.getName().equals("saveto"));
        assertThat(containsSaveTo, is(false));
    }
}
