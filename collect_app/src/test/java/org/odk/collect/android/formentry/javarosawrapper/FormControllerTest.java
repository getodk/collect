package org.odk.collect.android.formentry.javarosawrapper;

import com.google.common.io.Files;

import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FormControllerTest {

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsInRepeat_jumpsToRepeatPrompt() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ONE_QUESTION_REPEAT.getBytes());
        final FormEntryModel fem = new FormEntryModel(XFormUtils.getFormFromInputStream(inputStream));
        final FormEntryController formEntryController = new FormEntryController(fem);
        FormController formController = new FormController(Files.createTempDir(), formEntryController, File.createTempFile("instance", ""));

        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsRepeatInGroup_jumpsToRepeatPrompt() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ONE_QUESTION_GROUP_REPEAT.getBytes());
        final FormEntryModel fem = new FormEntryModel(XFormUtils.getFormFromInputStream(inputStream));
        final FormEntryController formEntryController = new FormEntryController(fem);
        FormController formController = new FormController(Files.createTempDir(), formEntryController, File.createTempFile("instance", ""));

        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsInNestedRepeat_jumpsToNestedRepeatPrompt() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ONE_QUESTION_NESTED_REPEAT.getBytes());
        final FormEntryModel fem = new FormEntryModel(XFormUtils.getFormFromInputStream(inputStream));
        final FormEntryController formEntryController = new FormEntryController(fem);
        FormController formController = new FormController(Files.createTempDir(), formEntryController, File.createTempFile("instance", ""));

        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0_0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0_1, "));
    }

    private static final String ONE_QUESTION_REPEAT = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "    <h:head>\n" +
            "        <h:title>One Question Repeat</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"one_question_repeat\">\n" +
            "                    <person>\n" +
            "                        <age/>\n" +
            "                    </person>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <bind nodeset=\"age\" type=\"int\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <group ref=\"/data/person\">\n" +
            "            <label>Person</label>\n" +
            "            <repeat nodeset=\"/data/person\">\n" +
            "                <input ref=\"/data/person/age\">\n" +
            "                    <label>What is their age?</label>\n" +
            "                </input>\n" +
            "            </repeat>\n" +
            "        </group>\n" +
            "    </h:body>\n" +
            "</h:html>\n";

    private static final String ONE_QUESTION_GROUP_REPEAT = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "    <h:head>\n" +
            "        <h:title>One Question Repeat</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"one_question_repeat\">\n" +
            "                    <person>\n" +
            "                        <questions>\n" +
            "                            <age/>\n" +
            "                        </questions>\n" +
            "                    </person>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <bind nodeset=\"age\" type=\"int\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <group ref=\"/data/person\">\n" +
            "            <label>Person</label>\n" +
            "            <repeat nodeset=\"/data/person\">\n" +
            "                <group ref=\"/data/person/questions\">\n" +
            "                    <input ref=\"/data/person/questions/age\">\n" +
            "                        <label>What is their age?</label>\n" +
            "                    </input>\n" +
            "                </group>\n" +
            "            </repeat>\n" +
            "        </group>\n" +
            "    </h:body>\n" +
            "</h:html>\n";

    private static final String ONE_QUESTION_NESTED_REPEAT = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "    <h:head>\n" +
            "        <h:title>One Question Repeat</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"one_question_repeat\">\n" +
            "                    <person>\n" +
            "                        <questions>\n" +
            "                            <age/>\n" +
            "                        </questions>\n" +
            "                    </person>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <bind nodeset=\"age\" type=\"int\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <group ref=\"/data/person\">\n" +
            "            <label>Person</label>\n" +
            "            <repeat nodeset=\"/data/person\">\n" +
            "                <repeat nodeset=\"/data/person/questions\">\n" +
            "                    <input ref=\"/data/person/questions/age\">\n" +
            "                        <label>What is their age?</label>\n" +
            "                    </input>\n" +
            "                </repeat>\n" +
            "            </repeat>\n" +
            "        </group>\n" +
            "    </h:body>\n" +
            "</h:html>\n";
}