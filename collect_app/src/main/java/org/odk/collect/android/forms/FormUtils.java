package org.odk.collect.android.forms;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FileReferenceFactory;

import java.io.File;

public class FormUtils {

    private FormUtils() {
        
    }

    public static void setupReferenceManagerForForm(ReferenceManager referenceManager, File formMediaDir) {

        // Remove previous forms
        referenceManager.clearSession();

        // This should get moved to the Application Class
        if (referenceManager.getFactories().length == 0) {
            // this is /sdcard/odk
            referenceManager.addReferenceFactory(new FileReferenceFactory(Collect.ODK_ROOT));
        }

        addSessionRootTranslators(formMediaDir.getName(), referenceManager,
                "images", "image", "audio", "video", "file");
    }

    private static void addSessionRootTranslators(String formMediaDir, ReferenceManager referenceManager, String... hostStrings) {
        // Set jr://... to point to /sdcard/odk/forms/formBasename-media/
        final String translatedPrefix = String.format("jr://file/forms/" + formMediaDir + "/");
        for (String t : hostStrings) {
            referenceManager.addSessionRootTranslator(new RootTranslator(String.format("jr://%s/", t), translatedPrefix));
        }
    }
}
