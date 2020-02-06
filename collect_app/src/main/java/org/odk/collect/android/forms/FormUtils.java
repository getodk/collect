package org.odk.collect.android.forms;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormUtils {

    private FormUtils() {
        
    }

    public static void setupReferenceManagerForForm(ReferenceManager referenceManager, File formMediaDir) {

        // Remove previous forms
        referenceManager.clearSession();

        // This should get moved to the Application Class
        if (referenceManager.getFactories().length == 0) {
            // this is /sdcard/odk
            referenceManager.addReferenceFactory(new FileReferenceFactory(new StoragePathProvider().getDirPath(StorageSubdirectory.ODK)));
        }

        addSessionRootTranslators(referenceManager,
                buildSessionRootTranslators(formMediaDir.getName(), enumerateHostStrings()));
    }

    public static String[] enumerateHostStrings() {
        return new String[] {"images", "image", "audio", "video", "file-csv", "file"};
    }

    public static List<RootTranslator> buildSessionRootTranslators(String formMediaDir, String[] hostStrings) {
        List<RootTranslator> rootTranslators = new ArrayList<>();
        // Set jr://... to point to /sdcard/odk/forms/formBasename-media/
        final String translatedPrefix = String.format("jr://file/forms/" + formMediaDir + "/");
        for (String t : hostStrings) {
            rootTranslators.add(new RootTranslator(String.format("jr://%s/", t), translatedPrefix));
        }
        return rootTranslators;
    }

    public static void addSessionRootTranslators(ReferenceManager referenceManager, List<RootTranslator> rootTranslators) {
        for (RootTranslator rootTranslator : rootTranslators) {
            referenceManager.addSessionRootTranslator(rootTranslator);
        }
    }
}
