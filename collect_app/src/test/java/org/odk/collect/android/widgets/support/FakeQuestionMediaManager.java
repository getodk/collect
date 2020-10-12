package org.odk.collect.android.widgets.support;

import org.odk.collect.android.utilities.QuestionMediaManager;

import java.util.HashMap;
import java.util.Map;

public class FakeQuestionMediaManager implements QuestionMediaManager {
    public Map<String, String> originalFiles = new HashMap<>();
    public Map<String, String> recentFiles = new HashMap<>();

    @Override
    public void deleteAnswerFile(String questionIndex, String fileName) {
        originalFiles.put(questionIndex, fileName);
    }

    @Override
    public void replaceAnswerFile(String questionIndex, String fileName) {
        recentFiles.put(questionIndex, fileName);
    }
}
