package org.odk.collect.android.javarosawrapper;

import com.google.common.io.Files;

import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class FormControllerUtils {
    private FormControllerUtils() {

    }

    // Replicates the essential functionality for loading a form. See FormLoaderTask.
    @NotNull
    public static FormController createFormController(String xform) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xform.getBytes());
        final FormEntryModel fem = new FormEntryModel(XFormUtils.getFormFromInputStream(inputStream));
        final FormEntryController formEntryController = new FormEntryController(fem);

        // ensure all secondary instances get correct names. See FormLoaderTask.initializeForm
        formEntryController.getModel().getForm().initialize(true, null);

        return new FormController(Files.createTempDir(), formEntryController, File.createTempFile("instance", ""));
    }
}
