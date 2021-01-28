package org.odk.collect.android.formentry.audit;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.junit.Test;
import org.odk.collect.android.javarosawrapper.FormController;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.android.javarosawrapper.FormControllerUtils.createFormController;

public class AuditUtilsIntegrationTest {
    @Test
    public void exitingScreenWithStaticSelect_logsNewUnderlyingValue() throws Exception {
        FormController formController = createFormController(STATIC_SELECT);
        formController.stepToNextScreenEvent(); // go to first question

        AuditConfig config = formController.getSubmissionMetadata().auditConfig;
        TestWriter testWriter = new TestWriter();
        AuditEventLogger eventLogger = new AuditEventLogger(config, testWriter, formController);

        AuditUtils.logCurrentScreen(formController, eventLogger, 1L);

        List<SelectChoice> level1Choices = formController.getQuestionPrompts()[0].getSelectChoices();
        formController.answerQuestion(formController.getFormIndex(), new SelectOneData(new Selection(level1Choices.get(1))));

        AuditUtils.logCurrentScreen(formController, eventLogger, 1L);

        eventLogger.flush();
        assertThat(testWriter.auditEvents.size(), is(1));
        AuditEvent event = testWriter.auditEvents.get(0);
        assertThat(event.getOldValue(), is(""));
        assertThat(event.getNewValue(), is("b"));
    }

    @Test
    public void exitingScreenWithDynamicSelect_logsNewUnderlyingValue() throws Exception {
        FormController formController = createFormController(DYNAMIC_SELECT);

        AuditConfig config = formController.getSubmissionMetadata().auditConfig;
        TestWriter testWriter = new TestWriter();
        AuditEventLogger eventLogger = new AuditEventLogger(config, testWriter, formController);

        formController.stepToNextScreenEvent(); // go to dynamic select question

        AuditUtils.logCurrentScreen(formController, eventLogger, 1L);

        List<SelectChoice> level2Choices = formController.getQuestionPrompts()[0].getSelectChoices();
        formController.answerQuestion(formController.getFormIndex(), new SelectOneData(new Selection(level2Choices.get(1))));

        AuditUtils.logCurrentScreen(formController, eventLogger, 1L);

        eventLogger.flush();
        assertThat(testWriter.auditEvents.size(), is(1));
        AuditEvent event = testWriter.auditEvents.get(0);
        assertThat(event.getOldValue(), is(""));
        assertThat(event.getNewValue(), is("bb"));
    }

    private static final String STATIC_SELECT = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:odk=\"http://www.opendatakit.org/xforms\">\n" +
            "   <h:head>\n" +
            "      <h:title>Static Select</h:title>\n" +
            "      <model>\n" +
            "         <instance>\n" +
            "            <data id=\"static_select\">\n" +
            "               <level1/>\n" +
            "               <meta>\n" +
            "                 <audit/>\n" +
            "               </meta>\n" +
            "            </data>\n" +
            "          </instance>\n" +
            "          <bind nodeset=\"/data/level1\" type=\"string\"/>\n" +
            "          <bind nodeset=\"/data/meta/audit\" type=\"binary\" odk:track-changes=\"true\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <select1 ref=\"/data/level1\">\n" +
            "            <label>Level1</label>\n" +
            "            <item>\n" +
            "                <label>A</label>\n" +
            "                <value>a</value>\n" +
            "            </item>\n" +
            "            <item>\n" +
            "                <label>B</label>\n" +
            "                <value>b</value>\n" +
            "            </item>\n" +
            "            <item>\n" +
            "                <label>C</label>\n" +
            "                <value>c</value>\n" +
            "            </item>\n" +
            "        </select1>\n" +
            "    </h:body>\n" +
            "</h:html>";

    private static final String DYNAMIC_SELECT = "<?xml version=\"1.0\"?>\n" +
            "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:odk=\"http://www.opendatakit.org/xforms\">\n" +
            "   <h:head>\n" +
            "      <h:title>Dynamic select</h:title>\n" +
            "      <model odk:xforms-version=\"1.0.0\">\n" +
            "         <instance>\n" +
            "            <data id=\"dynamic_select\">\n" +
            "               <level1>b</level1>\n" +
            "               <level2/>\n" +
            "               <meta>\n" +
            "                 <audit/>\n" +
            "               </meta>\n" +
            "             </data>\n" +
            "        </instance>\n" +
            "        <instance id=\"level2\">\n" +
            "            <root>\n" +
            "                <item>\n" +
            "                    <label>AA</label>\n" +
            "                    <level1>a</level1>\n" +
            "                    <name>aa</name>\n" +
            "                </item>\n" +
            "                <item>\n" +
            "                    <label>AB</label>\n" +
            "                    <level1>a</level1>\n" +
            "                    <name>ab</name>\n" +
            "                </item>\n" +
            "                <item>\n" +
            "                    <label>BA</label>\n" +
            "                    <level1>b</level1>\n" +
            "                    <name>ba</name>\n" +
            "                </item>\n" +
            "                <item>\n" +
            "                    <label>BB</label>\n" +
            "                    <level1>b</level1>\n" +
            "                    <name>bb</name>\n" +
            "                </item>\n" +
            "                <item>\n" +
            "                    <label>CA</label>\n" +
            "                    <level1>c</level1>\n" +
            "                    <name>ca</name>\n" +
            "                </item>\n" +
            "                <item>\n" +
            "                    <label>CB</label>\n" +
            "                    <level1>c</level1>\n" +
            "                    <name>cb</name>\n" +
            "                </item>\n" +
            "            </root>\n" +
            "         </instance>\n" +
            "         <bind nodeset=\"/data/level1\" type=\"string\"/>\n" +
            "         <bind nodeset=\"/data/level2\" type=\"string\"/>\n" +
            "         <bind nodeset=\"/data/meta/audit\" type=\"binary\" odk:track-changes=\"true\"/>\n" +
            "      </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <select1 ref=\"/data/level2\">\n" +
            "            <label>Level2</label>\n" +
            "            <itemset nodeset=\"instance('level2')/root/item[level1 =  /data/level1 ]\">\n" +
            "                <value ref=\"name\"/>\n" +
            "                <label ref=\"label\"/>\n" +
            "            </itemset>\n" +
            "        </select1>\n" +
            "    </h:body>\n" +
            "</h:html>";
}
