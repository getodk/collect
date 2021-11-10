package org.odk.collect.android.utilities;

import androidx.annotation.NonNull;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.forms.Form;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FormUtils {

    private FormUtils() {

    }

    @NonNull
    public static List<File> getMediaFiles(@NonNull Form form) {
        String formMediaPath = form.getFormMediaPath();
        return formMediaPath == null
                ? new ArrayList<>()
                : FileUtils.listFiles(new File(formMediaPath));
    }

    /**
     * Configures the given reference manager to resolve jr:// URIs to a folder in the root ODK forms
     * directory with name matching the name of the directory represented by {@code formMediaDir}.
     * <p>
     * E.g. if /foo/bar/baz is passed in as {@code formMediaDir}, jr:// URIs will be resolved to
     * projectRoot/forms/baz.
     */
    public static void setupReferenceManagerForForm(ReferenceManager referenceManager, File formMediaDir) {
        referenceManager.reset();

        // Always build URIs against the project root, regardless of the absolute path of formMediaDir
        referenceManager.addReferenceFactory(new FileReferenceFactory(new StoragePathProvider().getProjectRootDirPath()));

        addSessionRootTranslators(referenceManager,
                buildSessionRootTranslators(formMediaDir.getName(), enumerateHostStrings()));
    }

    public static String[] enumerateHostStrings() {
        return new String[]{"images", "image", "audio", "video", "file-csv", "file"};
    }

    public static List<RootTranslator> buildSessionRootTranslators(String formMediaDir, String[] hostStrings) {
        List<RootTranslator> rootTranslators = new ArrayList<>();
        // Set jr://... to point to <projectRoot>/forms/formBasename-media/
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
