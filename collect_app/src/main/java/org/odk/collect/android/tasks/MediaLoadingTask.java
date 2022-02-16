package org.odk.collect.android.tasks;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_IMAGE_SIZE;

import android.net.Uri;
import android.os.AsyncTask;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.widgets.BaseImageWidget;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.androidshared.ui.DialogFragmentUtils;
import org.odk.collect.settings.SettingsProvider;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class MediaLoadingTask extends AsyncTask<Uri, Void, File> {

    @Inject
    SettingsProvider settingsProvider;

    private WeakReference<FormEntryActivity> formEntryActivity;

    public MediaLoadingTask(FormEntryActivity formEntryActivity) {
        onAttach(formEntryActivity);
    }

    public void onAttach(FormEntryActivity formEntryActivity) {
        this.formEntryActivity = new WeakReference<>(formEntryActivity);
        DaggerUtils.getComponent(this.formEntryActivity.get()).inject(this);
    }

    @Override
    protected File doInBackground(Uri... uris) {
        FormController formController = Collect.getInstance().getFormController();

        if (formController != null) {
            File instanceFile = formController.getInstanceFile();
            if (instanceFile != null) {
                String extension = ContentUriHelper.getFileExtensionFromUri(uris[0]);

                File newFile = FileUtils.createDestinationMediaFile(instanceFile.getParent(), extension);
                FileUtils.saveAnswerFileFromUri(uris[0], newFile, Collect.getInstance());
                QuestionWidget questionWidget = formEntryActivity.get().getWidgetWaitingForBinaryData();

                // apply image conversion if the widget is an image widget
                if (questionWidget instanceof BaseImageWidget) {
                    String imageSizeMode = settingsProvider.getUnprotectedSettings().getString(KEY_IMAGE_SIZE);
                    ImageConverter.execute(newFile.getPath(), questionWidget, formEntryActivity.get(), imageSizeMode);
                }
                return newFile;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        FormEntryActivity activity = this.formEntryActivity.get();
        DialogFragmentUtils.dismissDialog(FormEntryActivity.TAG_PROGRESS_DIALOG_MEDIA_LOADING, activity.getSupportFragmentManager());
        activity.setWidgetData(result);
    }
}
