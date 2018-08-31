package org.odk.collect.android.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.GDriveConnectionException;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.ODKView;

import java.io.File;
import java.lang.ref.WeakReference;

import timber.log.Timber;

public class ImageLoadingTask extends AsyncTask<Uri, Void, File> {

    private WeakReference<FormEntryActivity> formEntryActivity;

    public ImageLoadingTask(FormEntryActivity formEntryActivity) {
        onAttach(formEntryActivity);
    }

    public void onAttach(FormEntryActivity formEntryActivity) {
        this.formEntryActivity = new WeakReference<>(formEntryActivity);
    }

    public void onDetach() {
        formEntryActivity = null;
    }

    @Override
    protected File doInBackground(Uri... uris) {

        File instanceFile;
        FormController formController = Collect.getInstance().getFormController();

        if (formController != null) {
            instanceFile = formController.getInstanceFile();
            if (instanceFile != null) {
                String instanceFolder = instanceFile.getParent();
                String destImagePath = instanceFolder + File.separator + System.currentTimeMillis() + ".jpg";

                File chosenImage;
                try {
                    chosenImage = MediaUtils.getFileFromUri(formEntryActivity.get(), uris[0], MediaStore.Images.Media.DATA);
                    if (chosenImage != null) {
                        final File newImage = new File(destImagePath);
                        FileUtils.copyFile(chosenImage, newImage);
                        ImageConverter.execute(newImage.getPath(), formEntryActivity.get().getWidgetWaitingForBinaryData(), formEntryActivity.get());
                        return newImage;
                    } else {
                        Timber.e("Could not receive chosen image");
                        ToastUtils.showShortToastInMiddle(R.string.error_occured);
                        return null;
                    }
                } catch (GDriveConnectionException e) {

                    Timber.e("Could not receive chosen image due to connection problem");
                    ToastUtils.showLongToastInMiddle(R.string.gdrive_connection_exception);
                    return null;
                }
            } else {
                ToastUtils.showLongToast(R.string.image_not_saved);
                Timber.w(formEntryActivity.get().getString(R.string.image_not_saved));
                return null;
            }
        }
        return null;

    }

    @Override
    protected void onPostExecute(File result) {
        Fragment prev = formEntryActivity.get().getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);
        ODKView odkView = formEntryActivity.get().getCurrentViewIfODKView();

        if (prev != null) {
            ((DialogFragment) prev).dismiss();
        }

        if (odkView != null) {
            odkView.setBinaryData(result);
        }
        formEntryActivity.get().saveAnswersForCurrentScreen(FormEntryActivity.DO_NOT_EVALUATE_CONSTRAINTS);
        formEntryActivity.get().refreshCurrentView();
    }
}
