package org.odk.collect.entities.javarosa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import org.javarosa.form.api.FormEntryModel;
import org.javarosa.test.Scenario;
import org.javarosa.test.XFormsElement;
import org.javarosa.xform.parse.XFormParserFactory;
import org.javarosa.xform.util.XFormUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra;
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor;
import org.odk.collect.entities.javarosa.parse.EntityXFormParserFactory;

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
}
