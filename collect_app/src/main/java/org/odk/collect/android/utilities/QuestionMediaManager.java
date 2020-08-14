package org.odk.collect.android.utilities;

public interface QuestionMediaManager {
    void markOriginalFileOrDelete(String questionIndex, String fileName);

    void replaceRecentFileForQuestion(String questionIndex, String fileName);
}
