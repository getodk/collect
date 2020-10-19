package org.odk.collect.android.widgets.support;

import com.google.common.io.Files;

import org.odk.collect.android.utilities.QuestionMediaManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FakeQuestionMediaManager implements QuestionMediaManager {

    public final Map<String, String> originalFiles = new HashMap<>();
    public final Map<String, String> recentFiles = new HashMap<>();
    private final File tempDir = Files.createTempDir();

    @Override
    public File getAnswerFile(String fileName) {
        return new File(tempDir, fileName);
    }

    @Override
    public void deleteAnswerFile(String questionIndex, String fileName) {
        originalFiles.put(questionIndex, fileName);
    }

    @Override
    public void replaceAnswerFile(String questionIndex, String fileName) {
        recentFiles.put(questionIndex, fileName);
    }

    public File getDir() {
        return tempDir;
    }
}
