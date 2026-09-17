package org.odk.collect.android.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.io.Files;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.JavaRosaFormController;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

@RunWith(AndroidJUnit4.class)
public class SaveFormIndexTaskTest {

    private static final String INSTANCE_NAME = "test.xml";

    @Before
    public void setup() {
        CollectHelpers.setupDemoProject(); // Allows using StoragePathProvider
    }

    @Test
    public void loadFormIndexFromFile_returnsTheIndexSavedByAPreviousRun() throws Exception {
        FormController formController = createFormController();
        formController.stepToNextScreenEvent();
        formController.stepToNextScreenEvent();
        FormIndex originalFormIndex = formController.getFormIndex();

        SaveFormIndexTask.exportFormIndexToFile(formController.getXPath(originalFormIndex), indexFile());
        FormIndex readFormIndex = SaveFormIndexTask.loadFormIndexFromFile(createFormController());

        assertThat(readFormIndex, equalTo(originalFormIndex));
    }

    /**
     * The file must stay free of class and member names, or an obfuscated release cannot read back
     * what an earlier one wrote.
     */
    @Test
    public void exportFormIndexToFile_storesNoClassOrMemberNames() throws Exception {
        FormController formController = createFormController();
        formController.stepToNextScreenEvent();
        formController.stepToNextScreenEvent();

        SaveFormIndexTask.exportFormIndexToFile(formController.getXPath(formController.getFormIndex()), indexFile());

        String stored = new String(FileUtils.read(indexFile()), StandardCharsets.UTF_8);
        assertThat(stored, equalTo("question./data/second[1]"));
    }

    private File indexFile() {
        return SaveFormToDisk.getFormIndexFile(INSTANCE_NAME);
    }

    private FormController createFormController() throws Exception {
        File instanceFile = new File(Files.createTempDir(), INSTANCE_NAME);
        FormEntryModel model = new FormEntryModel(XFormUtils.getFormFromInputStream(new ByteArrayInputStream(TWO_QUESTIONS.getBytes())));
        FormEntryController formEntryController = new FormEntryController(model);
        formEntryController.getModel().getForm().initialize(true, null);
        return new JavaRosaFormController(Files.createTempDir(), formEntryController, instanceFile);
    }

    private static final String TWO_QUESTIONS = html(
            head(
                    title("Two Questions"),
                    model(
                            mainInstance(t("data id=\"two_questions\"",
                                    t("first"),
                                    t("second")
                            )),
                            bind("/data/first").type("string"),
                            bind("/data/second").type("string")
                    )
            ),
            body(
                    input("/data/first"),
                    input("/data/second")
            )
    ).asXml();
}
