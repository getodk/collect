package org.odk.collect.android.formmanagement;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME;
import static org.odk.collect.android.utilities.FileUtils.STUB_XML;
import static org.odk.collect.android.utilities.FileUtils.write;

public class FormMetadataParser {

    private final File tempDir;
    private final ReferenceManager referenceManager;

    public FormMetadataParser(File tempDir, ReferenceManager referenceManager) {
        this.tempDir = tempDir;
        this.referenceManager = referenceManager;
    }

    public Map<String, String> parse(File file) {
        // Add a stub last-saved instance to the tmp media directory so it will be resolved
        // when parsing a form definition with last-saved reference
        File tmpLastSaved = new File(tempDir, LAST_SAVED_FILENAME);
        write(tmpLastSaved, STUB_XML.getBytes(Charset.forName("UTF-8")));
        referenceManager.reset();
        referenceManager.addReferenceFactory(new FileReferenceFactory(tempDir.getAbsolutePath()));
        referenceManager.addSessionRootTranslator(new RootTranslator("jr://file-csv/", "jr://file/"));

        HashMap<String, String> metadata = FileUtils.getMetadataFromFormDefinition(file);
        referenceManager.reset();
        tmpLastSaved.delete();

        return metadata;
    }
}
