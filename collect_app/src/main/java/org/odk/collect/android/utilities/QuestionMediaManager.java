package org.odk.collect.android.utilities;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import org.odk.collect.android.formentry.saving.FormSaveViewModel;

import java.io.File;

/**
 * Provides an interface for widgets to manage answer files. This lets them delete answer files
 * when an answer is cleared, replace answer files when an answer is changed and also access answer
 * files.
 */
public interface QuestionMediaManager {

    LiveData<FormSaveViewModel.CreateAnswerFileResult> createAnswerFile(File file);

    @Nullable
    File getAnswerFile(String fileName);

    void deleteAnswerFile(String questionIndex, String fileName);

    void replaceAnswerFile(String questionIndex, String fileName);

    class CreateAnswerFileResult {

        private final String fileName;

        public CreateAnswerFileResult(@Nullable String fileName) {
            this.fileName = fileName;
        }

        @Nullable
        public String getOrNull() {
            return fileName;
        }

        public boolean isSuccess() {
            return fileName != null;
        }
    }
}
