package org.odk.collect.android.widgets.support;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.io.Files;

import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.utilities.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeQuestionMediaManager implements QuestionMediaManager {

    public final List<File> answerFiles = new ArrayList<>();
    public final Map<String, String> originalFiles = new HashMap<>();
    public final Map<String, String> recentFiles = new HashMap<>();
    private final File tempDir = Files.createTempDir();

    @Override
    public LiveData<Result<File>> createAnswerFile(File file) {
        File answerFile = addAnswerFile(file);
        return new MutableLiveData<>(new Result<>(answerFile));
    }

    @Override
    public File getAnswerFile(String fileName) {
        File existing = answerFiles.stream().filter(f -> f.getName().equals(fileName)).findFirst().orElse(null);

        if (existing != null) {
            return existing;
        } else {
            return new File(tempDir, fileName);
        }
    }

    @Override
    public void deleteAnswerFile(String questionIndex, String fileName) {
        originalFiles.put(questionIndex, fileName);
    }

    @Override
    public void replaceAnswerFile(String questionIndex, String fileName) {
        recentFiles.put(questionIndex, fileName);
    }

    public File addAnswerFile(File file) {
        File answerFile = new File(tempDir, file.getName());
        try {
            Files.copy(file, answerFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        answerFiles.add(answerFile);
        return answerFile;
    }

    public File getDir() {
        return tempDir;
    }
}
