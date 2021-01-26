/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.ArbitraryFileWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class ArbitraryFileWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver {
    ArbitraryFileWidgetAnswerBinding binding;

    @NonNull
    private final MediaUtils mediaUtils;

    private final QuestionMediaManager questionMediaManager;
    private final WaitingForDataRegistry waitingForDataRegistry;

    private String binaryName;

    public ArbitraryFileWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        this(context, prompt, new MediaUtils(), questionMediaManager, waitingForDataRegistry);
    }

    ArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull MediaUtils mediaUtils,
                        QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.mediaUtils = mediaUtils;
        this.questionMediaManager = questionMediaManager;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binaryName = prompt.getAnswerText();

        if (prompt.isReadOnly()) {
            binding.arbitraryFileButton.setVisibility(GONE);
        } else {
            binding.arbitraryFileButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.arbitraryFileButton.setOnClickListener(v -> onButtonClick());
            binding.arbitraryFileAnswerText.setOnClickListener(v -> mediaUtils.openFile(getContext(), new File(getInstanceFolder() + File.separator + binaryName), null));
        }

        if (binaryName != null && !binaryName.isEmpty()) {
            binding.arbitraryFileAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.arbitraryFileAnswerText.setText(binaryName);
            binding.arbitraryFileAnswerText.setVisibility(VISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void deleteFile() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
    }

    @Override
    public IAnswerData getAnswer() {
        return binaryName != null ? new StringData(binaryName) : null;
    }

    @Override
    public void clearAnswer() {
        binding.arbitraryFileAnswerText.setVisibility(GONE);
        deleteFile();

        widgetValueChanged();
    }

    private void onButtonClick() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        mediaUtils.pickFile((Activity) getContext(), "*/*", ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER);
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
                binding.arbitraryFileAnswerText.setText(binaryName);
                binding.arbitraryFileAnswerText.setVisibility(VISIBLE);
                widgetValueChanged();
            } else {
                Timber.e("Inserting Arbitrary file FAILED");
            }
        } else {
            Timber.e("FileWidget's setBinaryData must receive a File or Uri object.");
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.arbitraryFileButton.setOnLongClickListener(l);
        binding.arbitraryFileAnswerText.setOnLongClickListener(l);
    }
}
