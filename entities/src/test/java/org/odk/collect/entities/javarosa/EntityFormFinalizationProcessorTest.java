package org.odk.collect.entities.javarosa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.group;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.setvalue;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import org.javarosa.core.model.data.DateData;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.test.Scenario;
import org.javarosa.test.XFormsElement;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.parse.XFormParserFactory;
import org.javarosa.xform.util.XFormUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra;
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor;
import org.odk.collect.entities.javarosa.finalization.FormEntity;
import org.odk.collect.entities.javarosa.parse.EntityXFormParserFactory;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import kotlin.Pair;

public class EntityFormFinalizationProcessorTest {

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
    public void whenFormDoesNotHaveEntityElement_addsNoEntitiesToExtras() throws Exception {
        Scenario scenario = Scenario.init("Normal form", XFormsElement.html(
                head(
                        title("Normal form"),
                        model(
                                mainInstance(
                                        t("data id=\"normal\"",
                                                t("name")
                                        )
                                ),
                                bind("/data/name").type("string")
                        )
                ),
                body(
                        input("/data/name")
                )
        ));

        EntityFormFinalizationProcessor processor = new EntityFormFinalizationProcessor();
        FormEntryModel model = scenario.getFormEntryController().getModel();
        processor.processForm(model);
        assertThat(model.getExtras().get(EntitiesExtra.class), equalTo(null));
    }

    @Test
    public void whenSaveToIsNotRelevant_itIsNotIncludedInEntity() throws Exception {
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
                                bind("/data/name").type("string").withAttribute("entities", "saveto", "name").relevant("false()"),
                                bind("/data/meta/entity/@id").type("string"),
                                bind("/data/meta/entity/label").type("string").calculate("/data/name"),
                                setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                        )
                ),
                body(
                        input("/data/name")
                )
        ));

        EntityFormFinalizationProcessor processor = new EntityFormFinalizationProcessor();
        FormEntryModel model = scenario.getFormEntryController().getModel();
        processor.processForm(model);

        List<FormEntity> entities = model.getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(emptyList()));
    }

    @Test
    public void createsEntityWithValuesTreatedAsOpaqueStrings() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
                asList(
                        new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
                ),
                head(
                        title("Create entity form"),
                        model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                                mainInstance(
                                        t("data id=\"create-entity-form\"",
                                                t("birthday"),
                                                t("meta",
                                                        t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                                                t("label")
                                                        )
                                                )
                                        )
                                ),
                                bind("/data/birthday").type("date").withAttribute("entities", "saveto", "birthday"),
                                bind("/data/meta/entity/@id").type("string"),
                                bind("/data/meta/entity/label").type("string").calculate("/data/birthday"),
                                setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                        )
                ),
                body(
                        input("/data/birthday")
                )
        ));

        EntityFormFinalizationProcessor processor = new EntityFormFinalizationProcessor();
        FormEntryModel model = scenario.getFormEntryController().getModel();

        scenario.next();
        scenario.getFormEntryController().answerQuestion(new DateData(Date.valueOf("2024-11-15")), true);

        processor.processForm(model);

        List<FormEntity> entities = model.getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties.get(0), equalTo(new Pair<>("birthday", "2024-11-15")));
    }

    @Test
    public void whenSaveToIsInNotRelevantGroup_itIsNotIncludedInEntity() throws Exception {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
                asList(
                        new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
                ),
                head(
                        title("Create entity form"),
                        model(asList(new Pair<>("entities:entities-version", "2024.1.0")),
                                mainInstance(
                                        t("data id=\"create-entity-form\"",
                                                t("group",
                                                        t("name")
                                                ),
                                                t("meta",
                                                        t("entity dataset=\"people\" create=\"1\" id=\"\"",
                                                                t("label")
                                                        )
                                                )
                                        )
                                ),
                                bind("/data/group").relevant("false()"),
                                bind("/data/group/name").type("string").withAttribute("entities", "saveto", "name"),
                                bind("/data/meta/entity/@id").type("string"),
                                bind("/data/meta/entity/label").type("string").calculate("/data/group/name"),
                                setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
                        )
                ),
                body(
                        group("/data/group",
                                input("/data/group/name")
                        )

                )
        ));

        EntityFormFinalizationProcessor processor = new EntityFormFinalizationProcessor();
        FormEntryModel model = scenario.getFormEntryController().getModel();
        processor.processForm(model);

        List<FormEntity> entities = model.getExtras().get(EntitiesExtra.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(emptyList()));
    }
}
