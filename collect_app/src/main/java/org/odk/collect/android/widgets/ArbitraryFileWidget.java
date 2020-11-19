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
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import android.util.TypedValue;
import android.view.View;
import android.webkit.MimeTypeMap;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.ArbitraryFileWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class ArbitraryFileWidget extends QuestionWidget implements WidgetDataReceiver {
    ArbitraryFileWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final ActivityAvailability activityAvailability;
    private final ContentUriProvider contentUriProvider;

    private String binaryName;

    ArbitraryFileWidget(Context context, QuestionDetails questionDetails, QuestionMediaManager questionMediaManager,
                        WaitingForDataRegistry waitingForDataRegistry, ActivityAvailability activityAvailability,
                        ContentUriProvider contentUriProvider) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.activityAvailability = activityAvailability;
        this.contentUriProvider = contentUriProvider;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.chooseFileButton.setVisibility(View.GONE);
        } else {
            binding.chooseFileButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.chooseFileButton.setOnClickListener(view -> {
                waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
                performFileSearch();
            });
        }
        binding.answerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        binaryName = prompt.getAnswerText();
        if (binaryName == null) {
            binding.answerLayout.setVisibility(View.GONE);
        } else {
            binding.answerTextView.setText(binaryName);
        }
        binding.answerLayout.setOnClickListener(view -> openFile());

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        return binaryName != null ? new StringData(binaryName) : null;
    }

    @Override
    public void clearAnswer() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                getFile().getAbsolutePath());
        binaryName = null;
        binding.answerLayout.setVisibility(GONE);

        widgetValueChanged();
    }

    @Override
    public void setData(Object object) {
        File newFile = (File) object;
        if (newFile.exists()) {
            if (binaryName != null && !binaryName.equals(newFile.getName())) {
                questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                        getFile().getAbsolutePath());
            }
            binaryName = newFile.getName();
            Timber.i("Setting current answer to %s", newFile.getName());

            binding.answerTextView.setText(binaryName);
            binding.answerLayout.setVisibility(VISIBLE);
            widgetValueChanged();
        } else {
            Timber.e("Inserting Arbitrary file FAILED");
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.chooseFileButton.setOnLongClickListener(l);
        binding.answerLayout.setOnLongClickListener(l);
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // all file types
        ((Activity) getContext()).startActivityForResult(intent, ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER);
    }

    private String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;
    }

    private void openFile() {
        Uri fileUri = Uri.fromFile(questionMediaManager.getAnswerFile(binaryName));

        Uri contentUri = contentUriProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                questionMediaManager.getAnswerFile(binaryName));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, getMimeType(
                MediaUtils.getPathFromUri(getContext(), fileUri, MediaStore.Files.FileColumns.DATA)));
        FileUtils.grantFileReadPermissions(intent, contentUri, getContext());

        if (activityAvailability.isActivityAvailable(intent)) {
            getContext().startActivity(intent);
        } else {
            String message = getContext().getString(R.string.activity_not_found, getContext().getString(R.string.open_file));
            ToastUtils.showLongToast(message);
            Timber.w(message);
        }
    }

    /**
     * Returns the file added to the widget for the current instance
     */
    private File getFile() {
        return questionMediaManager.getAnswerFile(binaryName);
    }
}
