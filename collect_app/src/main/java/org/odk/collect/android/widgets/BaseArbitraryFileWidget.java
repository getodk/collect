package org.odk.collect.android.widgets;

import android.content.Context;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

public abstract class BaseArbitraryFileWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver  {
    @NonNull
    protected final MediaUtils mediaUtils;

    private final QuestionMediaManager questionMediaManager;
    protected final WaitingForDataRegistry waitingForDataRegistry;

    protected String binaryName;

    public BaseArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull MediaUtils mediaUtils,
                                   QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.mediaUtils = mediaUtils;
        this.questionMediaManager = questionMediaManager;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public IAnswerData getAnswer() {
        return binaryName != null ? new StringData(binaryName) : null;
    }

    @Override
    public void deleteFile() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
    }

    @Override
    public void setData(Object object) {
        if (binaryName != null) {
            deleteFile();
        }

        if (object instanceof File) {
            File newFile = (File) object;
            if (newFile.exists()) {
                questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), newFile.getAbsolutePath());
                binaryName = newFile.getName();
                showAnswerText();
                widgetValueChanged();
            } else {
                Timber.e("Inserting Arbitrary file FAILED");
            }
        } else {
            Timber.e("FileWidget's setBinaryData must receive a File or Uri object.");
        }
    }

    protected abstract void showAnswerText();
}