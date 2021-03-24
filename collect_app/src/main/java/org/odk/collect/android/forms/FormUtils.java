package org.odk.collect.android.forms;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormUtils {

    private FormUtils() {
        
    }

    public static List<File> getMediaFiles(Form form) {
        FileUtil fileUtil = new FileUtil();

        String formMediaPath = form.getFormMediaPath();
        return formMediaPath == null
                ? new ArrayList<>()
                : fileUtil.listFiles(fileUtil.getFileAtPath(formMediaPath));
    }

    /**
     * Configures the given reference manager to resolve jr:// URIs to a folder in the root ODK forms
     * directory with name matching the name of the directory represented by {@code formMediaDir}.
     *
     * E.g. if /foo/bar/baz is passed in as {@code formMediaDir}, jr:// URIs will be resolved to
     * /odk/root/forms/baz.
     */
    public static void setupReferenceManagerForForm(ReferenceManager referenceManager, File formMediaDir) {
        // Clear mappings to the media dir for the previous form that was configured
        referenceManager.clearSession();

        // This should get moved to the Application Class
        if (referenceManager.getFactories().length == 0) {
            // Always build URIs against the ODK root, regardless of the absolute path of formMediaDir
            referenceManager.addReferenceFactory(new FileReferenceFactory(new StoragePathProvider().getOdkRootDirPath()));
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
