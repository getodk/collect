package org.odk.collect.android.utilities;

public interface QuestionMediaManager {

    void deleteAnswerFile(String questionIndex, String fileName);

    void replaceAnswerFile(String questionIndex, String fileName);
}
