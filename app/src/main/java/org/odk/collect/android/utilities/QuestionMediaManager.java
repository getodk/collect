package org.odk.collect.android.utilities;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import org.odk.collect.utilities.Result;

import java.io.File;

/**
 * Provides an interface for widgets to manage answer files. This lets them delete answer files
 * when an answer is cleared, replace answer files when an answer is changed and also access answer
 * files.
 */
public interface QuestionMediaManager {

    LiveData<Result<File>> createAnswerFile(File file);

    @Nullable
    File getAnswerFile(String fileName);

    void deleteAnswerFile(String questionIndex, String fileName);

    void replaceAnswerFile(String questionIndex, String fileName);
}
