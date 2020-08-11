package org.odk.collect.android.utilities;

public interface QuestionMediaManager {
    void deleteOriginalFile(String questionIndex, String fileName);

    void replaceRecentFile(String questionIndex, String fileName);
}
