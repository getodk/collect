package org.odk.collect.android.utilities;

import androidx.annotation.Nullable;

import java.io.File;

public interface QuestionMediaManager {

    @Nullable
    File getAnswerFile(String fileName);

    void deleteAnswerFile(String questionIndex, String fileName);

    void replaceAnswerFile(String questionIndex, String fileName);
}
