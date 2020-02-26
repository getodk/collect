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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.utilities.FileWidgetUtils;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createAnswerTextView;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;

public class ArbitraryFileWidget extends QuestionWidget implements FileWidget {

    @NonNull
    private FileUtil fileUtil;

    @NonNull
    private MediaUtil mediaUtil;

    private String binaryName;

    Button chooseFileButton;
    TextView chosenFileNameTextView;
    private LinearLayout answerLayout;

    public ArbitraryFileWidget(Context context, QuestionDetails prompt) {
        this(context, prompt, new FileUtil(), new MediaUtil());
    }

    ArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull FileUtil fileUtil, @NonNull MediaUtil mediaUtil) {
        super(context, questionDetails);

        this.fileUtil = fileUtil;
        this.mediaUtil = mediaUtil;

        binaryName = questionDetails.getPrompt().getAnswerText();

        setUpLayout(context);
    }

    @Override
    public void deleteFile() {
        MediaManager
                .INSTANCE
                .markOriginalFileOrDelete(getFormEntryPrompt().getIndex().toString(),
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
        waitForData();
        performFileSearch();
    }

    @Override
    public void setBinaryData(Object object) {
        File newFile;
        // get the file path and create a copy in the instance folder
        if (object instanceof Uri) {
            String sourcePath = getSourcePathFromUri((Uri) object);
            String destinationPath = FileWidgetUtils.getDestinationPathFromSourcePath(sourcePath, getInstanceFolder(), fileUtil);
            File source = fileUtil.getFileAtPath(sourcePath);
            newFile = fileUtil.getFileAtPath(destinationPath);
            fileUtil.copyFile(source, newFile);
        } else if (object instanceof File) {
            // Getting a file indicates we've done the copy in the before step
            newFile = (File) object;
        } else {
            Timber.w("FileWidget's setBinaryData must receive a File or Uri object.");
            return;
        }

        if (newFile.exists()) {
            // when replacing an answer remove the current one.
            if (binaryName != null && !binaryName.equals(newFile.getName())) {
                deleteFile();
            }

            binaryName = newFile.getName();
            chosenFileNameTextView.setText(binaryName);
            answerLayout.setVisibility(VISIBLE);
            Timber.i("Setting current answer to %s", newFile.getName());

            widgetValueChanged();
        } else {
            Timber.e("Inserting Arbitrary file FAILED");
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

        chooseFileButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getContext().getString(R.string.choose_file), getAnswerFontSize(), this);
        chooseFileButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.HORIZONTAL);
        answerLayout.setGravity(Gravity.CENTER);

        ImageView attachmentImg = new ImageView(getContext());
        attachmentImg.setImageResource(R.drawable.ic_attachment);
        chosenFileNameTextView = createAnswerTextView(getContext(), binaryName, getAnswerFontSize());
        chosenFileNameTextView.setGravity(Gravity.CENTER);

        answerLayout.addView(attachmentImg);
        answerLayout.addView(chosenFileNameTextView);
        answerLayout.setVisibility(binaryName == null ? GONE : VISIBLE);
        answerLayout.setOnClickListener(view -> openFile());

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

    private String getSourcePathFromUri(@NonNull Uri uri) {
        return mediaUtil.getPathFromUri(getContext(), uri, MediaStore.Files.FileColumns.DATA);
    }

    public String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;
    }

    private void openFile() {

        Uri fileUri = Uri.fromFile(new File(getInstanceFolder() + File.separator + binaryName));
        Uri contentUri = FileProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                new File(getInstanceFolder() + File.separator + binaryName));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, getMimeType(getSourcePathFromUri(fileUri)));
        FileUtils.grantFileReadPermissions(intent, contentUri, getContext());

        if (new ActivityAvailability(getContext()).isActivityAvailable(intent)) {
            getContext().startActivity(intent);
        } else {
            String message = getContext().getString(R.string.activity_not_found, getContext().getString(R.string.open_file));
            ToastUtils.showLongToast(message);
            Timber.w(message);
        }
    }
}
