package org.odk.collect.android.javarosawrapper;

import com.google.common.io.Files;

import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class FormControllerTest {

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsInRepeat_jumpsToRepeatPrompt() throws Exception {
        FormController formController = createFormController(ONE_QUESTION_REPEAT);

        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsRepeatInGroup_jumpsToRepeatPrompt() throws Exception {
        FormController formController = createFormController(ONE_QUESTION_GROUP_REPEAT);

        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsOuterOfNestedRepeat_jumpsToOuterRepeatPrompt() throws Exception {
        FormController formController = createFormController(ONE_QUESTION_NESTED_REPEAT);

        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenIndexIsInNestedRepeat_jumpsToNestedRepeatPrompt() throws Exception {
        FormController formController = createFormController(ONE_QUESTION_NESTED_REPEAT);

        formController.stepToNextScreenEvent();
        formController.stepToNextScreenEvent();
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 1_0, 0, "));

        formController.jumpToNewRepeatPrompt();

        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertThat(formController.getFormIndex().toString(), equalTo("0_0, 1_1, "));
    }

    @Test
    public void whenInstanceFileAndAuditConfigNull_getAuditEventLogger_isNotNull() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ONE_QUESTION_NESTED_REPEAT.getBytes());
        final FormEntryModel fem = new FormEntryModel(XFormUtils.getFormFromInputStream(inputStream));
        final FormEntryController formEntryController = new FormEntryController(fem);
        FormController formController = new JavaRosaFormController(Files.createTempDir(), formEntryController, null);
        assertThat(formController.getSubmissionMetadata().auditConfig, is(nullValue()));
        assertThat(formController.getInstanceFile(), is(nullValue()));

        assertThat(formController.getAuditEventLogger(), notNullValue());
    }


    //region indexIsInFieldList
    @Test
    public void questionInGroupWithoutFieldListAppearance_isNotInFieldList() throws IOException, XFormParser.ParseException {
        FormController formController = createFormController(GROUP);

        formController.stepToNextEvent(true);
        formController.stepToNextEvent(true);
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.indexIsInFieldList(), is(false));
    }

    @Test
    public void questionInGroupWithoutFieldListAppearance_isInFieldList() throws IOException, XFormParser.ParseException {
        FormController formController = createFormController(FIELD_LIST);

        formController.stepToNextEvent(true);
        formController.stepToNextEvent(true);
        assertThat(formController.getEvent(), equalTo(FormEntryController.EVENT_QUESTION));
        assertThat(formController.indexIsInFieldList(), is(true));
    }
    //endregion

    @NotNull
    private FormController createFormController(String xform) throws IOException, XFormParser.ParseException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xform.getBytes());
        final FormEntryModel fem = new FormEntryModel(XFormUtils.getFormFromInputStream(inputStream));
        final FormEntryController formEntryController = new FormEntryController(fem);
        return new JavaRosaFormController(Files.createTempDir(), formEntryController, File.createTempFile("instance", ""));
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
            "                        <age/>\n" +
            "                        <tattoo>\n" +
            "                            <description />\n" +
            "                        </tattoo>\n" +
            "                    </person>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <bind nodeset=\"age\" type=\"int\"/>\n" +
            "            <bind nodeset=\"description\" type=\"string\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <group ref=\"/data/person\">\n" +
            "            <label>Person</label>\n" +
            "            <repeat nodeset=\"/data/person\">\n" +
            "                <input ref=\"/data/person/age\">\n" +
            "                    <label>What is their age?</label>\n" +
            "                </input>\n" +
            "                <repeat nodeset=\"/data/person/tattoo\">\n" +
            "                    <input ref=\"/data/person/tattoo/description\">\n" +
            "                        <label>What is the tattoo of?</label>\n" +
            "                    </input>\n" +
            "                </repeat>\n" +
            "            </repeat>\n" +
            "        </group>\n" +
            "    </h:body>\n" +
            "</h:html>\n";

    private static final String GROUP = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\">\n" +
            "    <h:head>\n" +
            "        <h:title>Group</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"group\">\n" +
            "                    <group>\n" +
            "                        <question/>\n" +
            "                    </group>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <bind nodeset=\"/data/group/question\" type=\"int\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <group ref=\"/data/group\">\n" +
            "          <input ref=\"/data/group/question\">\n" +
            "            <label>Question</label>\n" +
            "          </input>\n" +
            "        </group>\n" +
            "    </h:body>\n" +
            "</h:html>\n";

    private static final String FIELD_LIST = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\">\n" +
            "    <h:head>\n" +
            "        <h:title>Field list</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"field-list\">\n" +
            "                    <group>\n" +
            "                        <question/>\n" +
            "                    </group>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <bind nodeset=\"/data/group/question\" type=\"int\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <group ref=\"/data/group\" appearance=\"fake fieLd-list fake\">\n" +
            "          <input ref=\"/data/group/question\">\n" +
            "            <label>Question</label>\n" +
            "          </input>\n" +
            "        </group>\n" +
            "    </h:body>\n" +
            "</h:html>\n";
}
