package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.ExVideoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.FileRequester;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.odk.collect.androidshared.ui.ToastUtils;

import java.io.File;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class ExVideoWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver {
    ExVideoWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final FileRequester fileRequester;

    File answerFile;

    public ExVideoWidget(Context context, QuestionDetails questionDetails, QuestionMediaManager questionMediaManager,
                         WaitingForDataRegistry waitingForDataRegistry, FileRequester fileRequester, Dependencies dependencies) {
        super(context, dependencies, questionDetails);

        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.fileRequester = fileRequester;

        render();
    }

    @Override
    protected View onCreateWidgetView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        setupAnswerFile(prompt.getAnswerText());

        binding = ExVideoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        binding.captureVideoButton.setVisibility(questionDetails.isReadOnly() ? GONE : VISIBLE);
        binding.captureVideoButton.setOnClickListener(view -> launchExternalApp());
        binding.playVideoButton.setOnClickListener(view -> mediaUtils.openFile(getContext(), answerFile, "video/*"));
        binding.playVideoButton.setEnabled(answerFile != null);

        return binding.getRoot();
    }

    @Override
    public void deleteFile() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(), answerFile.getAbsolutePath());
        answerFile = null;
    }

    @Override
    public void clearAnswer() {
        deleteFile();
        binding.playVideoButton.setEnabled(false);
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        return answerFile != null ? new StringData(answerFile.getName()) : null;
    }

    @Override
    public void setData(Object object) {
        if (answerFile != null) {
            clearAnswer();
        }

        if (object instanceof File && mediaUtils.isVideoFile((File) object)) {
            answerFile = (File) object;
            if (answerFile.exists()) {
                questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), answerFile.getAbsolutePath());
                binding.playVideoButton.setEnabled(true);
                widgetValueChanged();
            } else {
                Timber.e(new Error("Inserting Video file FAILED"));
            }
        } else if (object != null) {
            if (object instanceof File) {
                ToastUtils.showLongToast(org.odk.collect.strings.R.string.invalid_file_type);
                mediaUtils.deleteMediaFile(((File) object).getAbsolutePath());
                Timber.e(new Error("ExVideoWidget's setBinaryData must receive a video file but received: " + FileUtils.getMimeType((File) object)));
            } else {
                Timber.e(new Error("ExVideoWidget's setBinaryData must receive a video file but received: " + object.getClass()));
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.captureVideoButton.setOnLongClickListener(l);
        binding.playVideoButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.captureVideoButton.cancelLongPress();
        binding.playVideoButton.cancelLongPress();
    }

    private void launchExternalApp() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        fileRequester.launch((Activity) getContext(), ApplicationConstants.RequestCodes.EX_VIDEO_CHOOSER, getFormEntryPrompt());
    }

    private void setupAnswerFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            answerFile = questionMediaManager.getAnswerFile(fileName);
        }
    }
}
