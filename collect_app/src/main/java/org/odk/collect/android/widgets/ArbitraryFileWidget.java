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
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createAnswerTextView;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;

@SuppressLint("ViewConstructor")
public class ArbitraryFileWidget extends QuestionWidget implements FileWidget, ButtonClickListener, WidgetDataReceiver {
    @NonNull
    private final MediaUtils mediaUtils;

    private final QuestionMediaManager questionMediaManager;
    private final WaitingForDataRegistry waitingForDataRegistry;

    private String binaryName;

    Button chooseFileButton;
    TextView chosenFileNameTextView;
    private LinearLayout answerLayout;

    public ArbitraryFileWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        this(context, prompt, new MediaUtils(), questionMediaManager, waitingForDataRegistry);
    }

    ArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull MediaUtils mediaUtils,
                        QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.mediaUtils = mediaUtils;
        this.questionMediaManager = questionMediaManager;
        this.waitingForDataRegistry = waitingForDataRegistry;

        binaryName = questionDetails.getPrompt().getAnswerText();

        setUpLayout(context);
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
        answerLayout.setVisibility(GONE);
        deleteFile();

        widgetValueChanged();
    }

    @Override
    public void onButtonClick(int buttonId) {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        performFileSearch();
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
                chosenFileNameTextView.setText(binaryName);
                answerLayout.setVisibility(VISIBLE);
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
        chooseFileButton.setOnLongClickListener(l);
        answerLayout.setOnLongClickListener(l);
    }

    private void setUpLayout(Context context) {
        LinearLayout widgetLayout = new LinearLayout(getContext());
        widgetLayout.setOrientation(LinearLayout.VERTICAL);

        chooseFileButton = createSimpleButton(getContext(), questionDetails.isReadOnly(), getContext().getString(R.string.choose_file), getAnswerFontSize(), this);
        chooseFileButton.setEnabled(!questionDetails.isReadOnly());

        answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.HORIZONTAL);
        answerLayout.setGravity(Gravity.CENTER);
        answerLayout.setTag("ArbitraryFileWidgetAnswer");

        ImageView attachmentImg = new ImageView(getContext());
        attachmentImg.setImageResource(R.drawable.ic_attachment);
        chosenFileNameTextView = createAnswerTextView(getContext(), binaryName, getAnswerFontSize());
        chosenFileNameTextView.setGravity(Gravity.CENTER);

        answerLayout.addView(attachmentImg);
        answerLayout.addView(chosenFileNameTextView);
        answerLayout.setVisibility(binaryName == null ? GONE : VISIBLE);
        answerLayout.setOnClickListener(view -> mediaUtils.openFile(getContext(), new File(getInstanceFolder() + File.separator + binaryName), null));

        widgetLayout.addView(chooseFileButton);
        widgetLayout.addView(answerLayout);

        addAnswerView(widgetLayout, WidgetViewUtils.getStandardMargin(context));
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // all file types
        ((Activity) getContext()).startActivityForResult(intent, ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER);
    }
}
